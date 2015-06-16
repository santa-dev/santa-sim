/*
 * Deletion.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

import java.util.List;
import java.util.ArrayList;
import org.apache.commons.lang3.Range;

/**
 * @file   Deletion.java
 * @author cswarth
 * @date   Fri Jan  9 14:15:48 2015
 * 
 */

public class Deletion extends Indel {
    public Deletion(int position, int count) {
        this.position = position;
        this.count = count;
    }


	/**
	 * Apply a deletion to an range of genomic positions (presumed to represent a Fragment of a Feature.)

	 * If the deletion begins before the range, the entire range shifts left by the deletion count.
	 * if the deletion overlaps the deletion, only the end-point (righthand side) of the range is shifted left, but the start is not modified.
	 * if the deleteion begins completely to the right of the range, the range isnot modified.
	 *
	 * ranges are inclusive!
	 */
	public Range<Integer> apply(Range<Integer> fragment) {
		if (fragment.isAfter(position+count-1)) {
			fragment =  Range.between(fragment.getMinimum() - count, fragment.getMaximum() - count);

		} else if (fragment.isOverlappedBy(Range.between(position, position+count-1))) {
			int start = Math.min(fragment.getMinimum(), position);
			int end = Math.max(start, fragment.getMaximum() - count);
			fragment = Range.between(start, end);
		} 
		return (fragment);
	}

	public boolean apply(Genome genome) {
		return(genome.delete(position, count));
	}

	/**
	 * create a list of nucleotides changed by this mutation.
	 */
	public List<StateChange> getChanges(Genome genome, int[] featureSiteTable) {
		List<StateChange> scl = new ArrayList<StateChange>();
		int fp;
		
		// insertions affect everything to the right as well
		for (int p = this.position; p < featureSiteTable.length; p++) {
			fp = featureSiteTable[p];
			if (fp >= 0) {
				byte oldState = genome.getNucleotide(p);
				byte newState = genome.getNucleotide(p+this.count);
				scl.add( new StateChange(fp, oldState, newState) );
			}
		}

		return (scl);
	}

    public final int count; // how may to delete
}
