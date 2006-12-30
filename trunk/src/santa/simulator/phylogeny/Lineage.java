package santa.simulator.phylogeny;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Lineage.java,v 1.1.1.1 2006/02/16 10:22:54 rambaut Exp $
 */
public class Lineage {

    Lineage() {
	    // empty
    }

    public int getGeneration() {
        return generation;
    }

    public Lineage getParent() {
        return parent;
    }

	public int getChildCount() {
	    return childCount;
	}

	Lineage parent;
    int generation;
	int childCount;

	int count;
}
