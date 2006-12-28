package santa.simulator.samplers;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import santa.simulator.Population;

public class StatisticsSampler implements Sampler {
	private PrintStream destination;
    private String filename;

	public StatisticsSampler(String filename) {
        this.filename = filename;
	}

	public void initialize(int replicate) {
        String fname = filename.replaceAll("%r", String.valueOf(replicate));
        try {
            destination = new PrintStream(fname);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open file for writing: " + fname);
        }

        destination.println("generation\tmean_diversity\tmax_diversity\tmin_fitness\tmean_fitness\tmax_fitness\tmax_frequency\tmean_distance");
	}

	public void sample(int generation, Population population) {
        population.estimateDiversity(10);

		destination.println(generation + "\t"
                + population.getMeanDiversity() + "\t"
                + population.getMaxDiversity() + "\t"
				+ population.getMinFitness() + "\t"
				+ population.getMeanFitness() + "\t"
				+ population.getMaxFitness() + "\t"
				+ population.getMaxFrequency() + "\t"
				+ population.getMeanDistance());
	}

	public void cleanUp() {
		destination.close();
	}

}
