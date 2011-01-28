package santa.simulator.selectors;

import java.util.Collections;
import java.util.List;

import santa.simulator.Random;
import santa.simulator.Virus;

public class DynamicSelector implements Selector {

	public void selectParents(List<Virus> currentGeneration, List<Integer> selectedParents, int nbOfParents) {
		for(int i = 0; i < currentGeneration.size(); ++i) {
			double fitness = currentGeneration.get(i).getFitness();
			long nbChildren = fitness == 0 ? 0 : Random.nextPoisson(fitness);
			for(long n = 0; n < nbChildren * nbOfParents; ++n) {
				selectedParents.add(i);				
			}			
		}
		Collections.shuffle(selectedParents);
	}

}
