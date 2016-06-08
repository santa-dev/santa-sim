package santa.simulator.genomes;

import java.util.*;

import santa.simulator.Random;

import org.apache.commons.math3.distribution.BinomialDistribution;


/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: GenomeDescription.java,v 1.4 2006/07/18 14:33:11 kdforc0 Exp $
 */
public final class GenomeDescription {
	// An odd constructor!

	// This constructor is invoked from a static factory method (setDescription)

	private GenomeDescription(int genomeLength, List<Feature> features, List<Sequence> sequences) {
		this.genomeLength = genomeLength;
		Feature genomeFeature = new Feature("genome", Feature.Type.NUCLEOTIDE);
		genomeFeature.addFragment(0, genomeLength);

		this.features = new ArrayList<Feature>();
		this.features.add(genomeFeature);

		for (Feature feature : features)
			if (feature.getNucleotideFinish() >= genomeLength) {
				String msg = String.format("Feature %s exceeds available genome length (%d)", feature.getName(), genomeLength);
				throw new RuntimeException(msg);
			}

		this.features.addAll(features);

		if (sequences != null && sequences.size() > 0) {
			Sequence firstSequence = sequences.get(0);
			if (firstSequence.getLength() != this.genomeLength) {
				throw new IllegalArgumentException("Sequences are not the same length as the genome");
			}
			GenomeDescription.sequences = new ArrayList<Sequence>(sequences);
		}
		assert(this.features != null);
		assert(this.features.size() >= 1);
	}

	/**
	 * Private copy constructor that applies an insertion or deletion.
	 *
	 * Note this private method creates a new GenomeDescriptor instance and
	 * does not consult the instance cache.  It is intended to be used
	 * by routines like `recombine()` which do their own cache management.
	 *
	 **/
	private GenomeDescription(GenomeDescription gd, int position, int count) {
		assert(gd.features != null);
		assert(gd.features.size() >= 1);

		this.features = new ArrayList<Feature>();

		// cannot delete more than we have available.
		// should this throw an exception if |count| > available ?
		if (count < 0) 
			count = -Math.min(-count, gd.genomeLength - position);

		// Copy features from parent, adjusting as necessary for the new indel event.
		for (Feature feature : gd.features) {
			Feature tmp = new Feature(feature, position, count);
			if (tmp.getNucleotideLength() > 0)
				features.add(tmp);
		}

		// Special handling for the genome-spanning feature
		// named 'genome'.
		//
		// The 'genome' feature must be explicitly widened after
		// inserting nucleotides at the extreme ends of the
		// genome. All other features do not change size for
		// insertions to the immediate right of the feature.
		
		this.genomeLength = gd.genomeLength + count;
		Feature f = gd.getFeature("genome");
		if (f.getNucleotideLength() != genomeLength) {
			// create a new genome-spanning feature
			Feature tmp = new Feature(f.getName(), f.getFeatureType());
			tmp.addFragment(0, genomeLength);
			if (features.size() >= 1) {
				// Replace the first feature if already present in the list.
				features.set(0, tmp);
			} else {
				// otherwise create feature named 'genome'
				// The 'genome' feature is always the first feeature in the
				// list
				features.add(tmp);
			}
		}

		assert(features.size() >= 1);
	}




	/**
	 * Public static factory method for constructing a GenomeDescriptor from
	 * an existing instance and applying an insertion or deletion.
	 *
	 * This method may return a cached instance.
	 **/
	static public GenomeDescription applyIndel(GenomeDescription gd, int position, int count) {
		GenomeDescription tmp = new GenomeDescription(gd, position, count);
		GenomeDescription gd_cached = GenomeDescription.cache.get(tmp);
		if (gd_cached != null) {
			return(gd_cached);
		} 
		GenomeDescription.cache.put(tmp, tmp);
		return(tmp);
	}


	/**
	 * Create a new GenomeDescription by recombining fragments of two
	 * other parent GenomeDescriptions.
	 *  
	 * Fragment boundaries are determined by the array of integer
	 * breakpoints supplied as a parameter, and by the boundaries of
	 * the parent objects.  This routine relies up on two primitive
	 * operations defined on GenomeDescriptors; construction of a new
	 * description object after applying an indel, and appending one
	 * description to another.
	 */
	public static GenomeDescription recombine(GenomeDescription[] parents, SortedSet<Integer> breakPoints) {
		/*
		  Shortcut - if both parents are identical, then the recombined hybrid will have the same feature description.
		*/
		if (parents[0].equals(parents[1]))
			return parents[0];
		
		/*
		  The first call to applyIndel() below truncates
		  nucleotides on the left.  The second call truncates on the
		  right.  The result is a fragment of the original
		  GenomeDescription covering just the nucleotides between
		  'lastBreakPoint' and 'nextBreakPoint'.  This is appended to
		  the 'gd_recomb' under construction.  

		 */
		int lastBreakPoint = 0;
		int currentGenome = 0;

		assert(parents[0].genomeLength <= parents[1].genomeLength);
		GenomeDescription gd = parents[currentGenome];
		GenomeDescription gd_recomb = null;
		for (int nextBreakPoint: breakPoints) {
			gd = new GenomeDescription(parents[currentGenome], 0, -lastBreakPoint);
			gd = new GenomeDescription(gd, nextBreakPoint-lastBreakPoint, -(parents[currentGenome].genomeLength - nextBreakPoint));

			if (gd_recomb == null)
				gd_recomb = gd;
			else {
				gd_recomb.append(gd);
			}
			lastBreakPoint = nextBreakPoint;
			currentGenome = 1 - currentGenome;
		}
		if (lastBreakPoint < parents[currentGenome].genomeLength) {
			gd = new GenomeDescription(parents[currentGenome], 0, -lastBreakPoint);

			if (gd_recomb == null)
				gd_recomb = gd;
			else {
				gd_recomb.append(gd);
			}
		}

		GenomeDescription gd_cached = cache.get(gd_recomb);
		if (gd_cached != null) {
			return(gd_cached);
		} 
		cache.put(gd_recomb, gd_recomb);
					
		return(gd_recomb);
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
		result = prime * result + genomeLength;
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
		if (!(obj instanceof GenomeDescription))
			return false;
		GenomeDescription other = (GenomeDescription) obj;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		if (genomeLength != other.genomeLength)
			return false;
		return true;
	}

	/**
	 * Append a genome description to an existing GenomeDescription.
	 *
	 * This method extends this instance by the length of {@code gd},
	 * copying all the feature definitions after adjusting their
	 * coordinates to reflect their new position.  Adjacent like-named
	 * feature definitions are collapsed.
	 **/
	public void append(GenomeDescription gd) {
		int len = this.genomeLength;
		this.genomeLength += gd.genomeLength;

		// shift all the incoming features right by len
		for (Feature feature : gd.features) {
			if (feature.getName().equals("genome"))
				continue;
			Feature tmp = new Feature(feature);
			tmp.shift(len);
			Feature existing = this.getFeature(feature.getName());
			if (existing != null) {
				// collapse identically named features
				existing.merge(tmp);
			} else {
				this.features.add(tmp);
			}
		}
		// Replace the feature named 'genome'.
		Feature f = this.features.get(0);
		assert(f.getName().equals("genome"));
		Feature tmp = new Feature(f.getName(), f.getFeatureType());
		tmp.addFragment(0, this.genomeLength);
		this.features.set(0, tmp);

	}
	
	public static void setHotSpots(List<RecombinationHotSpot> recombinationHotSpots){
		GenomeDescription.recombinationHotSpots = recombinationHotSpots;		
	}

	public static GenomeDescription root = null;

	public static void setDescription(int genomeLength,
	                                  List<Feature> features) {
		setDescription(genomeLength, features, null);
	}

	public static void setDescription(int genomeLength,
	                                  List<Feature> features,
	                                  List<Sequence> sequences) {
		if (root != null) {
			throw new RuntimeException("GenomeDescription can only be set once");
		}

		GenomeDescription.root = new GenomeDescription(genomeLength,features,sequences);
	}
	
	public static boolean isSet() {
		return (root != null);
	}

	public int getGenomeLength() {
		return genomeLength;
	}

	public int getGenomeLength(SequenceAlphabet alphabet) {
		return genomeLength / alphabet.getTokenSize();
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public Feature getFeature(String name) {

		for (Feature f : features) {
			if (f.getName().equals(name)) {
				return f;
			}
		}

		return null;
	}

	/**
	 * For each feature, make two lookup tables (of type int[]):
	 *
	 * 'featureSiteTable' maps from genomic coordinates to feature-relative
	 * coordinates.  That is, featureSiteTable[200] = 0 if the current feature begins
	 * at genomic position 200.  'featureSiteTable' spans the entire genome and holds a
	 * feature-relative position where the feature is defined, and -1 elsewhere.
	 *
	 * 'genomeSiteTable' maps from feature coordinates to
	 * genome coordinates.  That is, genomeSiteTable[0] = 200 for the
	 * feature that begins at genomic position 200. The length of
	 * 'genomeSiteTable' is the number of nucleotides in the feature.
	 *
	 * A 'featureSiteTable' and 'genomeSiteTable' pair are created for
	 * each feature and are stored in HashMaps indexed by the feature
	 * name.
	 */
	private void computeSiteTables() {
		this.featureSiteTables = new HashMap<String, int[]>();
		this.genomeSiteTables = new HashMap<String, int[]>();

		assert(features != null);
		assert(features.size() >= 1);

		for (Feature feature : features) {
			int[] featureSiteTable = new int[genomeLength];
			int[] genomeSiteTable = new int[feature.getNucleotideLength()];

			Arrays.fill(featureSiteTable, -1);
			Arrays.fill(genomeSiteTable, -1);

			int k = 0;
			for (int i = 0; i < feature.getFragmentCount(); i++) {
				int len = feature.getFragmentLength(i);
				int j = feature.getFragmentStart(i);
				while (len-- > 0) {
					featureSiteTable[j] = k;
					genomeSiteTable[k] = j;
					k++; j++;
				}
			}
			featureSiteTables.put(feature.getName(), featureSiteTable);
			genomeSiteTables.put(feature.getName(), genomeSiteTable);
		}
	}

	/**
	 * returns an integer array that maps positions in the genome to positions in a feature.
	 * The returned array will have a length equal to the number of positions in the genome.
	 * Note: a genome may have zero length, in which case an empty array will be returned.
	 * @param feature
	 * @return integer array
	 */
	public int[] getFeatureSiteTable(Feature feature) {
		if (featureSiteTables == null) {
			computeSiteTables();
		}
		return featureSiteTables.get(feature.getName());
	}

	/**
	 * returns an integer array that maps positions in a feature to positions in the genome.
	 * The returned array will have a length equal to the number of positions in the feature.
	 * Note: a feature may have zero length, which case an empty array will be returned!
	 * @param feature
	 * @return integer array
	 */
	public int[] getGenomeSiteTable(Feature feature) {
		if (genomeSiteTables == null) {
			computeSiteTables();
		}
		return genomeSiteTables.get(feature.getName());
	}


	// generate number of substituion mutations across this genome.
    public int binomialDeviate(double mutationRate) {
		// create and cache random distribution if necessary
		if (mutationDist == null) 
			mutationDist = new BinomialDistribution(Random.randomData.getRandomGenerator(), genomeLength, mutationRate);
		int j = mutationDist.sample();
        return j;
    }

	
	public String toString() {
		String str = "";

		int current = 0;
		for (Feature f : features) {
			if (f.getName().equals("genome"))
				continue;
			String c = f.getName().substring(0, 1);
			for (int i = 0; i < f.getFragmentCount(); i++) {
				int start = f.getFragmentStart(i);
				if (start > current) {
					int n = start - current - 1;
					String span = new String(new char[n]).replace("\0", "-");

					str += span;
				}
				int n = f.getFragmentLength(i);
				String span = new String(new char[n]).replace("\0", c);
				str += span;
				current = f.getFragmentFinish(i);
			}
		}
		if (current < genomeLength-1) {
			int n = genomeLength - current - 1;
			String span = new String(new char[n]).replace("\0", "-");
			str += span;
		}
		return str;
	}
	

	public static List<Sequence> getSequences() {
		return sequences;
	}
	
	public static List<RecombinationHotSpot> getHotSpots(){
		return recombinationHotSpots;
	}		

	public static Sequence getConsensus() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	// The list of feature active in this simulation.  Note that the coordinates of features change as the genome evolves in the presence of
	// indels.  The feature coordinates in the list below are presumed to have been updated to account for the current and all previous indels
	// in this lineage.
	private List<Feature> features = null;

	// These site maps are recomputed based on the feature coordinates inherited from the parent description.
	private Map<String, int[]> genomeSiteTables = null;
	private Map<String, int[]> featureSiteTables = null;

	private int genomeLength;

    private BinomialDistribution mutationDist = null;


	// static variables.
	private static List<Sequence> sequences = null;

	private static List<RecombinationHotSpot> recombinationHotSpots = new ArrayList<RecombinationHotSpot>();
	
	private static Map<GenomeDescription, GenomeDescription> cache = new HashMap<GenomeDescription, GenomeDescription>();
}
