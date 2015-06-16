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
		genomeFeature.addFragment(0, genomeLength - 1);

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
	}


	// Copy constructor.
	
	// Link a new GenomeDescription into the tree of descriptions.  Each node in the tree is associated with a single Indel mutation.  It is
	// the Indel mutation that invalidates genome lengths and site maps, so those are recomputed and cached in each new GenomeDescription
	// object.
	public GenomeDescription(GenomeDescription gd, Mutation indel) {
		this.indel = indel;
		this.parent = gd;


		// Copy features from parent, adjusting as necessary for the new indel event.
		this.features = new ArrayList<Feature>();

		Feature g = gd.getFeature("genome");
		Range<Integer> r = Range.between(g.getFragmentStart(0), g.getFragmentFinish(0));
		r = indel.apply(r);
		genomeLength = r.getMaximum() - r.getMinimum() + 1;
		
		for (Feature feature : gd.features) {
			Feature tmp = new Feature(feature.getName(), feature.getFeatureType());

			for (int i = 0; i < feature.getFragmentCount(); i++) {
				Range<Integer> f = Range.between(feature.getFragmentStart(i), feature.getFragmentFinish(i));
				
				f = indel.apply(f);
				assert(f.getMinimum() >= 0);
				assert(f.getMaximum() >= f.getMinimum());
				assert(f.getMaximum() < genomeLength);
				tmp.addFragment(f.getMinimum(), f.getMaximum());
			}
			this.features.add(tmp);
		}

		assert(getFeature("genome").getNucleotideLength() == genomeLength);
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
	 * 		'featureSiteTable' maps from genomic coordinates to feature-relative coordinates.  That is, featureSiteTable[200] = 0 if the
	 * 		current feature begins at genomic position 0.  'featureSiteTable' spans the entire genome and holds a feature-relative position
	 * 		where the feature is defined, and -1 elsewhere.
	 *
	 *
	 * 		'genomeSiteTable' maps from feature-relative coordinates to genome coordinates.  That is, genomeSiteTable[0] = 200 for the feature that
	 * 		begins at the first position on the genome. The length of 'genomeSiteTable' is the number of nucleotides in the feature. 
	 *
	 * Remember that a 'featureSiteTable' and 'genomeSiteTable' pair are created for each feature.  The maps are stored in the class variables
	 * 'featureSiteTables' and 'genomeSiteTables' HashMaps indexed by the feature object.
	 */
	private void computeSiteTables() {
		this.featureSiteTables = new HashMap<Feature, int[]>();
		this.genomeSiteTables = new HashMap<Feature, int[]>();

		for (Feature feature : features) {
			int[] featureSiteTable = new int[genomeLength];
			for (int i = 0; i < genomeLength; i++) {
				featureSiteTable[i] = -1;
			}
			// Sanity check to make sure AMINO_ACID features have a length that is an integral multiple of 3.
			// Otherwise can possibly throw ArrayIndexOutOfBoundsException in SimpleGenome(BaseGenome).getChanges()
			if (feature.getFeatureType() == Feature.Type.AMINO_ACID) {
				int featureLength = feature.getNucleotideLength();
				if ((featureLength % 3) != 0) {
					throw new IllegalArgumentException("Total length of AminoAcid feature \"" + feature.getName() + "\" must be an integral number of codons long (currently " + String.format("%.1f", featureLength/3.0) + " codons as specified).");
				}
			}
			int[] genomeSiteTable = new int[feature.getNucleotideLength()];
			for (int i = 0; i < genomeSiteTable.length; i++) {
				genomeSiteTable[i] = -1;
			}


			int k = 0;
			for (int i = 0; i < feature.getFragmentCount(); i++) {
				int start = feature.getFragmentStart(i);
				int finish = feature.getFragmentFinish(i);
				if (start < finish) {
					for (int j = start; j <= finish; j++) {
						featureSiteTable[j] = k;
						genomeSiteTable[k] = j;
						k++;
					}
				} else {
					for (int j = finish; j >= start; j--) {
						featureSiteTable[j] = k;
						genomeSiteTable[k] = j;
						k++;
					}
				}
			}
			featureSiteTables.put(feature, featureSiteTable);
			genomeSiteTables.put(feature, genomeSiteTable);
		}
	}

	/**
	 * returns a table that maps the sites in the genome to the
	 * nucleotides sites of the specified feature (nucleotide
	 * sites even if the feature is amino acids).
	 * @param feature
	 * @return an array of integers as long as the genome
	 */
	public int[] getFeatureSiteTable(Feature feature) {
		if (featureSiteTables == null) {
			computeSiteTables();
		}
		return featureSiteTables.get(feature);
	}

	/**
	 * returns a table that maps the nucloetide sites of the specified
	 * feature to the sites of the genome.
	 * @param feature
	 * @return an array of integers as long as the feature in nucleotides
	 */
	public int[] getGenomeSiteTable(Feature feature) {
		if (genomeSiteTables == null) {
			computeSiteTables();
		}
		return genomeSiteTables.get(feature);
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
	private Map<Feature, int[]> genomeSiteTables = null;
	private Map<Feature, int[]> featureSiteTables = null;

	private int genomeLength;


    private BinomialDistribution mutationDist = null;




	
	private static List<Sequence> sequences = null;

	private static List<RecombinationHotSpot> recombinationHotSpots = new ArrayList<RecombinationHotSpot>();
	
}
