package santa.simulator.genomes;

import java.util.List;
import java.util.ArrayList;


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

	public static void setDescription(int genomeLength,
	                                  List<Feature> features,
	                                  List<Sequence> sequences) {
	    if (isSet) {
	        throw new RuntimeException("GenomeDescription can only be set once");
	    }

		GenomeDescription.genomeLength = genomeLength;
		GenomeDescription.features = features;

		if (sequences != null && sequences.size() > 0) {
			Sequence firstSequence = sequences.get(0);
		    if (firstSequence.getLength() != GenomeDescription.genomeLength) {
			    throw new IllegalArgumentException("Sequences are not the same length as the genome");
		    }
			GenomeDescription.sequences = new ArrayList<Sequence>(sequences);
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

	public static List<Sequence> getSequences() {
		return sequences;
	}

	public static Sequence getConsensus() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	private static int genomeLength;
	private static List<Feature> features = null;
	private static List<Sequence> sequences = null;

    private static boolean isSet = false;

}
