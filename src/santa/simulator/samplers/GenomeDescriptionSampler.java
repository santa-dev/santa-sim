package santa.simulator.samplers;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import santa.simulator.Virus;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.Feature;
import santa.simulator.population.Population;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: AlignmentSampler.java,v 1.6 2006/07/18 07:37:47 kdforc0 Exp $
 * Edited by Abbas Jariani Sep 2013
 */
public class GenomeDescriptionSampler implements Sampler {

    private int sampleSize;
    private String fileName;
    private String label;
    private PrintStream destination;
    private Map<Integer,Integer> schedule;
    private int replicate;

    /**
     * Construct an alignment sampler
     * @param sampleSize  amount of sequences to sample at regular intervals
     * @param schedule    amount of sequences to sample at irregular intervals
     * @param label       label with possible %g, %s and %t variables
     * @param fileName    name of the file to write the samples
     */
    public GenomeDescriptionSampler(int sampleSize, Map<Integer,Integer> schedule, String label, String fileName) {
        this.fileName = fileName;
        this.schedule = schedule;
        this.sampleSize = sampleSize;
        if (label == null) {
            this.label = "virus_%g_%s";
        } else {
            this.label = label;
        }
    }

    public void initialize(int replicate) {
        this.replicate = replicate;
        String fName = substituteVariables(fileName, 0, 0);

        try {
            destination = new PrintStream(fName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open file for writing: " + fName);
        }
    }

    private String substituteVariables(String name, int generation, int seq) {
        String result = name.replaceAll("%r", String.valueOf(replicate+1));
        result = result.replaceAll("%g", String.valueOf(generation));
        result = result.replaceAll("%s", String.valueOf(seq));
        return result;
    }

    public void sample(int generation, Population population) {
        List<GenomeDescription> sample = getSample(generation, population);

        if (sample != null) {
			for (GenomeDescription g: sample) {
				for (Feature f: g.getFeatures()) {
					int n = f.getFragmentCount();
					for (int i = 0; i < n; i++) {
						if (f.getFragmentLength(i) > 0)
							// avoid calling getFragmentFinish() on zero-length fragments.
							destination.format("%d\t%8x\t%s\t%d\t%d\n", generation, g.hashCode(), f.getName(), f.getFragmentStart(i), f.getFragmentFinish(i));
					}
				}
			}
		}
    }

    protected List<GenomeDescription> getSample(int generation, Population population) {
		List<GenomeDescription> sample = null;
		int count = 0;
		if (schedule != null) {
			if (!schedule.containsKey(generation)) {
				count = schedule.get(generation);
			}
		} else {
			count = this.sampleSize;
		}
		
		if (count > 0) {
			sample = new ArrayList<GenomeDescription>();
			List<Virus> viruses = population.getCurrentGeneration();
			for (Virus v: viruses) {
				Genome g = v.getGenome();
				GenomeDescription gd = g.getDescription();
				if (!sample.contains(gd)) {
					sample.add(gd);
					if (--count <= 0)
						break;
				}
			}
		}
		return sample;
    }


    public void cleanUp() {
        destination.close();
        destination = null;
    }
}
