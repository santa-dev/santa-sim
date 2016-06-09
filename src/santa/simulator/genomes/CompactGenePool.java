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
public class CompactGenePool extends BaseGenePool {

    public CompactGenePool() {
        super();
    }

    public Genome createGenome(Sequence sequence) {

        CompactGenome.setMasterSequence(sequence);

        CompactGenome newGenome = new CompactGenome(sequence);
        genomes.add(newGenome);

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
            CompactGenome oldGenome = (CompactGenome)genome;
            CompactGenome newGenome;

            if (unusedGenomes.size() > 0) {
                newGenome = (CompactGenome)unusedGenomes.removeFirst();
            } else {
                newGenome = new CompactGenome();
                genomes.add(newGenome);
            }
            newGenome.duplicate(oldGenome);

            newGenome.setFrequency(1);

	        fitnessFunction.updateLogFitness(newGenome, mutations);

	        newGenome.applyMutations(mutations);

	        fitnessFunction.updateLogFitness(newGenome);

            uniqueGenomeCount++;

            return newGenome;
        } else {
            genome.setFrequency(genome.getFrequency() + 1);
            return genome;
        }
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
            unusedGenomes.add(genome);
            uniqueGenomeCount--;
        }
    }

    public void finishGeneration(int generation) {
//        boolean updateMaster = (i != 0 && i % 10 == 0);
//        if (updateMaster) {
//            System.out.println("updating master");
//            byte[] consensus = genePool.getConsensusSequence();
//            changeMasterSequence(consensus);
//        }
    }

}
