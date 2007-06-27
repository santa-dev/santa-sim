/*
 * Population.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator;

import santa.simulator.mutators.Mutator;
import santa.simulator.genomes.*;
import santa.simulator.replicators.Replicator;
import santa.simulator.selectors.Selector;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.phylogeny.Phylogeny;

import java.util.*;

/**
 * @author rambaut
 *         Date: Apr 22, 2005
 *         Time: 9:12:27 AM
 */
public class Population {

    public Population(int populationSize, GenePool genePool, Selector selector, Phylogeny phylogeny) {

        this.phylogeny = phylogeny;

        this.populationSize = populationSize;
        this.genePool = genePool;
        this.selector = selector;

        lastGeneration = new Virus[populationSize];
        currentGeneration = new Virus[populationSize];
    }

    public void initialize(List<Sequence> inoculum) {
        Genome[] ancestors;

        genePool.initialize();

        if (inoculum.size() > 0) {
            // inoculum has sequences
            ancestors = new Genome[inoculum.size()];
            for (int i = 0; i < ancestors.length; i++) {
                Sequence sequence = inoculum.get(i);
                ancestors[i] = genePool.createGenome(sequence);
            }
        } else {
            // create a default nucleotide sequence
            ancestors = new Genome[1];
            Sequence sequence = new SimpleSequence(GenomeDescription.getGenomeLength());
            ancestors[0] = genePool.createGenome(sequence);
        }

        if (ancestors.length > 1) {
            for (int i = 0; i < populationSize; i++) {
                Genome ancestor = ancestors[Random.nextInt(0, ancestors.length - 1)];
                currentGeneration[i] = new Virus(ancestor, null);
                lastGeneration[i] = new Virus();
                ancestor.incrementFrequency();
            }
        } else {
            for (int i = 0; i < populationSize; i++) {
                currentGeneration[i] = new Virus(ancestors[0], null);
                lastGeneration[i] = new Virus();
                ancestors[0].incrementFrequency();
            }
        }

        if (phylogeny != null) {
            phylogeny.initialize();
        }
    }

    public void updateAllFitnesses(FitnessFunction fitnessFunction) {
        genePool.updateAllFitnesses(fitnessFunction);
        statisticsKnown = false;
    }

    public void selectNextGeneration(int generation, Replicator replicator, Mutator mutator, FitnessFunction fitnessFunction) {

        if (selectedParents == null) {
            selectedParents = new int[currentGeneration.length * replicator.getParentCount()];
        }

        selector.selectParents(currentGeneration, selectedParents);

        Virus[] parents = new Virus[replicator.getParentCount()];

        // first swap the arrays around
        Virus[] tmp = currentGeneration;
        currentGeneration = lastGeneration;
        lastGeneration = tmp;

        // then select the currentGeneration based on the last.
        int currentParent = 0;

        for (int i = 0; i < populationSize; i++) {

            if (parents.length == 1) {
                parents[0] = lastGeneration[selectedParents[currentParent]];
                currentParent ++;
            } else {
                for (int j = 0; j < parents.length; j++) {
                    parents[j] = lastGeneration[selectedParents[currentParent]];
                    currentParent ++;
                }
            }

            // replicate the parents to create a new virus
            replicator.replicate(currentGeneration[i], parents, mutator, fitnessFunction, genePool);

        }

        // then kill off the genomes in the last population.
        for (int i = 0; i < populationSize; i++) {
            genePool.killGenome(lastGeneration[i].getGenome());
        }

        if (phylogeny != null) {
            phylogeny.addGeneration(generation, selectedParents);
        }

        statisticsKnown = false;
    }

    protected Virus[] getSample(int sampleSize) {
        sampleSize = Math.min(sampleSize, populationSize);
        Virus[] viruses = getCurrentGeneration();
        Object[] tmp = Random.nextSample(Arrays.asList(viruses), sampleSize);
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

        Sequence seq1 = virus1.getGenome().getSequence();
        Sequence seq2 = virus2.getGenome().getSequence();

        int distance = 0;

        for (int i = 0; i < GenomeDescription.getGenomeLength(); ++i) {
            if (seq1.getNucleotide(i) != seq2.getNucleotide(i))
                ++distance;
        }

        return distance;
    }

    private void collectStatistics() {

        double d = 0;
        maxFrequency = 0;

        mostFrequentGenome = 0;
        sumFitness = 0.0;
        minFitness = Double.MAX_VALUE;
        maxFitness = 0.0;

        for (int i = 0; i < populationSize; i++) {
            Genome genome = currentGeneration[i].getGenome();

            d += genome.getTotalMutationCount();

            if (genome.getFrequency() > maxFrequency) {
                mostFrequentGenome = i;
                maxFrequency = genome.getFrequency();
            }

            sumFitness += genome.getFitness();
            if (genome.getFitness() > maxFitness) {
                maxFitness = genome.getFitness();
            }
            if (genome.getFitness() < minFitness) {
                minFitness = genome.getFitness();
            }
        }

        meanFitness = sumFitness / populationSize;
        meanDistance = d / populationSize;

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

    public int getPopulationSize() {
        return populationSize;
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

    public int getMostFrequentGenome() {
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
        int i = 0;
        for (int site : sites) {
            for (int j = 0; j < freqs[i].length; j++) {
                normalizedFreqs[i][j] = freqs[i][j] / (double)populationSize;
            }
            i++;
        }
        return normalizedFreqs;
    }

    public Virus[] getCurrentGeneration() {
        return currentGeneration;
    }

    public Phylogeny getPhylogeny() {
        return phylogeny;
    }

    private final int populationSize;

    private final GenePool genePool;

    private final Selector selector;

    private final Phylogeny phylogeny;

    private Virus[] lastGeneration;
    private Virus[] currentGeneration;
    private int[] selectedParents;

    private boolean statisticsKnown = false;

    private int mostFrequentGenome;
    private int maxFrequency;
    private double meanDistance;

    private double meanFitness;
    private double sumFitness;
    private double minFitness;
    private double maxFitness;
    private double maxDiversity;
    private double meanDiversity;
}
