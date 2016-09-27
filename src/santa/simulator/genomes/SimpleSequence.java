/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.genomes;

import java.util.Arrays;
import java.util.SortedSet;

/**
 * @author kdforc0
 *
 * A genomic mutable sequence.
 */
public final class SimpleSequence implements Sequence {

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */

	/*
	 * Internal representation: every byte corresponds to a nucleotide.
	 */
	private byte states[];

	/**
	 * Create a new sequence with given nucleotide length. The new sequence
	 * is initialized to all A's.
	 */
	public SimpleSequence(int length) {
		states = new byte[length];
	}

	/**
	 * Create a new sequence with the given nucleotides.
	 */
	public SimpleSequence(String nucleotides) {
		states = new byte[nucleotides.length()];

		for (int i = 0; i < nucleotides.length(); ++i) {
			setNucleotide(i,  Nucleotide.parse(nucleotides.charAt(i)));
		}
	}


	public SimpleSequence(byte[] states) {
		// it is possible for {@code states} to be null at this point.
		if (states == null)
			states = new byte[0];
		this.states = states;
	}

	public SimpleSequence(Sequence other) {
		states = new byte[other.getLength()];

		copyNucleotides(0, other, 0, other.getLength());
	}

	public SimpleSequence(SimpleSequence other) {
		states = new byte[other.getLength()];

		copyNucleotides(0, other, 0, other.getLength());
	}


	public SimpleSequence(Sequence other, int start, int length) {
		states = new byte[length];

		copyNucleotides(0, other, start, start + length);
	}

	public SimpleSequence(SimpleSequence other, int start, int length) {
		states = new byte[length];

		copyNucleotides(0, other, start, length);
	}

	/* Override hashCode() and equals() so Sequence instamces can be
	   used as keys in hash collections,
	   e.g. Population::initialize().
	*/
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(states);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleSequence other = (SimpleSequence) obj;
		if (!Arrays.equals(states, other.states))
			return false;
		return true;
	}

	public Sequence getSubSequence(int start, int length) {
		return new SimpleSequence(this, start, length);
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getLength()
		 */
	public int getLength() {
		return states.length;
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getAminoacidsLength()
		 */
	public int getAminoAcidsLength() {
		return getLength() / 3;
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getNucleotide(int)
		 */
	public byte getNucleotide(int i) {
		return states[i];
	}

	public void setNucleotide(int i, byte state) {
		states[i] = state;
	}

	protected void copyNucleotides(int start, SimpleSequence source,
	                               int sourceStart, int sourceCount) {
		System.arraycopy(source.states, sourceStart, states, start, sourceCount);
	}

	protected void copyNucleotides(int start, Sequence source,
	                               int sourceStart, int sourceStop) {
		for (int i = 0; i < sourceStop - sourceStart; ++i) {
			setNucleotide(start + i, source.getNucleotide(sourceStart + i));
		}
	}
	
	public boolean deleteSubSequence(int pos, int count) {
		// assert that insert preserved frame...
		assert (states.length % 3) == 0;
		assert (count % 3) == 0;

		byte newstates[] = new byte[states.length - count];
		System.arraycopy(states, 0, newstates, 0, pos);
		System.arraycopy(states, pos+count, newstates, pos, states.length-pos-count);
		states = newstates;
		assert (states.length % 3) == 0;

		return(true);
	}
	
	public boolean insertSequence(int start, SimpleSequence source) {
		// allocate more space and copy the old contents
		// assert that insert preserved frame...
		// assert (states.length % 3) == 0;
		// assert (source.getLength() % 3) == 0;

		byte newstates[] = Arrays.copyOf(states, states.length + source.getLength());
		System.arraycopy(source.states, 0, newstates, start, source.getLength());
		System.arraycopy(states, start, newstates, start+source.getLength(), states.length-start);
		states = newstates;
		// assert (states.length % 3) == 0;
		return(true);
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getAminoAcid(int)
		 */
	public byte getAminoAcid(int i) {
		int aa_i = i * 3;

		return AminoAcid.STANDARD_GENETIC_CODE[getNucleotide(aa_i)]
				[getNucleotide(aa_i + 1)]
				[getNucleotide(aa_i + 2)];
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getNucleotides()
		 */
	public String getNucleotides() {
		StringBuffer result = new StringBuffer(getLength());
		result.setLength(getLength());

		for (int i = 0; i < getLength(); ++i) {
			result.setCharAt(i, Nucleotide.asChar(getNucleotide(i)));
		}

		return result.toString();
	}

	/* (non-Javadoc)
	 * @see santa.simulator.genomes.Sequence#getNucleotideStates()
	 */
	public byte[] getNucleotideStates() {
		byte[] result = new byte[getLength()];

		for (int i = 0; i < getLength(); ++i) {
			result[i] = getNucleotide(i);
		}

		return result;
	}

	/* (non-Javadoc)
		 * @see santa.simulator.genomes.Sequence#getAminoacids()
		 */
	public String getAminoAcids() {
		StringBuffer result = new StringBuffer(getAminoAcidsLength());
		result.setLength(getAminoAcidsLength());

		for (int i = 0; i < getAminoAcidsLength(); ++i) {
			result.setCharAt(i, AminoAcid.asChar(getAminoAcid(i)));
		}

		return result.toString();
	}

	/* (non-Javadoc)
	 * @see santa.simulator.genomes.Sequence#getAminoAcidStates()
	 */
	public byte[] getAminoAcidStates() {
		byte[] result = new byte[getAminoAcidsLength()];

		for (int i = 0; i < getAminoAcidsLength(); ++i) {
			result[i] = getAminoAcid(i);
		}

		return result;
	}

	public int getLength(SequenceAlphabet alphabet) {
		return getLength() / alphabet.getTokenSize();
	}

	public byte getState(SequenceAlphabet alphabet, int i) {
		if (alphabet == SequenceAlphabet.NUCLEOTIDES)
			return getNucleotide(i);
		else
			return getAminoAcid(i);
	}

	public String getStateString(SequenceAlphabet alphabet) {
		if (alphabet == SequenceAlphabet.NUCLEOTIDES)
			return getNucleotides();
		else
			return getAminoAcids();
	}

	public byte[] getStates(SequenceAlphabet alphabet) {
		if (alphabet == SequenceAlphabet.NUCLEOTIDES)
			return getNucleotideStates();
		else
			return getAminoAcidStates();
	}


	public Sequence recombineWith(Sequence other, SortedSet<Integer> breakPoints) {
		SimpleSequence[] parents = {this, (SimpleSequence) other};
		return SimpleSequence.getRecombinantSequence(parents, breakPoints);
	}
	
	/**
	 * calculate how long the product of recombination will be.
	 *
	 **/
	static int getRecombinantLength(SimpleSequence[] parents, SortedSet<Integer> breakPoints)  {
		assert(parents.length == 2);
		assert(parents[0].getLength() <= parents[1].getLength());

		/**
		 * NOTE: for non-homologous, isolocus recombination, the
		 * length of the product genome is completely determined by
		 * the length of the parent genomes and the modulus of the
		 * number of crossings.  Homologous recombination will have a
		 * similar shortcut.
		 **/
		int fastlen = parents[breakPoints.size() % 2 == 0 ? 0 : 1].getLength();

		return fastlen;
	}
	
	/**
	 * Factory method to create a recombined nucleotide sequence from two parents.
	 *
	 * Given a pair of parent sequences and a set of breakpoints,
	 * create a new sequence that is a combination of fragments from
	 * both parents.  breakPoints describes the positions at which we
	 * switch from one template to the other.
	 *
	 * If 'breakPoints' is empty, this routine simply copies the
	 * sequence from first genome in 'parents'.
	 *
	 * NOTE: This routine implements non-homologuous recombination.
	 * It uses only a single vector to specify isoloci in each parent
	 * where recombination should occur.  Homologous recombination
	 * would need two breakpoint vectors identifying the points of
	 * homology in each parent.
	 **/
    static SimpleSequence getRecombinantSequence(SimpleSequence[] parents, SortedSet<Integer> breakPoints) {
	 	assert(parents.length == 2);
		assert(parents[0].getLength() <= parents[1].getLength());

		int lastBreakPoint = 0;		// previous recombination location
		int currentSeq = 0;			// index of currently selected parent
		int newlen = getRecombinantLength(parents, breakPoints);
		SimpleSequence product = new SimpleSequence(newlen);

		byte[] dest = product.states;	// where to put the product
		SimpleSequence seq = parents[currentSeq];
		for (int nextBreakPoint : breakPoints) {
			System.arraycopy(seq.states, lastBreakPoint, 
							 dest, lastBreakPoint, nextBreakPoint-lastBreakPoint);
			
			lastBreakPoint = nextBreakPoint;
			currentSeq = 1 - currentSeq;
			seq = parents[currentSeq];
		}
		int nextBreakPoint =  seq.getLength();
		System.arraycopy(seq.states, lastBreakPoint, 
						 dest, lastBreakPoint, nextBreakPoint-lastBreakPoint);
		return(product);
	}
}
