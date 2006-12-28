package santa.simulator.replicators;

import santa.simulator.Virus;
import santa.simulator.selectors.Selector;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.*;
import santa.simulator.mutators.Mutator;

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