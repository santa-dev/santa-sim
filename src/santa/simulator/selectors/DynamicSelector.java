package santa.simulator.selectors;

import java.util.Collections;
import java.util.List;

import santa.simulator.Random;
import santa.simulator.Virus;

public class DynamicSelector implements Selector {

	//Look through literature for estimates of growth rate per generation
	private double growthRate = 1000;
	//value of the carrying population would depend on the type of the question under investigation
	private double carryingPopulation = 1000;

	private double expectedProgenyCount;

	public DynamicSelector() {

	}

	public DynamicSelector(double growthRate, double carryingPopulation) {
		this.growthRate = growthRate;
		this.carryingPopulation = carryingPopulation;
	}

	public void selectParents(List<Virus> currentGeneration, List<Integer> selectedParents, int nbOfParents) {
            Random.shuffle(currentGeneration);
		for(int i = 0; i < currentGeneration.size(); ++i) {
			double fitness = currentGeneration.get(i).getFitness();
                        
                        //Abbas: The below formulation of logistic growth was implemented by Gertjan
			//It was noticed that for slower growth rates, the simulator overshoots the carrying size.
			//A slightly different formulation is introduced according to: https://www.maa.org/press/periodicals/loci/joma/logistic-growth-model-background-logistic-modeling
			//expectedProgenyCount =  Math.max(fitness * (1 + growthRate*(1-selectedParents.size()/carryingPopulation)),Double.MIN_VALUE);
			expectedProgenyCount =  fitness * growthRate * (1 - selectedParents.size() / carryingPopulation);
                        
                        // Set to zero if infinite fitness is observed (this usually happens if zero fitness is changed to nonzero fitness)
			long nbChildren = 0;
                        if (fitness > 0 && Double.isFinite(fitness) && expectedProgenyCount > Double.MIN_VALUE)
                                nbChildren =  Random.nextPoisson(expectedProgenyCount);
			for(long n = 0; n < nbChildren * nbOfParents; ++n) {
				selectedParents.add(i);
			}
		}
		Random.shuffle(selectedParents);
	}

}
