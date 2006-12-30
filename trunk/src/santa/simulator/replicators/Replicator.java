package santa.simulator.replicators;

import santa.simulator.Virus;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.GenePool;
import santa.simulator.mutators.Mutator;

/**
 * @author rambaut
 *         Date: Apr 27, 2005
 *         Time: 9:33:54 AM
 */
public interface Replicator {

	int getParentCount();

    void replicate(Virus virus, Virus[] parents, Mutator mutator, FitnessFunction fitnessFunction, GenePool genePool);

}
