/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.genomes;


public enum SequenceAlphabet {
    NUCLEOTIDES("nucleotide", 4, 1),
    AMINO_ACIDS("amino acid", 20, 3);

    SequenceAlphabet(String name, int stateCount, int tokenSize) {
        this.name = name;
        this.stateCount = stateCount;
        this.tokenSize = tokenSize;
    }

    public int getStateCount() {
        return stateCount;
    }

    public int getTokenSize() {
        return tokenSize;
    }

    public byte parse(char c) {
        switch(this) {
        case NUCLEOTIDES:
            return Nucleotide.parse(c);
        case AMINO_ACIDS:
            return AminoAcid.parse(c);
        default:
            throw new RuntimeException("Unknown alphabet");
        }
    }
    
    private final String name;
    private final int stateCount;
    private final int tokenSize;
    public String getName() {
        return name;
    }
}
