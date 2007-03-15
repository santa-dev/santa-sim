package santa.simulator.samplers;

import santa.simulator.*;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Nucleotide;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: AlignmentSampler.java,v 1.6 2006/07/18 07:37:47 kdforc0 Exp $
 */
public class AlignmentSampler implements Sampler {
	public enum Format {
        FASTA,
	    NEXUS,
	    XML
	};

	private final int sampleSize;
    private Format format;
    private String label;
    private String fileName;
    private PrintStream destination;
    private Map<Integer,Integer> schedule;
    private int replicate;
    private boolean consensus;

    /**
     * Construct an alignment sampler
     * @param sampleSize  amount of sequences to sample at regular intervals
     * @param consensus   write the consensus sequence of the sample rather than writing the sample ?
     * @param schedule    amount of sequences to sample at irregular intervals
     * @param format      format
     * @param label       label with possible %g, %s and %t variables
     * @param fileName    name of the file to write the samples
     */
    public AlignmentSampler(int sampleSize, boolean consensus,
                            Map<Integer,Integer> schedule, Format format, String label, String fileName) {
        this.format = format;
        this.fileName = fileName;

        if (label == null) {
            this.label = "virus_%g_%s";
        } else {
            this.label = label;
        }

	    this.sampleSize = sampleSize;
        this.consensus = consensus;
        this.schedule = schedule;
    }

    public void initialize(int replicate) {
        this.replicate = replicate;
        String fName = substituteVariables(fileName, 0, 0);

        try {
            destination = new PrintStream(fName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open file for writing: " + fName);
        }

        if (format == Format.NEXUS) {
            destination.println("#NEXUS");
            destination.println();
            destination.println("BEGIN DATA;");

            int nchar = GenomeDescription.getGenomeLength();
            int samplesize = computeSampleSize();

            destination.println("\tDIMENSIONS NTAX=" + samplesize + " NCHAR=" + nchar + ";");
            destination.println("\tFORMAT DATATYPE=NUCLEOTIDE GAP=-;");

            destination.println("\tMATRIX");
        }
    }

    private String substituteVariables(String name, int generation, int seq) {
        String result = name.replaceAll("%r", String.valueOf(replicate+1));
        result = result.replaceAll("%g", String.valueOf(generation));
        result = result.replaceAll("%s", String.valueOf(seq));
        return result;
    }

    private int computeSampleSize() {
        if (schedule == null)
            return 0; // do not know really, should be samplesize * number of samples
        else {
            int result = 0;

            for (Integer item : schedule.values()) {
                result += item;
            }

            return result;
        }
    }

    public void sample(int generation, Population population) {
        Virus[] sample = getSample(generation, population);

        if (sample != null) {
            if (format == Format.NEXUS) {
                writeNexusFormat(generation, sample);
            } else if (format == Format.XML) {
                writeXMLFormat(generation, sample);
            } else if (format == Format.FASTA) {
                writeFastaFormat(generation, sample);
            }
        }
    }

    protected Virus[] getSample(int generation, Population population) {
        if (schedule == null) {
            Virus[] viruses = population.getCurrentGeneration();
            Object[] tmp = Random.nextSample(Arrays.asList(viruses), sampleSize);
            Virus[] sample = new Virus[tmp.length];
            System.arraycopy(tmp, 0, sample, 0, tmp.length);
            return sample;
        } else {
            if (schedule.containsKey(generation)) {
                int count = schedule.get(generation);
                Virus[] viruses = population.getCurrentGeneration();
                Object[] tmp = Random.nextSample(Arrays.asList(viruses), count);
                Virus[] sample = new Virus[tmp.length];
                System.arraycopy(tmp, 0, sample, 0, tmp.length);
                return sample;
            } else
                return null;
        }
	}

    private void writeNexusFormat(int generation, Virus[] sample) {
        if (consensus) {
            String l = substituteVariables(label, generation, 0);

            destination.print(l + "\t");
            destination.println(computeConsensus(sample));
        } else {
            int i = 1;
            for (Virus virus : sample) {
                String l = substituteVariables(label, generation, i);

                destination.print(l + "\t");
                destination.println(virus.getGenome().getSequence().getNucleotides());

                i++;
            }
        }
    }

    private String computeConsensus(Virus[] sample) {
        String result = "";

        for (int i = 0; i < GenomeDescription.getGenomeLength(); ++i) {
            int freqs[] = new int[4];
            int maxfreq = 0;
            byte maxS = Nucleotide.A;

            for (Virus virus : sample) {
                byte s = virus.getGenome().getNucleotide(i);
                freqs[s]++;
                if (freqs[s] > maxfreq) {
                    maxfreq = freqs[s];
                    maxS = s;
                }
            }

            /* for now just take the maximum one */
            if (maxfreq < sample.length/2)
                result += "N";
            else
                result += Nucleotide.asChar(maxS);
        }

        return result;
    }

    private void writeFastaFormat(int generation, Virus[] sample) {
        if (consensus) {
            String l = substituteVariables(label, generation, 0);

            destination.println(">" + l);
            destination.println(computeConsensus(sample));
        } else {
            int i = 1;

            for (Virus virus : sample) {
                String l = substituteVariables(label, generation, i);

                destination.println(">" + l);
                destination.println(virus.getGenome().getSequence().getNucleotides());

                i++;
            }
        }
    }

    private void writeXMLFormat(int generation, Virus[] sample) {
        destination.println();
        destination.println("<sequences>");
        destination.println("<!-- Generation = " + generation + " -->");
        int i = 1;
        for (Virus virus : sample) {
            String l = substituteVariables(label, generation, i);
            destination.println("\t<sequence label=\"" + l+ "\">");
            destination.println("\t\t" + virus.getGenome().getSequence().getNucleotides());
            destination.println("\t</sequence>");
            i++;
        }
        destination.println("</sequences>");
    }

    public void cleanUp() {
        if (format == Format.NEXUS) {
            destination.println("\t;");
            destination.println("END;");
        }

        destination.close();
        destination = null;
    }
}
