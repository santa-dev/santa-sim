package santa.simulator.samplers;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import santa.simulator.Population;
import santa.simulator.genomes.AminoAcid;
import santa.simulator.genomes.Nucleotide;
import santa.simulator.genomes.SequenceAlphabet;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: AlleleFrequencySampler.java,v 1.2 2006/04/19 09:26:30 kdforc0 Exp $
 */
public class AlleleFrequencySampler implements Sampler {

    private final int site;
    private PrintStream destination;
    private final SequenceAlphabet alphabet;
    private String fileName;

    public AlleleFrequencySampler(int site, SequenceAlphabet alphabet, String fileName) {
        this.site = site;
        this.alphabet = alphabet;
        this.fileName = fileName;
    }

    public void initialize(int replicate) {
        String fname = fileName.replaceAll("%r", String.valueOf(replicate+1));
        try {
            destination = new PrintStream(fname);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open file for writing: " + fname);
        }
        
        destination.print("gen");
        for (int i = 0; i < alphabet.getStateCount(); ++i) {
            if (alphabet == SequenceAlphabet.NUCLEOTIDES) {
                destination.print("\t" + Nucleotide.asChar((byte) i));
            } else if (alphabet == SequenceAlphabet.AMINO_ACIDS) {
                destination.print("\t" + AminoAcid.asChar((byte) i));
            }
        }
        destination.println();
    }

    public void sample(int generation, Population population) {

        destination.print(generation);

        double[] frequencies = population.getAlleleFrequencies(site, alphabet);
        for (int i = 0; i < alphabet.getStateCount(); ++i) {
            destination.print("\t" + frequencies[i]);
        }
        destination.println();
    }

    public void cleanUp() {
        destination.close();
    }
}
