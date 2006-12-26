package org.virion.simulator.genomes;

import org.virion.simulator.fitness.FitnessFunction;
import org.virion.simulator.fitness.FitnessFunctionFactor;

import java.util.SortedSet;

/**
 * TODO: add a Gene concept. This will change the interface of:
 * applyMutations, getSequence, getNucleotide and getAminoAcid.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Genome.java,v 1.7 2006/07/19 12:53:05 kdforc0 Exp $
 */
public interface Genome {
    int getTotalMutationCount();

    void applyMutations(SortedSet<Mutation> newMutations, FitnessFunction fitnessFunction);

    Sequence getSequence();

    int getLength();

    byte getNucleotide(int site);

    double getLogFitness();

    void setLogFitness(double logFitness);

    double getFitness();

    int getFrequency();

    void setFrequency(int frequency);

    void incrementFrequency();
    
    void setFitnessCache(FitnessFunction.FitnessGenomeCache cache);
    
    FitnessFunction.FitnessGenomeCache getFitnessCache();
}
