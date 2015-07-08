package santa.simulator.genomes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public final class Feature {
	public enum Type {
		NUCLEOTIDE,
		AMINO_ACID
	};

	public Feature(String name, Type featureType) {
		this.name = name;
		this.featureType = featureType;
	}

	/** 
	 * copy constructor.   Clone a feature and apply an indel.
	 * It is possible the indel will have no effect on the feature in which
	 * case the cloned feature will have identical values as the original.
	 *
	 * @param: f: fragment to be cloned.
	 * @param position: non-negative position where indel whould begin
	 * @param count: positive/negative count of positions to be inserted/deleted.
	 */
	public Feature(Feature f, int position, int count) {
		this.name = f.name;
		this.featureType = f.featureType;
		for (Fragment fr: f.fragments) {
			fragments.add(new Fragment(fr, position, count));
		}
	}

	public void addFragment(int start, int finish) {
		fragments.add(new Fragment(start, finish));
	}

	public String getName() {
		return name;
	}

	public Type getFeatureType() {
		return featureType;
	}

	public SequenceAlphabet getAlphabet() {
		if (featureType == Type.AMINO_ACID) {
			return SequenceAlphabet.AMINO_ACIDS;
		}
		return SequenceAlphabet.NUCLEOTIDES;
	}

	public int getLength() {
		int length = getNucleotideLength();

		if (featureType == Type.AMINO_ACID) {
			length /= 3;
		}

		return length;
	}

	public int getNucleotideLength() {
		int length = 0;
		for (Fragment fragment : fragments) 
			length += fragment.getLength();
		return length;
	}
	
	public int getFragmentCount() {
		return fragments.size();
	}

	public int getFragmentStart(int index) {
		return fragments.get(index).getStart();
	}

	public int getFragmentFinish(int index) {
		return fragments.get(index).getFinish();
	}

	private final List<Fragment> fragments = new ArrayList<Fragment>();

	private class Fragment {
		public Fragment(int start, int finish) {
			this.start = start;
			this.count = finish - start + 1;
			assert(this.count > 0);
		}


		/** 
		 * copy constructor.   Clone a fragment and apply an indel.
		 * It is possible the indel will have no effect on the fragment in which
		 * case the cloned fragment will have identical values as the original.
		 * It is also possible that the indel will completely wipe out
		 * the fragment (reducing it's lemgth to zero).
		 *
		 * Note: A negative delta indicates deletion from the current position moving right.  It DOES NOT mean to remove bases to the left of current position.
		 * 
		 * @param: f: fragment to be cloned.
		 * @param position: non-negative position where indel whould begin
		 * @param delta: positive/negative count of positions to be inserted/deleted.
		 */
		public Fragment(Fragment f, int position, int delta) {
			if (position < f.start) {
				// delete: delta < 0
				// insert: delta > 0
				if (delta < 0) {
					// delete: delta < 0
					int shift = Math.min(-delta, f.start-position);
					this.start = f.start - shift;
					this.count = f.count + (delta+shift);
				} else {	
					this.start = f.start + delta;
					this.count = f.count;
				}
			}
			else if (position <= f.getFinish()) {
				this.start = f.start;
				if (delta < 0) {
					// delete: delta < 0
					delta = -Math.min(-delta, f.count);
				}
				this.count = f.count + delta;
			} else {
				this.start = f.start;
				this.count = f.count;
			}
		}

		// public Fragment(Fragment f, int position, int delta) {
		// 	if (position < f.start) {
		// 		// delete: delta < 0
		// 		// insert: delta > 0
		// 		this.finish = Math.max(position, f.finish + delta);
		// 		if (delta < 0) {
		// 			// delete: delta < 0
		// 			delta = -Math.min(-delta, f.start-position);
		// 		} 
		// 		this.start = f.start + delta;
		// 	}
		// 	else if (position <= f.finish) {
		// 		this.start = f.start;
		// 		if (delta < 0) {
		// 			// delete: delta < 0
		// 			delta = -Math.min(-delta, f.finish-position+1);
		// 		}
		// 		this.finish = f.finish + delta;
		// 	} else {
		// 		this.start = f.start;
		// 		this.finish = f.finish;
		// 	}
		// }

		public int getStart() {
			return start;
		}

		public int getFinish() {
			if (count <= 0) 
				throw new RuntimeException("getFinish() is meaningless on zero-length fragments");

			return start + count - 1;
		}

		public int getLength() {
			return count;
		}

		
		private final int start;
		private final int count;
	}

	private final String name;
	private final Type featureType;
}
