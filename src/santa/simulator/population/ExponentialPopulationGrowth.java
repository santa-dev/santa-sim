package santa.simulator.population;

import java.util.List;

import santa.simulator.Virus;
import santa.simulator.selectors.Selector;

public class ExponentialPopulationGrowth implements PopulationGrowth {

	private int initialPopulationSize;
	private double growthRate;
	
	public ExponentialPopulationGrowth(int initialPopulationSize, double growthRate) {
		this.initialPopulationSize = initialPopulationSize;
		this.growthRate = growthRate;
	}
	
	public void select(Selector selector, List<Virus> current, List<Integer> selectedParents, int parentCount, int generation) {
		int nbToSelect = (int) (initialPopulationSize * Math.pow(2, generation * growthRate));
		selector.selectParents(current, selectedParents, nbToSelect);		
	}
	
}
