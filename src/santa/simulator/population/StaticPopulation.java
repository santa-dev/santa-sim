package santa.simulator.population;

import java.util.List;

import santa.simulator.Virus;
import santa.simulator.genomes.GenePool;
import santa.simulator.phylogeny.Phylogeny;
import santa.simulator.selectors.Selector;

public class StaticPopulation extends Population {
	
	private int initialPopulationSize;
	
	public StaticPopulation(int initialPopulationSize, GenePool genePool, Selector selector, Phylogeny phylogeny) {
		super(genePool, selector, phylogeny);
		this.initialPopulationSize = initialPopulationSize;
	}

	@Override
	protected void select(List<Virus> current, List<Integer> selectedParents, int parentCount, int generation) {
		selector.selectParents(current, selectedParents, initialPopulationSize * parentCount);		
	}

}
