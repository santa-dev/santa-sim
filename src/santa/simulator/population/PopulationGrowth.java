package santa.simulator.population;

import java.util.List;

import santa.simulator.Virus;
import santa.simulator.selectors.Selector;

public interface PopulationGrowth {
	public void select(Selector selector, List<Virus> current, List<Integer> selectedParents, int parentCount, int generation);
}
