package org.virion.simulator.replicators;

import org.virion.simulator.Virus;
import org.virion.simulator.selectors.Selector;
import org.virion.simulator.fitness.FitnessFunction;
import org.virion.simulator.genomes.*;
import org.virion.simulator.mutators.Mutator;

import java.util.SortedSet;

/**
 * @author rambaut
 *         Date: Apr 27, 2005
 *         Time: 9:40:33 AM
 */
public class ClonalReplicator implements Replicator {

    public ClonalReplicator() {
        // nothing to do
    }

    public void replicate(Virus virus, Selector selector, Mutator mutator, FitnessFunction fitnessFunction, GenePool genePool) {

        Virus parent = selector.nextSelection();

        Genome parentGenome = parent.getGenome();

        SortedSet<Mutation> mutations = mutator.mutate(parentGenome);

        Genome genome = genePool.duplicateGenome(parentGenome, mutations, fitnessFunction);

        virus.setGenome(genome);
        virus.setParent(parent);
    }
}
