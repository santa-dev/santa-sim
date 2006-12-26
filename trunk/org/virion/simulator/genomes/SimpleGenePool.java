/*
 * GenePool.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package org.virion.simulator.genomes;

import java.util.SortedSet;

import org.virion.simulator.fitness.FitnessFunction;

/**
 * @author rambaut
 *         Date: Apr 22, 2005
 *         Time: 9:12:27 AM
 */
public class SimpleGenePool extends BaseGenePool {

    public SimpleGenePool() {
        super();
    }

    public Genome createGenome(Sequence sequence) {
        SimpleGenome newGenome = recycleOrCreateGenome(sequence);
        uniqueGenomeCount++;

        return newGenome;
    }

    /**
     * Duplicates a genome with mutations given by the array of mutations. If no mutations are
     * required then the original genome is returned but with the frequency incremented.
     *
     * @param genome the genome object
     * @param mutations the array of Mutation objects
     * @return the new replicated genome
     */
    public Genome duplicateGenome(Genome genome, SortedSet<Mutation> mutations, FitnessFunction fitnessFunction) {

        if (mutations.size() > 0) {
            SimpleGenome oldGenome = (SimpleGenome)genome;
            SimpleGenome newGenome;

            newGenome = recycleOrCreateGenome(null);
            newGenome.duplicate(oldGenome);

            newGenome.setFrequency(1);
            newGenome.applyMutations(mutations, fitnessFunction);

            uniqueGenomeCount++;

            return newGenome;
        } else {
            genome.setFrequency(genome.getFrequency() + 1);

            return genome;
        }
    }

    /**
     * @return
     */
    private SimpleGenome recycleOrCreateGenome(Sequence s) {
        SimpleGenome newGenome;
        if (unusedGenomes.size() > 0) {
            newGenome = (SimpleGenome)unusedGenomes.removeFirst();
        } else {
            if (s == null)
                newGenome = new SimpleGenome();
            else
                newGenome = new SimpleGenome(s);
            genomes.add(newGenome);
        }
        return newGenome;
    }

    public void killGenome(Genome genome) {

        int frequency = genome.getFrequency();
        if (frequency < 1) {
            throw new IllegalArgumentException("This genome has already been killed");
        }
        if (frequency > 1) {
            genome.setFrequency(frequency - 1);
        } else {
            genome.setFrequency(0);
            unusedGenomes.add((SimpleGenome)genome);
            uniqueGenomeCount--;
        }
    }

    public void finishGeneration(int generation) {
        // nothing to do
    }

}
