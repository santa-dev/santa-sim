package santa.simulator.genomes;

import santa.simulator.fitness.FitnessFunction;

import java.util.SortedSet;


/**
 * @author Andrew Rambaut
 */
public class SimpleGenome extends BaseGenome {

	public SimpleGenome() {
	}

	public void duplicate(SimpleGenome source) {
		this.sequence = new SimpleSequence(source.sequence);
		this.fitnessCache = source.fitnessCache.clone();
		setLogFitness(source.getLogFitness());
	}

	/**
	 * Apply an array of mutations to the genome. The new mutation array may not have the mutations
	 * in positional order. The mutation array for the genome (mutations from the master sequence)
	 * must be in positional order.
	 * @param newMutations the array of new mutations in positional order
	 */
	public void applyMutations(SortedSet<Mutation> newMutations) {
		for (Mutation mutation : newMutations) {
			byte oldState = sequence.getNucleotide(mutation.position);

			if (mutation.state != oldState) {
				incrementTotalMutationCount();
				sequence.setNucleotide(mutation.position, mutation.state);
			}
		}
	}

	public int getLength() {
		return sequence.getLength();
	}

	public byte getNucleotide(int site) {
		return sequence.getNucleotide(site);
	}

	public void setSequence(Sequence sequence) {
		this.sequence = new SimpleSequence(sequence);
	}

	/**
	 * Gets a byte array representing the entire sequence. If the genome stores
	 * differences rather than a complete sequence, then this may be an inefficient
	 * way of accessing each state.
	 *
	 * @return a byte array containing sequence states
	 */
	public Sequence getSequence() {
		return sequence;
	}

	// private members

	private SimpleSequence sequence = null;

}