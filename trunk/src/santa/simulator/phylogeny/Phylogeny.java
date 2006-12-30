package santa.simulator.phylogeny;

import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.graphs.Node;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Phylogeny.java,v 1.2 2006/02/20 15:18:00 rambaut Exp $
 */
public class Phylogeny {

	public Phylogeny(int populationSize) {
		this.populationSize = populationSize;
		extantLineages = new Lineage[populationSize];
		newExtantLineages = new Lineage[populationSize];
	}

	public void initialize() {
		availableLineages.addAll(lineages);
		lineages.clear();

		for (int i = 0; i < populationSize; i++) {
			Lineage child = createLineage();

			extantLineages[i] = child;
			child.parent = null;
			child.generation = 0;
			child.childCount = 0;
		}
		mrca = extantLineages[0];
	}

	public void addGeneration(int generation, int[] selectedParents) {
		for (int i = 0; i < populationSize; i++) {
			Lineage child = createLineage();

			child.parent = extantLineages[selectedParents[i]];
			if (child.parent != null) {
				child.parent.childCount ++;
			}

			child.generation = generation;
			child.childCount = 0;

			newExtantLineages[i] = child;
		}

		// prune out the dead wood (lineages that weren't selected as parents)
		for (int i = 0; i < populationSize; i++) {
			Lineage lineage = extantLineages[i];
			if (lineage.getChildCount() == 0) {

				Lineage parent = lineage.getParent();
				removeLineage(lineage);

				while (parent != null && parent.getChildCount() < 2) {
					lineage = parent;
					parent = lineage.parent;

					// prune the lineage out
					removeLineage(lineage);
				}
				if (parent != null) {
					parent.childCount --;
				}
			}
		}

		Lineage[] tmp = extantLineages;
		extantLineages = newExtantLineages;
		newExtantLineages = tmp;

	}

	public int getSize() {
		return lineages.size() - availableLineages.size();
	}

	public int getLineageCount() {
		return lineages.size();
	}

	public Lineage getMRCA() {
		return mrca;
	}

	public void pruneDeadLineages() {

		int oldestGeneration = Integer.MAX_VALUE;
		mrca = null;

		// prune out degree 2 nodes
		for (int i = 0; i < populationSize; i++) {
			Lineage lineage = extantLineages[i];
			Lineage parent = lineage.getParent();

			while (parent != null) {

				assert(parent.getChildCount() > 0);

				if (parent.getChildCount() == 1) {
					lineage.parent = parent.parent;
					removeLineage(parent);
				} else {
					if (parent.getGeneration() < oldestGeneration) {
						oldestGeneration = parent.getGeneration();
						mrca = parent;
					}

					lineage = parent;
				}
				parent = lineage.parent;
			}
		}
	}

	public RootedTree reconstructPhylogeny(int[] sample, List<Taxon> taxa) {

		SimpleRootedTree tree = new SimpleRootedTree();
		Lineage[] lineages = new Lineage[sample.length];
		Node[] nodes = new Node[sample.length];

		for (int i = 0; i < sample.length; i++) {
			lineages[i] = extantLineages[sample[i]];
			nodes[i] = tree.createExternalNode(taxa.get(i));
			tree.setHeight(nodes[i], lineages[i].generation);
		}
		int lineageCount = sample.length;

		while (lineageCount > 1) {
// @todo
		}


		return tree;
	}

	private Lineage createLineage() {
		if (availableLineages.isEmpty()) {
			Lineage lineage = new Lineage();
			lineages.add(lineage);
			return lineage;
		} else {
			return availableLineages.remove(0);
		}
	}

	private void removeLineage(Lineage lineage) {
		availableLineages.add(lineage);
	}

	private final int populationSize;
	private final List<Lineage> lineages = new LinkedList<Lineage>();
	private Lineage[] extantLineages;
	private Lineage[] newExtantLineages;

	private Lineage mrca = null;


	private List<Lineage> availableLineages = new LinkedList<Lineage>();
}
