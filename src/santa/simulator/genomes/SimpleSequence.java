/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.genomes;

import java.util.Arrays;

/**
 * @author kdforc0
 *
 * A genomic mutable sequence.
 */
public final class SimpleSequence implements Sequence {
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
}
