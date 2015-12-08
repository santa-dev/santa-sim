package santa.simulator.genomes;

import java.util.*;
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
	 * Copy constructor.
	 *
	 * Copy an existing GenomeDescription, inserting or deleting positions as indicated.
	 * Adjust features as necessary to accommodate indel.
	 *
	 * The copied GenomeDescription will be linked to the object from
	 * which it is copied from so all GenomeDescription objects form a
	 * tree.  The history of a genome can potentially be reconstructed
	 * by following links backward in the tree of GenomeDescription
	 * objects.
	 **/
	public GenomeDescription(GenomeDescription gd, int position, int count) {
		assert(gd.features != null);
		assert(gd.features.size() >= 1);

		this.parent = gd;

		// Copy features from parent, adjusting as necessary for the new indel event.
		this.features = new ArrayList<Feature>();

		// cannot delete more than we have available.
		// should this throw an exception if |count| > available ?
		if (count < 0) 
			count = -Math.min(-count, gd.genomeLength - position);

		for (Feature feature : gd.features) {
			Feature tmp = new Feature(feature, position, count);
			if (tmp.getNucleotideLength() > 0)
				this.features.add(tmp);
		}

		this.genomeLength = gd.genomeLength + count;
		Feature f = gd.getFeature("genome");
		if (f.getNucleotideLength() != this.genomeLength) {
			/** Replace the feature named 'genome'.
			 *
			 *  Insertions at the immediate left and right of a feature do
			 *	not change the size of that feature.  That rule works well
			 *	for all features except the genome-spanning feature named
			 *	'genome'.  The 'genome' feature must be explicitly widened after
			 *	inserting nucleotides at the extreme ends of the genome. 
			 **/
			Feature tmp = new Feature(f.getName(), f.getFeatureType());
			tmp.addFragment(0, this.genomeLength);
			this.features.set(0, tmp);
		}
		assert(this.getFeature("genome").getNucleotideLength() == this.genomeLength);

		assert(this.features != null);
		assert(this.features.size() >= 1);
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
	public static GenomeDescription recombine(GenomeDescription[] parents, int[] breakPoints) {
		/*
		  The first call to GenomeDescription() below truncates
		  nucleotides on the left.  The second call truncates on the
		  right.  The result is a fragment of the original
		  GenomeDescription covering just the nucleotides between
		  'lastBreakPoint' and 'nextBreakPoint'.  This is appended to
		  the 'gd_recomb' under construction.

		 */
		assert(parents[0].genomeLength <= parents[1].genomeLength);
			
		int lastBreakPoint = 0;
		int currentGenome = 0;
		
		GenomeDescription gd = parents[currentGenome];
		GenomeDescription gd_recomb = null;
		for (int i = 0; i < breakPoints.length; i++) {
			int nextBreakPoint = breakPoints[i];
			gd = new GenomeDescription(gd, 0, -lastBreakPoint);
			gd = new GenomeDescription(gd, nextBreakPoint-lastBreakPoint, -(gd.genomeLength - nextBreakPoint));
			if (gd_recomb == null)
				gd_recomb = gd;
			else
				gd_recomb.append(gd);
			lastBreakPoint = nextBreakPoint;
			currentGenome = 1 - currentGenome;
			gd = parents[currentGenome];
		}
		if (lastBreakPoint < gd.genomeLength) {
			int nextBreakPoint = gd.genomeLength;
			gd = new GenomeDescription(gd, 0, -lastBreakPoint);
			if (gd_recomb == null)
				gd_recomb = gd;
			else
				gd_recomb.append(gd);
		}

		return(gd_recomb);
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
			Feature tmp = new Feature(feature);
			tmp.shift(len);
			this.features.add(tmp);
		}

		// collapse adjacent like-named feature definitions.
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
	 * at genomic position 200.  'featureSiteTable' spans the entire genome and holds a
	 * feature-relative position where the feature is defined, and -1 elsewhere.
	 *
	 * 'genomeSiteTable' maps from feature-relative coordinates to
	 * genome coordinates.  That is, genomeSiteTable[0] = 200 for the
	 * feature that begins at genomic position 200. The length of
	 * 'genomeSiteTable' is the number of nucleotides in the feature.
	 *
	 * A 'featureSiteTable' and 'genomeSiteTable' pair are created for
	 * each feature and are stored HashMaps indexed by the feature
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

	/**
	 * At one time each GenomeDescription was associated with a single
	 * indel mutation.  Now that GenomeDescriptions are also created
	 * by recombination events, this no longer hold true.  See if
	 * there is any need to keep these indel mutations around....
	 **/
	private Mutation indel = null;

	/**
	 * GenomeDescriptions are linked together in a tree that captures
	 * a phylogeny of indel mutations.  Each node in the tree points
	 * to its parent.  The lineage may be recapitulated by following
	 * parent links up to the root.  Traversing the tree from the root
	 * toward the leaves is not supported.
	 *
	 * NOTE: this is not strictly true.  Recombination should lead to
	 * non-tree-like structures, but only a single parental lineage is
	 * currently captured.  if this reference isn't being used, it
	 * should be removed.
	**/
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
