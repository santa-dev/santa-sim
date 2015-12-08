package santa.simulator.genomes;

import santa.simulator.fitness.FitnessFunction;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: GenePool.java,v 1.8 2006/07/19 12:53:05 kdforc0 Exp $
 */
public interface GenePool {

    void initialize();

    Genome createGenome(Sequence sequence);

    Genome createGenome(Sequence sequence, GenomeDescription gd);

    Genome duplicateGenome(Genome genome, SortedSet<Mutation> mutations, FitnessFunction fitnessFunction);

    void killGenome(Genome genome);

    void finishGeneration(int generation);

    int[][] getStateFrequencies();

    int[][] getStateFrequencies(Feature feature, Set<Integer> sites);

    Sequence getConsensusSequence();

    int hammingDistance(Genome genome1, Genome genome2);

    int getUniqueGenomeCount();

    int getUnusedGenomeCount();

    void updateAllFitnesses(FitnessFunction fitnessFunction);

    List<Genome> getGenomes();
}
