package santa.simulator.genomes;

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
		this.descriptor = source.descriptor;
		setLogFitness(source.getLogFitness());
		assert(this.descriptor.getGenomeLength() == this.sequence.getLength());

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

		assert(descriptor.getGenomeLength() == getLength());

		for (Mutation m : newMutations) {
			int l = getLength();
			if (m.apply(this)) {

				// If the genome length changes, a new, updated GenomeDescription object must be created.  The 
				// genome description objects are linked together in a tree so we can trace the evolutionary history of
				// indel events for a genome.
				int nl = getLength();
				if (l != nl) {
					if (gd == null) {
						gd = new GenomeDescription(descriptor, m.position, m.length());
						descriptor = gd;
						this.fitnessCache = null;
						assert(descriptor.getGenomeLength() == getLength());
					}
				}
				assert(descriptor.getGenomeLength() == getLength());
				incrementTotalMutationCount();
			}
		}
		assert(descriptor.getGenomeLength() == sequence.getLength());
	}


	/**
	 * Substitution mutation effector function.  Substitute one
	 * nucleotide at a single site for another.  
	 *
	 * @param position   integer position relative to beginning of genome where the substitution should be made.
	 * @param state      new nucleotie at site.  Coded as integer 0-3 = {A,C,G,T}
	 * @return boolean   indication whether state was changed at designated site.
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
	 * available.  
	 *
	 * @param position non-negative integer position of first nucletide to be deleted.
	 * @param count number of nucleotides to be deleted.
	 * @return boolean indication of success
	 **/
	public boolean delete(int position, int count) {
		assert(count >= 0);
		assert(position >=0 && position < sequence.getLength());
		count = Math.min(count, sequence.getLength()-position);
		return sequence.deleteSubSequence(position, count);

  	}


	/**
	 * Insertion mutation effector function.  Inserts nucleotides at
	 * designated position relative to start of genome.  
	 * [ may not be trye: Can only
	 *   insert integral codon lengths on integral codon boundary.]
	 *
	 * @param position non-negative integer position at which insertion begins.
	 * @param seq nucleotides to be inserted.
	 * @return boolean indication of success
	 **/
	public boolean insert(int position, SimpleSequence seq) {
		return sequence.insertSequence(position, seq);
	}

	// private members

	/** 
	 * The sequence of nucleotides making up this genome.  
	 * This sequence is continually maintained in the face of substitutions, insertions, and deletions.
	 */
	private SimpleSequence sequence = null;

}
