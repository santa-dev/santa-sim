package org.virion.simulator.genomes;

import java.util.List;

import org.virion.simulator.fitness.FitnessFunction;
import org.virion.simulator.fitness.FitnessFunctionFactor;

import java.util.SortedSet;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: GenePool.java,v 1.8 2006/07/19 12:53:05 kdforc0 Exp $
 */
public interface GenePool {

    void initialize();

    Genome createGenome(Sequence sequence);

    Genome duplicateGenome(Genome genome, SortedSet<Mutation> mutations, FitnessFunction fitnessFunction);

    void killGenome(Genome genome);

    void finishGeneration(int generation);

    int[][] getStateFrequencies();

    int[] getStateFrequencies(int site);

    Sequence getConsensusSequence();

    int hammingDistance(Genome genome1, Genome genome2);

    int getUniqueGenomeCount();

    int getUnusedGenomeCount();

    void updateFitness(FitnessFunction fitnessFunction);
    
    List<Genome> getGenomes();
}
