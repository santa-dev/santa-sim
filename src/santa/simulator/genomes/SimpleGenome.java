package santa.simulator.genomes;

import java.util.SortedSet;

import santa.simulator.Random;


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

	/**
	 * Apply an array of mutations to the genome. The new mutation array may not have the mutations
	 * in positional order. The mutation array for the genome (mutations from the master sequence)
	 * must be in positional order.
	 * @param newMutations the array of new mutations in positional order
	 */
	@Override 
	public void applyMutations(SortedSet<Mutation> newMutations) {
		for (Mutation m : newMutations) {
			if (m.apply(this)) {
				incrementTotalMutationCount();
			}
		}
	}


	// Mutation effector functions.
	// These methods carry out the low-level transformations when mutation object apply themselves to this genome

	public boolean substitute(int position, byte state) {
		assert(state >= 0 && state <= 3);
		assert(position >=0 && position < sequence.getLength());

		byte oldState = sequence.getNucleotide(position);
		
		if (state != oldState) 
			sequence.setNucleotide(position, state);
		return(state != oldState);
	}


	// delete `count` elements beginning at `position`.
	// If there are not `count` elements beyond `position`, remove what is available. 
	public boolean delete(int position, int count) {
		// restrict indel lengths to multiples of 3 (count % 3) == 0.
		// See Issue #8 - https://github.com/matsengrp/santa-sim/issues/8
		assert(count >= 0);
		assert(position >=0 && position < sequence.getLength());
		count = Math.min(count, sequence.getLength()-position);
		if ((count % 3) == 0)
			return(sequence.deleteSubSequence(position, count));
		else
			return(false);
	}


	public boolean insert(int position, SimpleSequence seq) {
		// restrict indel lengths to multiples of 3 (count % 3) == 0.
		// See Issue #8 - https://github.com/matsengrp/santa-sim/issues/8
		if ((seq.getLength() % 3) == 0) 
			return(sequence.insertSequence(position, seq));
		else
			return(false);
	}


	// private members

	private SimpleSequence sequence = null;

}
