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

	public static void setDescription(List<Sequence> sequences) {
		if (sequences == null || sequences.size() == 0) {
		    throw new RuntimeException("GenomeDescription requires some sequences to initialize");
		}

	    if (isSet) {
	        throw new RuntimeException("GenomeDescription can only be set once");
	    }


		Sequence firstSequence = sequences.get(0);
	    GenomeDescription.genomeLength = firstSequence.getLength();
		GenomeDescription.sequences = new ArrayList<Sequence>(sequences);

	    isSet = true;
	}

    public static void setDescription(int genomeLength) {
        if (isSet) {
            throw new RuntimeException("GenomeDescription can only be set once");
        }

        GenomeDescription.genomeLength = genomeLength;

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

	public static List<Sequence> getSequences() {
		return sequences;
	}

    private static int genomeLength;
	private static List<Sequence> sequences = null;

    private static boolean isSet = false;

}
