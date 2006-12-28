package santa.simulator.fitness;

import santa.simulator.Population;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.Mutation;

/**
 * An abstract class defining a general fitness function on a byte-encoded molecular sequence
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 */
public interface FitnessFunctionFactor {

    /**
     * @return Compute the log fitness of the given genome, from scratch.
     */
	double computeLogFitness(Genome genome);

    /**
     * @param logFitness Current log fitness contribution
     * @return Update the log fitness of the given genome which it will have after the given
     *         mutation is applied.
     */
    double updateLogFitness(Genome genome, double logFitness, Mutation m);

    /**
     * @return Update the log fitness of the given genome, considering that fitness may change
     *         from generation to generation.
     */
    double updateLogFitness(Genome genome, double logFitness);

    /**
     * Advance to a next generation, which is important for fitness functions that are
     * time-dependent.
     *
     * @return If the fitness function factor has changed and thus fitness needs to recomputed
     *         using updateLogFitness(genome, logFitness).
     */
    boolean updateGeneration(int generation, Population population);
}