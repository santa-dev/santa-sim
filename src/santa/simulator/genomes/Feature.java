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

		for (Fragment fragment : fragments) {
			length += Math.abs(fragment.getFinish() - fragment.getStart()) + 1;
		}

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
			this.finish = finish;
		}


		/** 
		 * copy constructor.   Clone a fragment and apply an indel.
		 * It is possible the indel will have no effect on the fragment in which
		 * case the cloned fragment will have identical values as the original.
		 * It is also possible that the indel will completely wipe out
		 * the fragment (reducing it to one nucleotide or less).
		 *
		 * @param: f: fragment to be cloned.
		 * @param position: non-negative position where indel whould begin
		 * @param count: positive/negative count of positions to be inserted/deleted.
		 */
		public Fragment(Fragment f, int position, int count) {
			if (position < f.start) {
				// delete: count < 0
				// insert: count > 0
				this.finish = Math.max(position, f.finish + count);
				if (count < 0) {
					// delete: count < 0
					count = -Math.min(-count, f.start-position);
				} 
				this.start = f.start + count;
			}
			else if (position <= f.finish) {
				this.start = f.start;
				if (count < 0) {
					// delete: count < 0
					count = -Math.min(-count, f.finish-position+1);
				}
				this.finish = f.finish + count;
			} else {
				this.start = f.start;
				this.finish = f.finish;
			}
		}

		public int getStart() {
			return start;
		}

		public int getFinish() {
			return finish;
		}

		
		private final int start;
		private final int finish;
	}

	private final String name;
	private final Type featureType;
}
