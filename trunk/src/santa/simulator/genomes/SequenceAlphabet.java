/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.genomes;

public enum SequenceAlphabet {
    NUCLEOTIDES(4, 1),
    AMINO_ACIDS(20, 3);

    SequenceAlphabet(int stateCount, int tokenSize) {
        this.stateCount = stateCount;
        this.tokenSize = tokenSize;
    }

    public int getStateCount() {
        return stateCount;
    }

    public int getTokenSize() {
        return tokenSize;
    }

    private final int stateCount;
    private final int tokenSize;

}
