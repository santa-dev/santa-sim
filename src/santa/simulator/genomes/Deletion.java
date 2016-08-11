/*
 * Deletion.java
 *
 * (c) 2002-2015 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

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

	 * If the deletion begins and ends to the left of the range, the entire range shifts left by the deletion count.
	 * if the deletion overlaps the left-end of range,  the start and end of the range may be shifted-left by different amounts.
	 * if the deletion is completely contained with the range, only the end-point (righthand side) of the range is shifted left, but the start is not modified.
	 * if the deletion overlaps the left-end of range,  only the end-point (righthand side) of the range is shifted left, but the start is not modified.
	 * if the deletion is entirely to the right of the range, the range is unaffected.
	 *
	 * ranges are inclusive!
	 */
	public Range<Integer> apply(Range<Integer> fragment) {

		Range<Integer> del = Range.between(position, position+count-1);
		
		if (fragment.isAfterRange(del))
			// deletion is fully left of fragment
			// fragment moves left
			fragment = Range.between(fragment.getMinimum() - count, fragment.getMaximum() - count);
		else if (del.getMinimum() <= fragment.getMinimum()) {
			if (del.getMaximum() <= fragment.getMaximum()) 
				// deletion overlaps fragment on the left
				fragment = Range.between(del.getMinimum(), fragment.getMaximum() - count);
			else
				// deletion completely covers fragment - fragment gets wiped out.
				fragment = null;
		} else if (del.getMinimum() <= fragment.getMaximum()) {
			if (del.getMaximum() <  fragment.getMaximum())
				// fragment completely covers deletion
				fragment = Range.between(fragment.getMinimum(), fragment.getMaximum() - count);
			else
				// deletion overlaps fragment on the right
				fragment = Range.between(fragment.getMinimum(), del.getMinimum()-1);
		}
		/// otherwise deletion is fully to the right of fragment and fragment is unaffected.

		return (fragment);
	}

	public boolean apply(Genome genome) {
		return(genome.delete(position, count));
	}

	public int length() {
		return -count;
	}

    public final int count; // how many to delete
}
