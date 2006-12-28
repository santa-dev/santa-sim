package santa.simulator.replicators;

import santa.simulator.Virus;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.GenePool;
import santa.simulator.mutators.Mutator;
import santa.simulator.selectors.Selector;

/**
 * @author rambaut
 *         Date: Apr 27, 2005
 *         Time: 9:33:54 AM
 */
public interface Replicator {

    void replicate(Virus virus, Selector selector, Mutator mutator, FitnessFunction fitnessFunction, GenePool genePool);

}