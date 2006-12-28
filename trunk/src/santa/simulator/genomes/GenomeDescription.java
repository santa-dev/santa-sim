package santa.simulator.genomes;


/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: GenomeDescription.java,v 1.4 2006/07/18 14:33:11 kdforc0 Exp $
 */
public final class GenomeDescription {

    private GenomeDescription() { /* private to prohibit instances */ }

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

    private static int genomeLength;

    private static boolean isSet = false;

}
