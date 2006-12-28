/*
 * Mutation.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

/**
 * @author rambaut
 *         Date: Apr 26, 2005
 *         Time: 10:28:35 AM
 */
public class Mutation implements Comparable<Mutation> {
    private Mutation(int position, byte state) {
        this.position = position;
        this.state = state;
    }

    public final int position;
    public final byte state;

    public static Mutation getMutation(int position, byte state) {
        return mutations[position][state];
    }

    private static Mutation[][] mutations = null;

    public static void initialize() {

        mutations = new Mutation[GenomeDescription.getGenomeLength()][];
        for (int i = 0; i < GenomeDescription.getGenomeLength(); i++) {
            mutations[i] = new Mutation[4];
            for (byte j = 0; j < 4; j++) {
                mutations[i][j] = new Mutation(i, j);
            }
        }
    }

    public int compareTo(Mutation other) {
        return other.position - position;
    }

    public boolean equals(Object other) {
        return ((Mutation) other).position == position;
    }
}
