/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.genomes;

public class Nucleotide {
    public static final byte A = 0;
    public static final byte C = 1;
    public static final byte G = 2;
    public static final byte T = 3;

    public static byte parse(char c) {
        switch (c) {
        case 'A':
        case 'a':
            return A;
        case 'C':
        case 'c':
            return C;
        case 'G':
        case 'g':
            return G;
        case 'T':
        case 't':
            return T;
        default:
            throw new RuntimeException("Cannot parse '" + c + "' as a nucelotide (acgt)");
        }
    }

    public static char asChar(byte state) {
        final char nucleotideChars[] = { 'A', 'C', 'G', 'T' };

        return nucleotideChars[state];
    }
}
