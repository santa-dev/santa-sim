package santa.simulator.genomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;

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
	 * @param position: non-negative position where indel would begin
	 * @param count: positive/negative count of positions to be inserted/deleted.
	 */
	public Feature(Feature f, int position, int delta) {
		this.name = f.name;
		this.featureType = f.featureType;
		for (Fragment fr: f.fragments) {
			Fragment tmp = new Fragment(fr, position, delta);
			if (tmp.getLength() > 0)
				fragments.add(tmp);
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((featureType == null) ? 0 : featureType.hashCode());
		result = prime * result
			+ ((fragments == null) ? 0 : fragments.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Feature))
			return false;
		Feature other = (Feature) obj;
		if (featureType != other.featureType)
			return false;
		if (fragments == null) {
			if (other.fragments != null)
				return false;
		} else if (!fragments.equals(other.fragments))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
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

	public void merge(Feature f) {
		// expect to only merge features of identical name
		assert(this.name.equals(f.name));
		fragments.addAll(f.fragments);

		// sort in order of starting position (see Fragment.compareTo())
		Collections.sort(fragments);
		
		// iterate through the list, merging fragments as necessary
		Fragment previous = null;
		for (Iterator<Fragment> iter = fragments.listIterator(); iter.hasNext(); ) {
			Fragment next = iter.next();
			if (previous != null && (previous.overlaps(next) || previous.adjacent(next))) {
				previous.merge(next);
				iter.remove();
				continue;
			}
			previous = next;
		}
	}
	
	public void shift(int howmuch) {
		for (Fragment fragment : fragments) 
			fragment.shift(howmuch);
	}
	
	private final List<Fragment> fragments = new ArrayList<Fragment>();


	private final String name;
	private final Type featureType;
}
