/*
 * Indel.java
 *
 * (c) 2015 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

/**
 * @file   Indel.java
 * @author cswarth
 * @date   Thu Jun 11 16:12:43 PDT 2015
 * 
 * 
 */


/**
 * The idea is to have indels linked together into a tree which can be
 * used to reconstruct the entire indel history of a lineage.  It
 * would be nice to use the Lineage class for this, but that is not a
 * good fit because lineages are managed only when the user is
 * sampling phylogenetic trees.
 */

public class Indel extends Mutation {
	Indel() {
		// Empty
	}
	
    public int getGeneration() {
        return generation;
    }

    public Indel getParent() {
        return parent;
    }

	public Indel parent;
    public int generation;
}
