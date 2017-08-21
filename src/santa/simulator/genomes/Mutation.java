/*
 * Mutation.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;


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
	
	public Range<Integer> apply(Range<Integer> r) { return(r); }

    public int compareTo(Mutation other) {
        return other.position - position;
    }

    public boolean equals(Object other) {
        return ((Mutation) other).position == position;
    }


	public int length() {
		return 1;
	}
	
	/**
	 * create a list of nucleotides changed by this mutation.
	 */
	public List<StateChange> getChanges(Genome genome, int[] featureSiteTable) {
		List<StateChange> l = new ArrayList<StateChange>();
		if (featureSiteTable[this.position] != -1) {
			byte oldState = genome.getNucleotide(this.position);
			StateChange c = new StateChange(featureSiteTable[this.position], oldState, this.state);
			l.add(c);
		}
		return (l);
	}

}
