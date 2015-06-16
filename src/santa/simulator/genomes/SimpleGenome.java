package santa.simulator.genomes;

import java.util.SortedSet;

/**
 * @author Andrew Rambaut
 */
public class SimpleGenome extends BaseGenome {
	public SimpleGenome() {
	}

	/**
	 * Retrieve a contiguous byte array of nucleotides corresponding to a feature.
	 *
	 * Adjust feature coordinates to reflect indel
	 * mutations.  Feature coordinates are relative to the original
	 * founder genome.  Before using those coordinates to retrieve
	 * nucleotides, they must be updated to take into account all the
	 * overlapping indel events that have occurred on this genome.
	 * @return byte array of nucleotides (encoded as ints? as characters? )
	 **/
	@Override 
	public byte[] getNucleotides(Feature feature) {
		int total = feature.getNucleotideLength();	// total width of feature

		byte[] nucleotides = new byte[total];
		int k = 0;
		for (int i = 0; i < feature.getFragmentCount(); i++) {
			int start = feature.getFragmentStart(i);
			int finish = feature.getFragmentFinish(i);
			
			/*
			it looks like this code is prepared for 'finish' to be less than 'start'.
			Why do we need to support that?  It doesn't seem to have anything to do with supporting fitness on the non-template DNA strand.
			So why not just always assume (or ensure) that start < finish?
			*/

			if (start < finish) {
				for (int j = start; j <= finish; j++) {
					nucleotides[k] = getNucleotide(j);
					k++;
				}
			} else {
				for (int j = finish; j >= start; j--) {
					nucleotides[k] = getNucleotide(j);
					k++;
				}
			}
		}

		return nucleotides;
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
		GenomeDescription gd = null;
		
		for (Mutation m : newMutations) {
			int l = getLength();
			if (m.apply(this)) {

				// If the genome length changes, the genomedescription object mut be updated.  We
				// keep genome descriptions in a tree so we can trace the evolutionary history of
				// indel events for a genome.
				int nl = getLength();
				if (l != nl) {
					if (gd == null) {
						gd = new GenomeDescription(descriptor, m);
						descriptor = gd;
					}
				}
				incrementTotalMutationCount();
			}
		}
	}


	/**
	 * Substitution mutation effector function.  Substitute one
	 * nucleotide at a single site for another.  Substitution will be
	 * ignored if position is not within bounds of genome.
	 *
	 * @param position integer position relataive to beginning of genome where the substitution should be made.
	 * @param state new nucleotie at site.  Coded as integer 0-3 = {A,C,G,T}
	 * @return boolean indication whether state was changed at designated site.
	 **/
	public boolean substitute(int position, byte state) {
		assert(state >= 0 && state <= 3);
		assert(position >=0 && position < sequence.getLength());

		byte oldState = sequence.getNucleotide(position);
		
		if (state != oldState) 
			sequence.setNucleotide(position, state);
		return(state != oldState);
	}


	/**
	 * Deletion mutation effector function.  Deletes nucleotides at
	 * designated position relative to start of genome.  If there are
	 * not `count` elements beyond `position`, remove what is
	 * available.  Can only delete on integral codon boundary, and
	 * integral codon lengths.
	 * @param position non-negative integer position of first nucletide to be deleted.
	 * @param count number of nucleotides to be deleted.
	 * @return boolean indication of success
	 **/
	public boolean delete(int position, int count) {
		// restrict indel lengths to multiples of 3 (count % 3) == 0.
		// See Issue #8 - https://github.com/matsengrp/santa-sim/issues/8
		assert(count >= 0);
		assert(position >=0 && position < sequence.getLength());
		count = Math.min(count, sequence.getLength()-position);
		boolean status = false;
		if ((count % 3) == 0) {
			status = sequence.deleteSubSequence(position, count);
		}
		return(status);

  	}


	/**
	 * Insertion mutation effector function.  Inserts nucleotides at
	 * designated position relative to start of genome.  Can only
	 * insert integral codon lengths on integral codon boundary.
	 * @param position non-negative integer position at which insertion begins.
	 * @param seq nucleotides to be inserted.
	 * @return boolean indication of success
	 **/
	public boolean insert(int position, SimpleSequence seq) {
		// restrict indel lengths to multiples of 3 (count % 3) == 0.
		// See Issue #8 - https://github.com/matsengrp/santa-sim/issues/8
		boolean status = false;
		if ((seq.getLength() % 3) == 0) {
			status = sequence.insertSequence(position, seq);
		}
		return(status);
	}


	// private members

	/** 
	 * The sequence of nucleotides making up this genome.  
	 * This sequence is continually maintained in the face of substitutions, insertions, and deletions.
	 */
	private SimpleSequence sequence = null;

}
