/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.genomes;

public class AminoAcid {
    public static final byte A = 0;
    public static final byte C = 1;
    public static final byte D = 2;
    public static final byte E = 3;
    public static final byte F = 4;
    public static final byte G = 5;
    public static final byte H = 6;
    public static final byte I = 7;
    public static final byte K = 8;
    public static final byte L = 9;
    public static final byte M = 10;
    public static final byte N = 11;
    public static final byte P = 12;
    public static final byte Q = 13;
    public static final byte R = 14;
    public static final byte S = 15;
    public static final byte T = 16;
    public static final byte V = 17;
    public static final byte W = 18;
    public static final byte Y = 19;
    public static final byte STP = 20;

    public static byte parse(char c) {
        switch (c) {
        case 'A':
            return A;
        case 'C':
            return C;
        case 'D':
            return D;
        case 'E':
            return E;
        case 'F':
            return F;
        case 'G':
            return G;
        case 'H':
            return H;
        case 'I':
            return I;
        case 'K':
            return K;
        case 'L':
            return L;
        case 'M':
            return M;
        case 'N':
            return N;
        case 'P':
            return P;
        case 'Q':
            return Q;
        case 'R':
            return R;
        case 'S':
            return S;
        case 'T':
            return T;
        case 'V':
            return V;
        case 'W':
            return W;
        case 'Y':
            return Y;
        default:
            throw new RuntimeException("Cannot parse '" + c + "' as a nucelotide (acgt)");
        }
    }

    public static char asChar(byte state) {
        final char aminoAcidChars[]
            = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L',
                'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y',
                '*'};

        return aminoAcidChars[state];
    }
}
