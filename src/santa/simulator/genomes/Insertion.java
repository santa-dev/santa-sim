/*
 * Insertion.java
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
		int len = seq.getLength();

		if (fragment.isAfter(position)) {
			fragment =  Range.between(fragment.getMinimum() + len, fragment.getMaximum() + len);
		} else if (fragment.contains(position)) {
			fragment =  Range.between(fragment.getMinimum(), fragment.getMaximum() + len);
		}
		return (fragment);
	}

	public boolean apply(Genome genome) {
		return(genome.insert(position, seq));
	}


	/**
	 * create a list of nucleotides changed by this mutation.
	 */
	public List<StateChange> getChanges(Genome genome, int[] featureSiteTable) {
		List<StateChange> scl = new ArrayList<StateChange>();
		int fp;
		
		int p = this.position;
		for (int i = 0; i < seq.getLength(); i++) {
			byte oldState = genome.getNucleotide(p);
			fp = featureSiteTable[p];
			if (fp >= 0) {
				scl.add( new StateChange(fp, oldState, seq.getNucleotide(i)) );
			}
			p++;
		}

		if ( !scl.isEmpty() ) {

			// insertions affect everything to the right as well
			for (; p < featureSiteTable.length; p++) {
				fp = featureSiteTable[p];
				if (fp >= 0) {
					byte oldState = genome.getNucleotide(p);
					byte newState = genome.getNucleotide(p-seq.getLength());
					scl.add( new StateChange(fp, oldState, newState) );
				}
			}
		}

		return (scl);
	}

    public final SimpleSequence seq;
}
