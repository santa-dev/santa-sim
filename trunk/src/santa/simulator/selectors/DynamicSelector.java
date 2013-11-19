package santa.simulator.selectors;

import java.util.Collections;
import java.util.List;

import santa.simulator.Random;
import santa.simulator.Virus;
/**
 * 
 * @author abbas
 *	implementation of population dynamics:
 *	E(number of progeny per parent) = (1 + r (1-PopulationSize/CarryingSize)) * fitness of the parrent
 *	where r is the growth rate per generation
 * 
 */
public class DynamicSelector implements Selector {

	//Look through literature for estimates of growth rate per generation 
	double growthRate = 5;
	//value of the carrying population would depend on the type of the question under investigation
	double carryingPopulation = 1000;
	double expectedProgenyCount;
	public void selectParents(List<Virus> currentGeneration, List<Integer> selectedParents, int nbOfParents) {
		for(int i = 0; i < currentGeneration.size(); ++i) {
			double fitness = currentGeneration.get(i).getFitness();
			expectedProgenyCount =  fitness * (1 + growthRate*(1-selectedParents.size()/carryingPopulation));
			long nbChildren = fitness == 0 ? 0 : Random.nextPoisson(expectedProgenyCount);
			for(long n = 0; n < nbChildren * nbOfParents; ++n) {
				selectedParents.add(i);				
			}			
		}
		Collections.shuffle(selectedParents);
	}

}
