package santa.simulator.samplers;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import santa.simulator.population.Population;

public class StatisticsSampler implements Sampler {
	private PrintStream destination;
    private String filename;
    private String separator = ",";
    
	public StatisticsSampler(String filename) {
        this.filename = filename;
	}

	public void initialize(int replicate) {
        String fname = filename.replaceAll("%r", String.valueOf(replicate+1));
        try {
            destination = new PrintStream(fname);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open file for writing: " + fname);
        }

        destination.println("generation" + separator
        		+"population_size" + separator
        		+"mean_diversity" + separator
        		+"max_diversity" + separator
        		+"min_fitness" + separator
        		+"mean_fitness" + separator
        		+"max_fitness" + separator
        		+"max_frequency" + separator
        		+"mean_distance");
	}

	public void sample(int generation, Population population) {
		
        population.estimateDiversity((population.getCurrentGeneration().size()/100)+1);

		destination.println(generation + separator
				+ population.getCurrentGeneration().size() + separator
                + population.getMeanDiversity() + separator
                + population.getMaxDiversity() + separator
				+ population.getMinFitness() + separator
				+ population.getMeanFitness() + separator
				+ population.getMaxFitness() + separator
				+ population.getMaxFrequency() + separator
				+ population.getMeanDistance());
	}

	public void cleanUp() {
		destination.close();
	}

}
