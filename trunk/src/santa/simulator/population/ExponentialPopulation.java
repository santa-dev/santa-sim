package santa.simulator.population;

import java.util.List;

import santa.simulator.Virus;
import santa.simulator.genomes.GenePool;
import santa.simulator.phylogeny.Phylogeny;
import santa.simulator.selectors.Selector;

public class ExponentialPopulation extends Population {

	private int initialPopulationSize;
	private double growthRate;
	
	public ExponentialPopulation(int initialPopulationSize, double growthRate, GenePool genePool, Selector selector, Phylogeny phylogeny) {
		super(genePool, selector, phylogeny);
		this.initialPopulationSize = initialPopulationSize;
		this.growthRate = growthRate;
	}
	
	protected void select(List<Virus> current, List<Integer> selectedParents, int parentCount, int generation) {
		int nbToSelect = (int) (initialPopulationSize * Math.pow(2, generation * growthRate));
		selector.selectParents(current, selectedParents, nbToSelect);		
	}
	
}
