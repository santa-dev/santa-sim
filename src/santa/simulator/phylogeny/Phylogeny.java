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

	public void addGeneration(int generation, List<Integer> selectedParents) {
		for (int i = 0; i < populationSize; i++) {
			Lineage child = createLineage();

			child.parent = extantLineages[selectedParents.get(i)];
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
			tree.setHeight(nodes[i], 0);

			Lineage lineage = lineages[i];
			while (lineage != null) {
				lineage.count = 0;
				lineage = lineage.parent;
			}
		}

		int tipGeneration = lineages[0].generation;

		for (int i = 0; i < sample.length; i++) {
			Lineage lineage = lineages[i];
			while (lineage != null) {
				lineage.count ++;
				lineage = lineage.parent;
			}
		}

		// find the next shared node for each lineage
		for (int i = 0; i < sample.length; i++) {
			while (lineages[i] != null && lineages[i].count == 1) {
				lineages[i] = lineages[i].parent;
			}
			if (lineages[i] == null) {
				// the phylogeny has not fully coalesced.
				return null;
			}
		}

		int lineageCount = sample.length;
		List<Node> children = new ArrayList<Node>();
		while (lineageCount > 1) {
			int i = 0;
			while (i < lineageCount) {
				children.clear();

				// find matches
				int j = i + 1;
				while (j < lineageCount) {
					if (lineages[i] == lineages[j]) {
						children.add(nodes[j]);

						// move the lineage/node from the end of the arrays
						lineages[j] = lineages[lineageCount - 1];
						lineages[lineageCount - 1] = null;
						nodes[j] = nodes[lineageCount - 1];
						lineageCount --;
					} else {
						j++;
					}
				}

				// if node i matched anything then create an internal node
				if (children.size() > 0) {
					children.add(nodes[i]);
					Node node = tree.createInternalNode(children);
					tree.setHeight(node, tipGeneration - lineages[i].generation);
					nodes[i] = node;

					if (lineageCount > 1) {
						// we still have lineages to coalesce

						int currentCount = lineages[i].count;
						lineages[i] = lineages[i].parent;
						// find the next shared node for this lineage - this is found when
						// the count increases by one
						while (lineages[i] != null && lineages[i].count == currentCount) {
							lineages[i] = lineages[i].parent;
						}
						if (lineages[i] == null) {
							// the phylogeny has not fully coalesced.
							return null;
						}
					}
				} else {
					i++;
				}
			}
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
