
/*
 * Insertion.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

import org.apache.commons.lang3.Range;


/**
 * @file   Insertion.java
 * @author cswarth
 * @date   Fri Jan  9 14:15:48 2015
 * 
 * 
 */

public class Insertion extends Indel {
    public Insertion(int position, SimpleSequence insert) {
        this.position = position;
        this.seq = insert;
    }

	public Range<Integer> apply(Range<Integer> fragment) {
		int count = seq.getLength();
		Range<Integer> ins = Range.between(position, position+count-1);


		if (ins.getMinimum() <= fragment.getMinimum()) 
			// insert is fully left of fragment
			// fragment moves right
			fragment = Range.between(fragment.getMinimum() + count, fragment.getMaximum() + count);
		else if (ins.getMinimum() <= fragment.getMaximum()) {
			// insert occurs inside feature
			fragment = Range.between(fragment.getMinimum(), fragment.getMaximum() + count);
		}
		/// otherwise insertion is fully to the right of fragment and fragment is unaffected.
		return (fragment);
	}

	public boolean apply(Genome genome) {
		return(genome.insert(position, seq));
	}

	public int length() {
		return seq.getLength();
	}

    public final SimpleSequence seq;
}
