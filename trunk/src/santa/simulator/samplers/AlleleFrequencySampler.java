package santa.simulator.samplers;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Set;

import santa.simulator.genomes.AminoAcid;
import santa.simulator.genomes.Nucleotide;
import santa.simulator.genomes.SequenceAlphabet;
import santa.simulator.genomes.Feature;
import santa.simulator.population.Population;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: AlleleFrequencySampler.java,v 1.2 2006/04/19 09:26:30 kdforc0 Exp $
 */
public class AlleleFrequencySampler implements Sampler {

    private final Feature feature;
    private final Set<Integer> sites;
    private PrintStream destination;
    private String fileName;

    public AlleleFrequencySampler(Feature feature, Set<Integer> sites, String fileName) {
        this.feature = feature;
        this.sites = sites;
        this.fileName = fileName;
    }

    public void initialize(int replicate) {
        SequenceAlphabet alphabet = feature.getAlphabet();

        String fname = fileName.replaceAll("%r", String.valueOf(replicate+1));
        try {
            destination = new PrintStream(fname);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open file for writing: " + fname);
        }

        destination.print("gen");
        for (int s:sites) {
            for (int i = 0; i < alphabet.getStateCount(); ++i) {
                if (alphabet == SequenceAlphabet.NUCLEOTIDES) {
                    destination.print("\t" + Integer.toString(s + 1) + Nucleotide.asChar((byte) i));
                } else if (alphabet == SequenceAlphabet.AMINO_ACIDS) {
                    destination.print("\t" + Integer.toString(s + 1) + AminoAcid.asChar((byte) i));
                }
            }
        }
        destination.println();
    }

    public void sample(int generation, Population population) {

        destination.print(generation);

        double[][] frequencies = population.getAlleleFrequencies(feature, sites);

        for (int i = 0; i < frequencies.length; i++) {
            for (int j = 0; j < frequencies[i].length; j++) {
                destination.print("\t" + frequencies[i][j]);
            }
        }

        destination.println();
    }

    public void cleanUp() {
        destination.close();
    }
}
