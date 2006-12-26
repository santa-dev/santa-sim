package org.virion.simulator.replicators;

import org.virion.simulator.Virus;
import org.virion.simulator.fitness.FitnessFunction;
import org.virion.simulator.genomes.GenePool;
import org.virion.simulator.mutators.Mutator;
import org.virion.simulator.selectors.Selector;

/**
 * @author rambaut
 *         Date: Apr 27, 2005
 *         Time: 9:33:54 AM
 */
public interface Replicator {

    void replicate(Virus virus, Selector selector, Mutator mutator, FitnessFunction fitnessFunction, GenePool genePool);

}
