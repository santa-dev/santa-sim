/*
 * Created on Apr 19, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import java.util.List;

import santa.simulator.Population;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.Mutation;

/**
 * Computes the fitness as the product of different components.
 *
 * In addition, keeps track of the individual contributions of each component for
 * any Genome, so that factors may decide to recompute or update their individual
 * contribution.
 */
public class FitnessFunction  {
    int generation;
    private List<FitnessFunctionFactor> factors;

    /**
     * A cache of calculated fitness contributions, for each factor,
     * that is stored in each Genome.
     */
    public class FitnessGenomeCache {
        double[] factorContributions;

        FitnessGenomeCache() {
            this.factorContributions = new double[factors.size()];
        }

        public FitnessGenomeCache clone() {
            FitnessGenomeCache clone = new FitnessGenomeCache();
            System.arraycopy(factorContributions, 0,
                             clone.factorContributions, 0, factorContributions.length);

            return clone;
        }
    }

    public FitnessFunction(List<FitnessFunctionFactor> factors) {
        this.factors = factors;
    }

    /**
     * Compute and set the fitness of a genome from scratch, and store the cached
     * values for each factor.
     */
    public double computeLogFitness(Genome genome) {
        double result = 0;

        FitnessGenomeCache cache = new FitnessGenomeCache();
        genome.setFitnessCache(cache);

        int i = 0;
        for (FitnessFunctionFactor f : factors) {
            double contrib = f.computeLogFitness(genome);
            cache.factorContributions[i++] = contrib;
            result += contrib;
        }

        genome.setLogFitness(result);

        return result;
    }

    /**
     * Return the updated log fitness that the given genome will
     * have when the given mutation will be applied to it.
     */
    public double updateLogFitness(Genome genome, Mutation m) {
        double result = 0;

        FitnessGenomeCache cache = genome.getFitnessCache();

        int i = 0;
        for (FitnessFunctionFactor f : factors) {
            double contrib = cache.factorContributions[i];
            contrib = f.updateLogFitness(genome, contrib, m);
            cache.factorContributions[i++] = contrib;
            result += contrib;
        }

        return result;
    }

    /**
     * Update the log fitness for the genome, given that a new generation is
     * started and one of the components has indicated that all fitnesses need
     * to be recomputed.
     */
    public void updateLogFitness(Genome genome) {
        FitnessGenomeCache cache = genome.getFitnessCache();

        double result = 0;

        int i = 0;
        for (FitnessFunctionFactor f : factors) {
            double contrib = cache.factorContributions[i];
            contrib = f.updateLogFitness(genome, contrib);
            cache.factorContributions[i++] = contrib;
            result += contrib;
        }

        genome.setLogFitness(result);
    }

    public boolean updateGeneration(int generation, Population population) {
        boolean changed = false;

        for (FitnessFunctionFactor f : factors) {
            if (f.updateGeneration(generation, population))
                changed = true;
        }

        return changed;
    }
}
