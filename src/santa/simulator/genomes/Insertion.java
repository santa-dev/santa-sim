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

	/**
	 * Get a list of nucleotide changes relative to a particular feature.
	 *
	 * Each change element will have a feature-relative position, an old nucleotide state, and a new nucleotide state.   
	 * Insertion is the creation of new nucleotides that have no ancestor so the old nucleotide value is always '-'.
	 *
	 * 'featureSiteTable' maps from genomic coordinates to feature-relative
	 * coordinates.  That is, featureSiteTable[200] = 0 if the feature begins
	 * at genomic position 0.  'featureSiteTable' spans the entire genome and holds a
	 * feature-relative position where the feature is defined, and -1 elsewhere.

	 * @param genome: the genome object which is changing.
	 * @param featureSiteTable: map from genomeic coordinates to feature coordinates.
	 */
	public List<StateChange> getChanges(Genome genome, int[] featureSiteTable) {
		List<StateChange> scl = new ArrayList<StateChange>();
		int fp;

		assert(genome.getLength() == featureSiteTable.length);
		
		int p = this.position;
		for (int i = 0; i < seq.getLength() && p < featureSiteTable.length; i++) {
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
