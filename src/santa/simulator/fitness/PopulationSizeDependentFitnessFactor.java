package santa.simulator.fitness;

import java.util.Set;

import santa.simulator.genomes.Feature;
import santa.simulator.genomes.StateChange;
import santa.simulator.population.Population;

public class PopulationSizeDependentFitnessFactor extends AbstractFitnessFactor {

	private int max;
	private double declineRate;
	
	private int populationSize;
	private double logFitness;
	
	
	public PopulationSizeDependentFitnessFactor(int max, double declineRate, Feature feature, Set<Integer> sites) {
		super(feature, sites);
		this.max = max;
		this.declineRate = declineRate;
	}
	
	public boolean updateGeneration(int generation, Population population) {
		//we should only be updating if population size changed
		if(population.getPopulationSize() != populationSize) {
			populationSize = population.getPopulationSize();
			// get the population mean fitness without the contribution of this factor in the previous generation
			double meanFitness = population.getMeanFitness() - logFitness;
			// calculate the new factor
			GeneralisedLogisticFunction glf = new GeneralisedLogisticFunction(1-meanFitness, 0, -declineRate, max);
			logFitness = glf.getFunctionValue(populationSize);
			//System.err.println("DEBUG: "+populationSize+" "+meanFitness+" "+logFitness);
			return true;
		}
		return false;
	}

	public double computeLogFitness(byte[] states) {		
		return logFitness;
	}

	public double getLogFitnessChange(StateChange change) {
		return logFitness;
	}
	
}
