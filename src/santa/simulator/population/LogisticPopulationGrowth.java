package santa.simulator.population;

import java.util.List;

import santa.simulator.Virus;
import santa.simulator.selectors.Selector;

public class LogisticPopulationGrowth implements PopulationGrowth {

	private int maxPopulationSize;
	private int initialPopulationSize;
	private int growthRate;

	public LogisticPopulationGrowth(int initialPopulationSize, int growth, int maxPopulationSize) {
		this.initialPopulationSize = initialPopulationSize;
		this.growthRate = growth;
		this.maxPopulationSize = maxPopulationSize;
	}

	public void select(Selector selector, List<Virus> current, List<Integer> selectedParents, int parentCount, int generation) {
		int nbToSelect = (int) (growthRate * initialPopulationSize * (1 - (initialPopulationSize / (double) maxPopulationSize)));
		selector.selectParents(current, selectedParents, nbToSelect);
	}

}
