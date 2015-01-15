package santa.simulator.genomes;

import java.util.*;


/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: GenomeDescription.java,v 1.4 2006/07/18 14:33:11 kdforc0 Exp $
 */
public final class GenomeDescription {

	private GenomeDescription() { /* private to prohibit instances */ }

	public static void setDescription(int genomeLength,
	                                  List<Feature> features) {
		setDescription(genomeLength, features, null);
	}

	public static void setHotSpots(List<RecombinationHotSpot> recombinationHotSpots){
		GenomeDescription.recombinationHotSpots = recombinationHotSpots;		
	}
	public static void setDescription(int genomeLength,
	                                  List<Feature> features,
	                                  List<Sequence> sequences) {
		if (isSet) {
			throw new RuntimeException("GenomeDescription can only be set once");
		}

		GenomeDescription.genomeLength = genomeLength;
		Feature genomeFeature = new Feature("genome", Feature.Type.NUCLEOTIDE);
		genomeFeature.addFragment(0, genomeLength - 1);
		GenomeDescription.features.add(genomeFeature);
		GenomeDescription.features.addAll(features);

		if (sequences != null && sequences.size() > 0) {
			Sequence firstSequence = sequences.get(0);
			if (firstSequence.getLength() != GenomeDescription.genomeLength) {
				throw new IllegalArgumentException("Sequences are not the same length as the genome");
			}
			GenomeDescription.sequences = new ArrayList<Sequence>(sequences);
		}

		for (Feature feature : features) {
			int[] featureSiteTable = new int[genomeLength];
			for (int i = 0; i < genomeLength; i++) {
				featureSiteTable[i] = -1;
			}
			if (feature.getFeatureType() == Feature.Type.AMINO_ACID) {
				int featureLength = feature.getNucleotideLength();
				if ((featureLength % 3) != 0) {
					// to avoid an ArrayIndexOutOfBoundsException
					// in SimpleGenome(BaseGenome).getChanges(), ensure that the total length
					// of AminoAcid features is an even multiple of 3 nucleotides.
					throw new IllegalArgumentException("Total length of AminoAcid feature \"" + feature.getName() + "\" must be an even number of codons long (currently " + String.format("%.1f", featureLength/3.0) + " codons as specified).");
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

		isSet = true;
	}

	public static boolean isSet() {
		return isSet;
	}

	public static int getGenomeLength() {
		return genomeLength;
	}

	public static int getGenomeLength(SequenceAlphabet alphabet) {
		return genomeLength / alphabet.getTokenSize();
	}

	public static List<Feature> getFeatures() {
		return features;
	}

	public static Feature getFeature(String name) {

		for (Feature f : features) {
			if (f.getName().equals(name)) {
				return f;
			}
		}

		return null;
	}

	/**
	 * returns a table that maps the sites in the genome to the
	 * nucleotides sites of the specified feature (nucleotide
	 * sites even if the feature is amino acids).
	 * @param feature
	 * @return an array of integers as long as the genome
	 */
	public static int[] getFeatureSiteTable(Feature feature) {
		return featureSiteTables.get(feature);
	}

	/**
	 * returns a table that maps the nucloetide sites of the specified
	 * feature to the sites of the genome.
	 * @param feature
	 * @return an array of integers as long as the feature in nucleotides
	 */
	public static int[] getGenomeSiteTable(Feature feature) {
		return genomeSiteTables.get(feature);
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

	private static final Map<Feature, int[]> genomeSiteTables = new HashMap<Feature, int[]>();
	private static final Map<Feature, int[]> featureSiteTables = new HashMap<Feature, int[]>();

	private static int genomeLength;
	private static final List<Feature> features = new ArrayList<Feature>();
	private static List<Sequence> sequences = null;

	private static boolean isSet = false;
	private static List<RecombinationHotSpot> recombinationHotSpots = new ArrayList<RecombinationHotSpot>();
	
}
