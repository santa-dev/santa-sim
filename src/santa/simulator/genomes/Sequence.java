/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.genomes;

import java.util.SortedSet;

/**
 * The interface for a Sequence.
 *
 * The Sequence represents a string of nucleotides, which may be translated
 * int amino acids.
 */
public interface Sequence {
    /**
     * Get the sequence length as number of nucleotides.
     */
    int getLength();

    /**
     * Get the sequence length for the specified alphabet.
     */
    int getLength(SequenceAlphabet alphabet);

    /**
     * Get the nucleotide at position i (0 .. length() - 1).
     */
    byte getNucleotide(int i);

    /**
     * Get the state for the given alphabet at the given
     * position (0  ..  length() - 1).
     *
     * The position is always the nucleotide position. If the alphabet
     * is amino acids, then position must be &lt;= length() - 3
     */
    byte getState(SequenceAlphabet alphabet, int i);

    /**
     * Get a String representation of the nucleotide sequence.
     */
    String getNucleotides();

	/**
	 * Get an array representation of the nucleotide sequence.
	 */
	byte[] getNucleotideStates();

    /**
     * Get a String representation of the amino acid sequence.
     */
    String getAminoAcids();

	/**
	 * Get an array representation of the amino acid sequence.
	 */
	byte[] getAminoAcidStates();

	/**
	 * Get a string representation in the given alphabet.
	 */
	String getStateString(SequenceAlphabet alphabet);

    /**
     * Get an array representation in the given alphabet.
     */
    byte[] getStates(SequenceAlphabet alphabet);

    /**
     * Get a subsequence.
     */
    Sequence getSubSequence(int start, int length);

	/**
	 * Create a hybrid sequence by combining two parents.
	 */
	Sequence recombineWith(Sequence other, SortedSet<Integer> breakPoints);
}
