/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.virion.simulator.genomes;

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
     * Get the amino acid sequence length.
     */
    int getAminoAcidsLength();
    
    /**
     * Get the sequence length for the specified alphabet.
     */
    int getLength(SequenceAlphabet alphabet);
    
    /**
     * Get the nucleotide at position i (0 .. length() - 1).
     */
    byte getNucleotide(int i);

    /**
     * Get the amino acid at amino acid position i (0 .. length()/3 - 1).
     */
    byte getAminoAcid(int i);

    /**
     * Get the state for the given alphabet at the given position.
     */
    byte getState(SequenceAlphabet alphabet, int i);
    
    /**
     * Get a String representation of the nucleotide sequence.
     */
    String getNucleotides();

    /**
     * Get a String representation of the amino acid sequence.
     */
    String getAminoAcids();

    /**
     * Get a String representation in the given alphabet.
     */
    String getStates(SequenceAlphabet alphabet);
    
    /**
     * Get a subsequence.
     */
    Sequence getSubSequence(int start, int length);
}