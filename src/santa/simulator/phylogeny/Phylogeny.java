package santa.simulator.phylogeny;

import java.util.List;
import java.util.LinkedList;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Phylogeny.java,v 1.2 2006/02/20 15:18:00 rambaut Exp $
 */
public class Phylogeny {

    public Phylogeny(int populationSize) {
        this.populationSize = populationSize;

        extantLineages = new int[populationSize];
        for (int i = 0; i < populationSize; i++) {
            extantLineages[i] = i;
            lineages.add(Lineage.createLineage(null, 0));
        }
    }

    public void addGeneration(int generation, int[] selectedParents) {
        for (int i = 0; i < populationSize; i++) {
            Lineage child = Lineage.createLineage(lineages.get(extantLineages[selectedParents[i]]), generation);
            lineages.add(child);
        }
    }

    private final int populationSize;
    private final List<Lineage> lineages = new LinkedList<Lineage>();
    private final int[] extantLineages;
}
