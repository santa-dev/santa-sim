package santa.simulator.genomes;

import santa.simulator.fitness.FitnessFunction;

import java.util.SortedSet;
import java.util.List;

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

    void applyMutations(SortedSet<Mutation> newMutations);

    Sequence getSequence();

    int getLength();

    byte getNucleotide(int site);

	byte[] getNucleotides(Feature feature);

	byte[] getStates(Feature feature);

	List<StateChange> getChanges(Feature feature, SortedSet<Mutation> newMutations);

    double getLogFitness();

    void setLogFitness(double logFitness);

    double getFitness();

    int getFrequency();

    void setFrequency(int frequency);

    void incrementFrequency();

    void setFitnessCache(FitnessFunction.FitnessGenomeCache cache);

    FitnessFunction.FitnessGenomeCache getFitnessCache();

	boolean substitute(int position, byte state);

	boolean delete(int position, int count);

	boolean insert(int position, SimpleSequence seq);

    int binomialDeviate(double mutationRate);

	GenomeDescription getDescription();
	void setDescription(GenomeDescription gd);

}
