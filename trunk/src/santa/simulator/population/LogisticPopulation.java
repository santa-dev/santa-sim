package santa.simulator.population;

import java.util.List;

import santa.simulator.Virus;
import santa.simulator.genomes.GenePool;
import santa.simulator.phylogeny.Phylogeny;
import santa.simulator.selectors.Selector;

public class LogisticPopulation extends Population {

	private int maxPopulationSize;
	private int initialPopulationSize;
	private int growthRate;

	public LogisticPopulation(int initialPopulationSize, int growth, int maxPopulationSize, GenePool genePool, Selector selector, Phylogeny phylogeny) {
		super(genePool, selector, phylogeny);
		this.initialPopulationSize = initialPopulationSize;
		this.growthRate = growth;
		this.maxPopulationSize = maxPopulationSize;
	}

	protected void select(List<Virus> current, List<Integer> selectedParents, int parentCount, int generation) {
		int nbToSelect = (int) (growthRate * initialPopulationSize * (1 - (initialPopulationSize / (double) maxPopulationSize)));
		selector.selectParents(current, selectedParents, nbToSelect);
	}

}
