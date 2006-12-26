package org.virion.simulator.replicators;

import org.virion.simulator.Virus;
import org.virion.simulator.Random;
import org.virion.simulator.selectors.Selector;
import org.virion.simulator.genomes.*;
import org.virion.simulator.fitness.FitnessFunction;
import org.virion.simulator.mutators.Mutator;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.DistributionFactory;
import org.apache.commons.math.MathException;

import java.util.*;

/**
 * @author rambaut
 *         Date: Apr 27, 2005
 *         Time: 9:40:33 AM
 */
public class RecombinantReplicator implements Replicator {

    public RecombinantReplicator(double dualInfectionProbability, double recombinationProbability) {
        this.dualInfectionProbability = dualInfectionProbability;
        this.recombinationProbability = recombinationProbability;

        preCalculateBinomial(GenomeDescription.getGenomeLength() - 1, recombinationProbability);
    }

    public void replicate(Virus virus, Selector selector, Mutator mutator, FitnessFunction fitnessFunction, GenePool genePool) {

        if (Random.nextUniform(0.0, 1.0) < dualInfectionProbability) {
            // dual infection and recombination
            Virus parent1 = selector.nextSelection();
            Virus parent2 = selector.nextSelection();

            Genome parent1Genome = parent1.getGenome();
            Genome parent2Genome = parent2.getGenome();

            Sequence recombinantSequence = getRecombinantSequence(parent1Genome, parent2Genome);

            Genome genome = genePool.createGenome(recombinantSequence);
            genome.setLogFitness(fitnessFunction.computeLogFitness(genome));
            
	        SortedSet<Mutation> mutations = mutator.mutate(genome);

	        genome.setFrequency(1);
	        genome.applyMutations(mutations, fitnessFunction);

            virus.setGenome(genome);
            virus.setParent(parent1);

        } else {
            // single infection - no recombination...
            Virus parent = selector.nextSelection();

            Genome parentGenome = parent.getGenome();

            SortedSet<Mutation> mutations = mutator.mutate(parentGenome);

            Genome genome = genePool.duplicateGenome(parentGenome, mutations, fitnessFunction);

            virus.setGenome(genome);
            virus.setParent(parent);
        }

    }

    private Sequence getRecombinantSequence(Genome parent1Genome, Genome parent2Genome) {

        // First draw the number of break points
        int n = binomialDeviate();
        int[] breakPoints = new int[n];

        // Then draw the positions
        for (int i = 0; i < breakPoints.length; i++) {
            breakPoints[i] = Random.nextInt(1, parent1Genome.getLength() - 1);
        }
        Arrays.sort(breakPoints);

        // now create the recombinant by getting the list of mutations for
        // the recombinant segments donated by the second parent.

        int lastBreakPoint = 0;
        int currentGenome = 0;

	    SimpleSequence recombinantSequence = new SimpleSequence(parent1Genome.getSequence());
        for (int i = 0; i < breakPoints.length; i++) {
            if (currentGenome == 1) {
                // If this segment is given by the second parent...
                for (int j = lastBreakPoint; j < breakPoints[i]; j++) {
                    recombinantSequence.setNucleotide(j, parent2Genome.getNucleotide(j));
                }
            }

            lastBreakPoint = breakPoints[i];
            currentGenome = 1 - currentGenome;
        }

        return recombinantSequence;
    }

    protected int binomialDeviate() {
        double r = org.virion.simulator.Random.nextUniform(0.0, 1.0);
        for (int j = 0; j < binomial.length; j++) {
            if (r < binomial[j]) {
                return j;
            }
        }
        return binomial.length;
    }

    protected void preCalculateBinomial(int numExperiments, double eventRate) {
        binomial = new double[(int)(numExperiments * eventRate * 100)];

        BinomialDistribution distr
            = DistributionFactory.newInstance()
              .createBinomialDistribution(numExperiments, eventRate);
        
        for (int j = 0; j < binomial.length; ++j) {
            try {
                binomial[j] = distr.cumulativeProbability(j);
            } catch (MathException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final double dualInfectionProbability;
    private final double recombinationProbability;
    private double[] binomial;
}
