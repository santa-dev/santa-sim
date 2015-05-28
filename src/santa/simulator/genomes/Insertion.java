/*
 * Insertion.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

/**
 * @file   Insertion.java
 * @author cswarth
 * @date   Fri Jan  9 14:15:48 2015
 * 
 * 
 */

public class Insertion extends Mutation {
    public Insertion(int position, SimpleSequence insert) {
        this.position = position;
        this.seq = insert;
    }

	public boolean apply(Genome genome) {
		return(genome.insert(position, seq));
	}

    public final SimpleSequence seq;
}
