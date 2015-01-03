package santa.simulator.fitness;

import santa.simulator.genomes.*;
import santa.simulator.population.Population;

import java.util.Set;

/**
 * An abstract class defining a general fitness function on a byte-encoded molecular sequence
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 */
public interface FitnessFactor {

	/**
	 * Advance to a next generation, which is important for fitness functions that are
	 * time-dependent.
	 *
	 * @return If the fitness function factor has changed and thus fitness needs to recomputed
	 *         using computeLogFitness(states).
	 */
	boolean updateGeneration(int generation, Population population);

    /**
     * @return Compute the log fitness of the given genome, from scratch.
     */
	double computeLogFitness(byte[] states);

    /**
	 * @return Update the log fitness of the given genome which it will have after the given
     *         mutation is applied.
     */
    double getLogFitnessChange(StateChange change);

	Feature getFeature();

    Set<Integer> getSites();
}