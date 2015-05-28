/*
 * Mutation.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

import santa.simulator.genomes.Genome;

/**
 * @author rambaut
 *         Date: Apr 26, 2005
 *         Time: 10:28:35 AM
 *         
 */
public class Mutation  implements Comparable<Mutation>  {
    protected Mutation() { state = 0; }

    private Mutation(int position, byte state) {
        this.position = position;
        this.state = state;
    }

    /*
     * position is 0-based -- koen.
     */
    public int position;
    public final byte state;

    public static Mutation getMutation(int position, byte state) {
        return new Mutation(position, state);
    }

	public boolean apply(Genome genome) {
		return(genome.substitute(position, state));
	}

    public int compareTo(Mutation other) {
        return other.position - position;
    }

    public boolean equals(Object other) {
        return ((Mutation) other).position == position;
    }
}
