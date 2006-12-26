/*
 * Created on Jul 17, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.virion.simulator.genomes;

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

    public static char asChar(byte state) {
        final char aminoAcidChars[]
            = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L',
                'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y',
                '*'};

        return aminoAcidChars[state];
    }
}
