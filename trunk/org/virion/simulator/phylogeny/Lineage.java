package org.virion.simulator.phylogeny;

import java.util.List;
import java.util.LinkedList;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Lineage.java,v 1.1.1.1 2006/02/16 10:22:54 rambaut Exp $
 */
public class Lineage {

    private Lineage(Lineage parent, int generations) {
        this.parent = parent;
        this.generations = generations;
    }

    public int getGenerations() {
        return generations;
    }

    void setGenerations(int generations) {
        this.generations = generations;
    }

    public Lineage getParent() {
        return parent;
    }

    void setParent(Lineage parent) {
        this.parent = parent;
    }

    private Lineage parent;
    private int generations;

    public static Lineage createLineage(Lineage parent, int generations) {
        if (availableLineages.isEmpty()) {
            return new Lineage(parent, generations);
        }
        return availableLineages.remove(0);
    }

    public static void destroyLineage(Lineage lineage) {
        availableLineages.add(lineage);
    }

    private static List<Lineage> availableLineages = new LinkedList<Lineage>();
}
