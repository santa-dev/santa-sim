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
	public Feature(Feature f, int position, int delta) {
		this.name = f.name;
		this.featureType = f.featureType;
		for (Fragment fr: f.fragments) {
			fragments.add(new Fragment(fr, position, delta));
		}
	}

	// copy constructor
	public Feature(Feature f) {
		this.name = f.name;
		this.featureType = f.featureType;
		for (Fragment fr: f.fragments) {
			fragments.add(new Fragment(fr));
		}
	}

	public void addFragment(int start, int count) {
		fragments.add(new Fragment(start, count));
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
			length = length + fragment.getLength();
		return length;
	}

	public int getNucleotideFinish() {
		int finish = 0;
		for (Fragment fragment : fragments) 
			finish = Math.max(finish, fragment.getFinish());
		return finish;
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

	public int getFragmentLength(int index) {
		return fragments.get(index).getLength();
	}


	public void shift(int howmuch) {
		for (Fragment fragment : fragments) 
			fragment.shift(howmuch);
	}
	
	private final List<Fragment> fragments = new ArrayList<Fragment>();

	private class Fragment {
		public Fragment(int start, int count) {
			assert(count >= 0);
			this.start = start;
			this.count = count;
		}


		/** 
		 * copy constructor.   Clone a fragment and apply an indel.
		 * It is possible the indel will have no effect on the fragment in which
		 * case the cloned fragment will have identical values as the original.
		 * It is also possible that the indel will completely wipe out
		 * the fragment (reducing it's length to zero).
		 *
		 * Note: A negative delta indicates deletion from the current position moving right.  It DOES NOT mean to remove bases to the left of current position.
		 * 
		 * @param: f: fragment to be cloned.
		 * @param position: non-negative position where indel would begin
		 * @param delta: positive/negative count of positions to be inserted/deleted.
		 */
		public Fragment(Fragment f, int position, int delta) {
			if (position < f.start) {
				// delete: delta < 0
				// insert: delta > 0
				if (delta < 0) {
					// delete: delta < 0
					int shift = Math.min(-delta, f.start-position);
					int shrink = Math.min(-delta - shift, f.count);
					this.start = f.start - shift;
					this.count = f.count - shrink;
				} else {	
					this.start = f.start + delta;
					this.count = f.count;
				}
			}
			else if (f.count > 0 && position <= f.getFinish()) {
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
			assert(this.start >= 0);
			assert(this.count >= 0);

		}

		// copy constructor
		public Fragment(Fragment f) {
			this.start = f.start;
			this.count = f.count;

		}

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

		public void shift(int howmuch) {
			this.start += howmuch;
		}
		
		private int start;
		private int count;
	}

	private final String name;
	private final Type featureType;
}
