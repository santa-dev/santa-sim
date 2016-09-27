/*
 * Population.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.population;


import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

import santa.simulator.NotImplementedException;
import santa.simulator.Random;
import santa.simulator.Virus;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.Feature;
import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.Sequence;
import santa.simulator.mutators.Mutator;
import santa.simulator.phylogeny.Phylogeny;
import santa.simulator.replicators.Replicator;
import santa.simulator.selectors.Selector;

/**
 * @author rambaut
 *         Date: Apr 22, 2005
 *         Time: 9:12:27 AM
 */
public abstract class Population {

    public int getPopulationSize() {
        return getCurrentGeneration().size();
    }

    public Population(GenePool genePool, Selector selector, Phylogeny phylogeny) {
        this.phylogeny = phylogeny;
        this.genePool = genePool;
        this.selector = selector;

        lastGeneration = new ArrayList<Virus>();
        currentGeneration = new ArrayList<Virus>();
    }

    public void initialize(List<Sequence> inoculum, int initialPopulationSize) {
        Genome[] ancestors;

        genePool.initialize();
        currentGeneration.clear();
        lastGeneration.clear();
        if (inoculum.size() > 0) {
            //  Build `ancestors` array holding Genomes corresponding to each entry in the
            // inoculum.  Identical sequences share a single Genome instance (via the HashMap).
            Map<Sequence,Genome> seqmap = new HashMap<Sequence,Genome>();
            ancestors = new Genome[inoculum.size()];
            for (int i = 0; i < ancestors.length; i++) {
                Sequence s = inoculum.get(i);
                Genome g = seqmap.get(s);
                if (g == null) {
                    g = genePool.createGenome(s);
                    seqmap.put(s, g);
                }
                ancestors[i] = g;
            }
        } else {
            throw new NotImplementedException();

            // create a default nucleotide sequence
            // ancestors = new Genome[1];
            // Sequence sequence = new SimpleSequence(GenomeDescription.getGenomeLength());
            // ancestors[0] = genePool.createGenome(sequence);
        }

        if (ancestors.length >= initialPopulationSize) {
            // Allow experimenting with deterministic initial populations.
            // If the inoculum is the same size or greater than the population,
            // use the supplied inoculum as the complete population without any random selection.
            for (int i = 0; i < initialPopulationSize; i++) {
                Genome ancestor = ancestors[i];
                currentGeneration.add(new Virus(ancestor, null));
                lastGeneration.add(new Virus());
                ancestor.incrementFrequency();
            }
        } else if (ancestors.length > 1) {
            for (int i = 0; i < initialPopulationSize; i++) {
                Genome ancestor = ancestors[Random.nextInt(0, ancestors.length - 1)];
                currentGeneration.add(new Virus(ancestor, null));
                lastGeneration.add(new Virus());
                ancestor.incrementFrequency();
            }
        } else {
            for (int i = 0; i < initialPopulationSize; i++) {
                currentGeneration.add(new Virus(ancestors[0], null));
                lastGeneration.add(new Virus());
                ancestors[0].incrementFrequency();
            }
        }

        if (phylogeny != null) {
            phylogeny.initialize();
        }
    }

    protected abstract void select(List<Virus> current, List<Integer> selectedParents, int parentCount, int generation);


    public void selectNextGeneration(int generation, Replicator replicator, Mutator mutator, FitnessFunction fitnessFunction) {
        List<Integer> selectedParents = new ArrayList<Integer>();
        select(currentGeneration, selectedParents, replicator.getParentCount(), generation);

        Virus[] parents = new Virus[replicator.getParentCount()];

        lastGeneration.clear();
        lastGeneration.addAll(currentGeneration);
        currentGeneration.clear();

        // then select the currentGeneration based on the last.
        for (int currentParent = 0; currentParent < selectedParents.size() - replicator.getParentCount() + 1;) {
            for (int j = 0; j < parents.length; j++) {
                parents[j] = lastGeneration.get(selectedParents.get(currentParent));
                currentParent++;
            }

            // replicate the parents to create a new virus
            Virus child = new Virus();
            replicator.replicate(child, parents, mutator, fitnessFunction, genePool);
            currentGeneration.add(child);
        }

        // then kill off the genomes in the last population.
        for (Virus v : lastGeneration) {
            genePool.killGenome(v.getGenome());
        }
///////////////////////////////////
        if (phylogeny != null) {
            phylogeny.addGeneration(generation, selectedParents);
        }
///////////////////////////////
        statisticsKnown = false;
    }

    public void updateAllFitnesses(FitnessFunction fitnessFunction) {
        genePool.updateAllFitnesses(fitnessFunction);
        statisticsKnown = false;
    }

    protected Virus[] getSample(int sampleSize) {
        List<Virus> viruses = getCurrentGeneration();
        sampleSize = Math.min(sampleSize, viruses.size());
        Object[] tmp = Random.nextSample(viruses, sampleSize);

        Virus[] sample = new Virus[tmp.length];
        System.arraycopy(tmp, 0, sample, 0, tmp.length);
        return sample;
    }

    public void estimateDiversity(int sampleSize) {
        Virus[] sample = getSample(sampleSize);

        maxDiversity = 0;
        meanDiversity = 0;

        int count = 0;
        for (int i = 0; i < sample.length; ++i) {
            for (int j = i+1; j < sample.length; ++j) {
                double d = computeDistance(sample[i], sample[j]);
                if (d > maxDiversity)
                    maxDiversity = d;
                meanDiversity += d;
                ++count;
            }
        }

        meanDiversity /= (double)count;
    }

    private double computeDistance(Virus virus1, Virus virus2) {
        if (virus1.getGenome() == virus2.getGenome())
            return 0;

        if (virus1.getGenome().getDescription() != virus2.getGenome().getDescription()) {
            // we don't know how to calculate distances between sequences when the sequences diverge due to indels.
            // We can theoretically reconstruct an accurate pairwise alignment for any two related sequences.
            // we'll have to implement that later.  for now just comment this out - revisit later - csw
            throw new RuntimeException("Cannot compute distances if genomes differ in length");
        }

       Sequence seq1 = virus1.getGenome().getSequence();
       Sequence seq2 = virus2.getGenome().getSequence();

       int distance = 0;

        for (int i = 0; i < virus1.getGenome().getLength(); ++i) {
            if (seq1.getNucleotide(i) != seq2.getNucleotide(i))
                ++distance;
        }

       return distance;
    }

    private void collectStatistics() {

        double d = 0;
        maxFrequency = 0;

        mostFrequentGenome = null;
        sumFitness = 0.0;
        minFitness = Double.MAX_VALUE;
        maxFitness = 0.0;
        double nFitness = 0;

        for (Virus v : currentGeneration) {
            Genome genome = v.getGenome();

            d += genome.getTotalMutationCount();

            if (genome.getFrequency() > maxFrequency) {
                mostFrequentGenome = v.getGenome();
                maxFrequency = genome.getFrequency();
            }

            double fitness = genome.getFitness();
            if (fitness != 0) {
                sumFitness += fitness;
                nFitness += 1;
                if (genome.getFitness() > maxFitness) {
                    maxFitness = genome.getFitness();
                }
                if (genome.getFitness() < minFitness) {
                    minFitness = genome.getFitness();
                }
            }
        }

        if (nFitness > 0)
            meanFitness = sumFitness / nFitness;
        meanDistance = d / currentGeneration.size();

        statisticsKnown = true;
    }

    public GenePool getGenePool() {
        return genePool;
    }

    public int getMaxFrequency() {
        if (!statisticsKnown) {
            collectStatistics();
        }
        return maxFrequency;
    }

    public double getMaxFitness() {
        if (!statisticsKnown) {
            collectStatistics();
        }
        return maxFitness;
    }

    public double getMinFitness() {
        if (!statisticsKnown) {
            collectStatistics();
        }
        return minFitness;
    }

    public double getSumFitness() {
        if (!statisticsKnown) {
            collectStatistics();
        }
        return sumFitness;
    }

    public double getMeanDistance() {
        if (!statisticsKnown) {
            collectStatistics();
        }
        return meanDistance;
    }

    public double getMeanFitness() {
        if (!statisticsKnown) {
            collectStatistics();
        }
        return meanFitness;
    }

    public Genome getMostFrequentGenome() {
        if (!statisticsKnown) {
            collectStatistics();
        }
        return mostFrequentGenome;
    }

    public double getMaxDiversity() {
        return maxDiversity;
    }

    public double getMeanDiversity() {
        return meanDiversity;
    }

    public double[][] getAlleleFrequencies(Feature feature, Set<Integer> sites) {
        int[][] freqs = genePool.getStateFrequencies(feature, sites);
        double[][] normalizedFreqs = new double[sites.size()][freqs[0].length];
        for (int i = 0; i < sites.size(); i++) {
            for (int j = 0; j < freqs[i].length; j++) {
                normalizedFreqs[i][j] = freqs[i][j] / (double) currentGeneration.size();
            }
        }
        return normalizedFreqs;
    }

    public List<Virus> getCurrentGeneration() {
        return currentGeneration;
    }

    public Phylogeny getPhylogeny() {
        return phylogeny;
    }

    private final GenePool genePool;

    protected final Selector selector;

    private final Phylogeny phylogeny;

    private List<Virus> lastGeneration;
    private List<Virus> currentGeneration;

    private boolean statisticsKnown = false;

    private Genome mostFrequentGenome;
    private int maxFrequency;
    private double meanDistance;

    private double meanFitness;
    private double sumFitness;
    private double minFitness;
    private double maxFitness;
    private double maxDiversity;
    private double meanDiversity;
}
