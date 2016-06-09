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
		// This is not correct since the introduction of indels or homologous recombination
		// We cannot use a fixed-size array to represent the state frequencies as genomes may be different sizes.
		// what do we use the state frequencies for anyway?
        stateFrequencies = new int[GenomeDescription.root.getGenomeLength()][4];
    }

    public void initialize() {
        unusedGenomes.clear();
        //unusedGenomes.addAll(genomes);
        genomes.clear();
        uniqueGenomeCount = 0;
    }


	public Genome createGenome(Sequence sequence, GenomeDescription gd) {
		Genome newGenome = createGenome(sequence);
		newGenome.setDescription(gd);

        return newGenome;
    }

	
    public int[][] getStateFrequencies() {
        calculateStateFrequencies();
        return stateFrequencies;
    }


	/**
	 * Return true if all genomes in this pool have the same GenomeDescription.
	 * This is a proxy for asking if indel mutations are enabled.
	 **/
	private boolean uniformPool() {
		GenomeDescription gd = genomes.peekFirst().getDescription();
        for (Genome genome : genomes) {
			if (genome.getDescription() != gd)
				return false;
		}
		return true;
	}


	
	/**
	 * calculate distribution of states across multiple sites over all genomes.
	 *
	 * Used by class AlleleFrequencySampler ( via
	 * Population:getAlleleFrequencies() ) to report allele
	 * frequencies.
	 *
	 **/
    public int[][] getStateFrequencies(Feature feature, Set<Integer> sites) {
        int[][] freqs = new int[sites.size()][feature.getAlphabet().getStateCount()];

		/*
		 * Cannot do this calculation if indels are turned on.
		 * Features will shift and shrink in each genome - features
		 * may not be common across genomes.  Fail if all genomes do
		 * not have the same GenomeDescription.
		 */
		if ( !uniformPool() )
			throw new RuntimeException("Cannot count state frequencies among genomes of different length.");
		
        for (Genome genome : genomes) {
            int freq = genome.getFrequency();
            byte[] states = genome.getStates(feature);
            int i = 0;
            for (int site : sites) {
                if (states[site] < feature.getAlphabet().getStateCount()) {
                    freqs[i][states[site]] += freq;
                }
                ++i;
            }
        }

        return freqs;
    }


	/**
	 * calculate a nucleotide consensus sequence across the entire
	 * length of all genomes in the pool.
	 *
	 * Difficulty: This only works if all genomes in the pool are the
	 * same length and are genealogically related.  When indels are
	 * turned on, sites will shift position relative to the same site
	 * in another genome, making it impossible to calculate a
	 * meaningful consensus without first aligning the sequences.
	 **/

    public Sequence getConsensusSequence() {
		/*
		 * Fail if all genomes do not have the same GenomeDescription.
		 */
		if ( !uniformPool() )
			throw new RuntimeException("Cannot calculate consensus among genomes of different length.");

        calculateStateFrequencies();
        SimpleSequence sequence = new SimpleSequence(GenomeDescription.root.getGenomeLength());

        for (int i = 0; i < sequence.getLength(); i++) {
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
		// zero-out the state frequencies matrix
		for (int[] row: stateFrequencies)
			Arrays.fill(row, 0);

		/*
		 * Fail if all genomes do not have the same GenomeDescription.
		 */
		if ( !uniformPool() )
			throw new RuntimeException("Cannot calculate consensus among genomes of different length.");

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
