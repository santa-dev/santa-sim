package santa.simulator.replicators;

import santa.simulator.Virus;
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

	public int getParentCount() {
		return 1;
	}

    public void replicate(Virus virus, Virus[] parents, Mutator mutator, FitnessFunction fitnessFunction, GenePool genePool) {

        Genome parentGenome = parents[0].getGenome();

        SortedSet<Mutation> mutations = mutator.mutate(parentGenome);

        Genome genome = genePool.duplicateGenome(parentGenome, mutations, fitnessFunction);

        virus.setGenome(genome);
        virus.setParent(parents[0]);
    }
}
