package santa.simulator.genomes;

import java.util.*;
import org.apache.commons.lang3.Range;
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


	// Copy constructor.
	
	// Link a new GenomeDescription into the tree of descriptions.  Each node in the tree is associated with a single Indel mutation.  It is
	// the Indel mutation that invalidates genome lengths and site maps, so those are recomputed and cached in each new GenomeDescription
	// object.
	public GenomeDescription(GenomeDescription gd, int position, int count) {
		assert(gd.features != null);
		assert(gd.features.size() >= 1);

		this.parent = gd;

		// Copy features from parent, adjusting as necessary for the new indel event.
		this.features = new ArrayList<Feature>();
		if (count < 0) 
			count = -Math.min(-count, gd.genomeLength - position);
		this.genomeLength = gd.genomeLength + count;
		for (Feature feature : gd.features) {
			Feature tmp = new Feature(feature, position, count);
			this.features.add(tmp);
		}

		Feature g = gd.getFeature("genome");
		Feature tmp = new Feature(g.getName(), g.getFeatureType());
		tmp.addFragment(0, this.genomeLength);
		this.features.set(0, tmp);
		assert(this.getFeature("genome").getNucleotideLength() == this.genomeLength);

		assert(this.features != null);
		assert(this.features.size() >= 1);

		
	}

	
	public static void setDescription(int genomeLength,
	                                  List<Feature> features) {
		setDescription(genomeLength, features, null);
	}

	public static void setHotSpots(List<RecombinationHotSpot> recombinationHotSpots){
		GenomeDescription.recombinationHotSpots = recombinationHotSpots;		
	}

	public static GenomeDescription root = null;

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
	 * at genomic position 0.  'featureSiteTable' spans the entire genome and holds a
	 * feature-relative position where the feature is defined, and -1 elsewhere.
	 *
	 * 'genomeSiteTable' maps from feature-relative coordinates to genome
	 * coordinates.  That is, genomeSiteTable[0] = 200 for the feature that begins at
	 * the first position on the genome. The length of 'genomeSiteTable' is the
	 * number of nucleotides in the feature.
	 *
	 * A 'featureSiteTable' and 'genomeSiteTable' pair are created for each feature.
	 * The maps are stored in 'featureSiteTables' and 'genomeSiteTables' HashMaps
	 * indexed by the feature object.   
	 *
	 * (why not index by the feature name?  what is the advantage to using an object
	 * as the index?  now that we can have multiple genomedescription instances, each with
	 * their own featureSiteTables, it is probably better to index by feature name
	 * instead of by object.)
	 */
	private void computeSiteTables() {
		this.featureSiteTables = new HashMap<String, int[]>();
		this.genomeSiteTables = new HashMap<String, int[]>();

		assert(features != null);
		assert(features.size() >= 1);

		for (Feature feature : features) {
			int[] featureSiteTable = new int[genomeLength];
			int[] genomeSiteTable = new int[feature.getNucleotideLength()];

			System.out.println("feature "+feature.getName()+" NucleotideLength="+feature.getNucleotideLength());


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
			mutationDist = new BinomialDistribution(genomeLength, mutationRate);
		int j = mutationDist.sample();
        return j;
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

	// Each GenomeDescription is associated with a single indel mutation.  Indel events cause a genome description to become stale and need
	// recomputation. GenomeDescriptions are linked together in a tree that captures a phylogeny of indel mutations.  Each node in the tree
	// points to its parent.  The lineage may be recaputilated by following parent links up to the root.  Traversing the tree from the root
	// toward the leaves is not supported.
	private Mutation indel = null;
	private GenomeDescription parent = null;

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
	
}
