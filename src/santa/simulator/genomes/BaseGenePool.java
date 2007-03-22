package santa.simulator.genomes;

import santa.simulator.fitness.FitnessFunction;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: BaseGenePool.java,v 1.7 2006/07/19 12:53:05 kdforc0 Exp $
 */
public abstract class BaseGenePool implements GenePool {
    protected int uniqueGenomeCount = 0;
    protected final int[][] stateFrequencies;
    protected final LinkedList<Genome> genomes = new LinkedList<Genome>();
    protected final LinkedList<Genome> unusedGenomes = new LinkedList<Genome>();

    public LinkedList<Genome> getGenomes() {
        return genomes;
    }

    public BaseGenePool() {
        stateFrequencies = new int[GenomeDescription.getGenomeLength()][4];
    }

    public void initialize() {
        unusedGenomes.clear();
        //unusedGenomes.addAll(genomes);
        genomes.clear();
        uniqueGenomeCount = 0;
    }

    public int[][] getStateFrequencies() {
        calculateStateFrequencies();
        return stateFrequencies;
    }

    public int[] getStateFrequencies(int site) {
        return getStateFrequencies(site, SequenceAlphabet.NUCLEOTIDES);
    }

    public int[] getStateFrequencies(int site, SequenceAlphabet alphabet) {
        int[] freqs = new int[alphabet.getStateCount()];
        int totalfreq = 0;
        for (Genome genome : genomes) {
            int freq = genome.getFrequency();
            byte state = genome.getSequence().getState(alphabet, site);
            if (state < alphabet.getStateCount())
                freqs[state] += freq;
            totalfreq += freq;
        }

        return freqs;
    }

    public Sequence getConsensusSequence() {
        calculateStateFrequencies();
        SimpleSequence sequence = new SimpleSequence(GenomeDescription.getGenomeLength());

        for (int i = 0; i < GenomeDescription.getGenomeLength(); i++) {
            sequence.setNucleotide(i, Nucleotide.A);
            int freq = stateFrequencies[i][Nucleotide.A];
            for (byte j = 1; j < 4; j++) {
                if (freq < stateFrequencies[i][j]) {
                    freq = stateFrequencies[i][j];
                    sequence.setNucleotide(i, j);
                }
            }
        }

        return sequence;
    }

    private void calculateStateFrequencies() {
        for (int i = 0; i < stateFrequencies.length; i++) {
            for (int j = 0; j < stateFrequencies[0].length; j++) {
                stateFrequencies[i][j] = 0;
            }
        }

        for (Genome genome : genomes) {
            int freq = genome.getFrequency();
            Sequence sequence = genome.getSequence();
            for (int i = 0; i < sequence.getLength(); i++) {
                stateFrequencies[i][sequence.getNucleotide(i)] += freq;
            }
        }
    }

    public void updateAllFitnesses(FitnessFunction fitnessFunction) {
        for (Genome genome : genomes) {
            fitnessFunction.computeLogFitness(genome);
        }
    }

    /**
     * Returns the hamming distance (absolute number of differences) between two genomes
     * @param genome1 a genome
     * @param genome2 another genome
     * @return the hamming distance
     */
    public int hammingDistance(Genome genome1, Genome genome2) {
        return hammingDistance(genome1.getSequence(), genome2.getSequence());
    }

    public int hammingDistance(Sequence sequence1, Sequence sequence2) {
        int distance = 0;
        for (int i = 0; i < sequence1.getLength(); i++) {
            if (sequence1.getNucleotide(i) != sequence2.getNucleotide(i)) {
                distance += 1;
            }
        }
        return distance;
    }

    public int getUniqueGenomeCount() {
        return uniqueGenomeCount;
    }

    public int getUnusedGenomeCount() {
        return unusedGenomes.size();
    }
}
