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

		mrca = createLineage();
		mrca.parent = null;
		mrca.generation = 0;
		mrca.childCount = 0;
		for (int i = 0; i < populationSize; i++) {
			extantLineages[i] = mrca;
		}
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

	/*
	 * Coalesce the lineages into a tree.
	 *
	 * Imagine a tree with leaves at the bottom and the root at
	 * the top.  We start out knowing only a list of leaves and
	 * want to populate the internal nodes of the tree with a
	 * branching structure that reflects the lineage of the
	 * leaves.
	 *
	 * In this routine, the leaves of the tree are instances of
	 * 'Lineage', each of which is the head of a linked list of ancestral lineage objects.  Each lineage element in the 
	 * list holds a 'count' of the number of leaves that are reachable
	 * from that object.  While traversing the linked list of lineage
	 * objects, the count goes up at each point where two or more
	 * branches coalesce into one.  Each lineage also contains a
	 * 'generation' that indicates in which generation the lineage was
	 * created.
	 *
	 * At all times, leaves descended from the same immediate common
	 * ancestor will reference the same lineage object representing
	 * that branch point.  This invariant is maintained as the list of
	 * leaves is changed during tree construction.
	 * 
	 * To construct the tree, we move from the leaves toward the root,
	 * creating internal nodes in the tree and coalescing identical
	 * lineages as we go.  To start, choose the leftmost lineage with
	 * the highest generation number.  Collect all other leaves
	 * referencing the same lineage; these are siblings.  Create an
	 * internal node with the siblings as children.  Replace the
	 * siblings with a single leaf that references the next branch
	 * point up the lineage.  Repeat until there is only one remaining
	 * lineage and the tree has been constructed.
	 * 
	 * If the leaves are processed in decreasing generation order, the
	 * result will be a tree with a single MRCA.  Previous versions of
	 * this code processed the leaves left-to-right regardless of
	 * generation, and that is guaranteed to fail in some cases.
	 */
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

			// find the left-most, maximum generation lineage.
			int i = 0;
			int maxgen = 0;
			for (int ii = 0; ii < lineageCount; ii++) {
				if (maxgen < lineages[ii].generation) {
					maxgen = lineages[ii].generation;
					i = ii;
				}
			}

			// must have one or more lineages to the right or we won't have anything to coalesce.
			assert i < lineageCount-1;
			
			// find siblings
			children.clear();
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

			// expect to coalesce at least two lineages each time through this loop
			assert children.size() > 0;

			children.add(nodes[i]);
			Node node = tree.createInternalNode(children);
			tree.setHeight(node, tipGeneration - lineages[i].generation);
			nodes[i] = node;

			if (lineageCount > 1) {
				// we still have lineages to coalesce

				int currentCount = lineages[i].count;
				while (lineages[i].parent != null) {
					lineages[i] = lineages[i].parent;
					if (lineages[i].count != currentCount) {
						break;
					}
				}

				// sanity check - should never happen
				assert lineages[i] != null;
			}
		} // end-while

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
		lineage.parent = null;
		availableLineages.add(lineage);
	}

	private final int populationSize;
	private final List<Lineage> lineages = new LinkedList<Lineage>();
	private Lineage[] extantLineages;
	private Lineage[] newExtantLineages;

	private Lineage mrca = null;


	private List<Lineage> availableLineages = new LinkedList<Lineage>();
}
