/*
 * GenePool.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

import santa.simulator.fitness.FitnessFunction;

import java.util.SortedSet;

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
        newGenome.setFrequency(0);
        uniqueGenomeCount++;

        return newGenome;
    }

    /**
     * Duplicate a genome and apply mutations. 
     *
     * The implementation is a little tricky as this routine tries to
     * minimized recalculation of fitness functions when it can.  If
     * any of the mutations are insertions or deletions, cached
     * fitness values are bypassed and a full recalculation is
     * performed.
     *
	 * {@code mutations} is list of mutation objects which may include
	 * multiple substitutions along with a single insertion or
	 * deletion; multiple indels in a single call are not supported.
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

            fitnessFunction.updateLogFitness(newGenome, mutations);

            newGenome.applyMutations(mutations);

            fitnessFunction.updateLogFitness(newGenome);

            uniqueGenomeCount++;

            return newGenome;
        } else {
            genome.incrementFrequency();
            fitnessFunction.updateLogFitness(genome);
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
            newGenome = new SimpleGenome();
            genomes.add(newGenome);
        }

        if (s != null) {
            newGenome.setSequence(s);
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
