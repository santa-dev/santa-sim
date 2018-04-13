package santa.simulator.population;

import java.util.List;

import santa.simulator.Virus;
import santa.simulator.selectors.Selector;

public class StaticPopulationGrowth implements PopulationGrowth {
	
	private int initialPopulationSize;
	
	public StaticPopulationGrowth(int initialPopulationSize) {
		this.initialPopulationSize = initialPopulationSize;
	}

	public void select(Selector selector, List<Virus> current, List<Integer> selectedParents, int parentCount, int generation) {
		selector.selectParents(current, selectedParents, initialPopulationSize * parentCount);		
	}

}
