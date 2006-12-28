package santa.simulator.samplers;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import santa.simulator.Population;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: AlleleFrequencySampler.java,v 1.2 2006/04/19 09:26:30 kdforc0 Exp $
 */
public class AlleleFrequencySampler implements Sampler {

    private final int site;
    private PrintStream destination;
    private final SequenceType type;
    private String fileName;

    public AlleleFrequencySampler(int site, SequenceType type, String fileName) {
        this.site = site;
        this.type = type;
        this.fileName = fileName;
    }

    public void initialize(int replicate) {
        String fname = fileName.replaceAll("%r", String.valueOf(replicate));
        try {
            destination = new PrintStream(fname);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open file for writing: " + fname);
        }

        destination.print("generation");
        for (State state : type.getCanonicalStates()) {
            destination.print("\t"+state.getCode());
        }
        destination.println();
    }

    public void sample(int generation, Population population) {

        destination.print(generation);

        double[] frequencies = population.getAlleleFrequencies(site);
        for (int i = 0; i < type.getCanonicalStateCount(); i++) {
            destination.print("\t" + frequencies[i]);
        }
        destination.println();
    }

    public void cleanUp() {
        destination.close();
    }
}
