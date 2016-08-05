/*
 * Indel.java
 *
 * (c) 2015 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

import java.util.List;
import java.util.ArrayList;

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

	/**
	 * Return a list of nucleotide changes relative to a particular feature.
	 * Changes induced by mutations are difficult to capture as a list of single-nucleotde changes.
	 * This routine doesn't even try to do that, and always returns an empty list of changes.
	 * Changes to fitness will be captured at a complete fitness recalculation.
	 *
	 * @param genome: the genome object which is changing.
	 * @param featureSiteTable: map from genomic coordinates to feature coordinates.
	 */
	@Override
	public List<StateChange> getChanges(Genome genome, int[] featureSiteTable) {
		// probably time to revisit the idea that indels are just another kind of mutation like substitutions.
		// They share very little infrastructure.

		List<StateChange> scl = new ArrayList<StateChange>();
		return (scl);
	}


	public Indel parent;
    public int generation;
}
