/*
 * Deletion.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

/**
 * @file   Deletion.java
 * @author cswarth
 * @date   Fri Jan  9 14:15:48 2015
 * 
 */

public class Deletion extends Mutation {
    public Deletion(int position, int count) {
        this.position = position;
        this.count = count;
    }

	public boolean apply(Genome genome) {
		return(genome.delete(position, count));
	}


    public final int count; // how may to delete
}
