/*
 * Created on Apr 19, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import santa.simulator.Population;
import santa.simulator.genomes.*;

import java.util.*;

/**
 * Computes the fitness as the product of different components.
 *
 * In addition, keeps track of the individual contributions of each component for
 * any Genome, so that factors may decide to recompute or update their individual
 * contribution.
 */
public final class FitnessFunction  {
    int generation;
    private List<FitnessFactor> factors;

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

    public FitnessFunction(List<FitnessFactor> factors) {
        this.factors = factors;
    }

	public boolean updateGeneration(int generation, Population population) {
	    boolean changed = false;

	    for (FitnessFactor f : factors) {
	        if (f.updateGeneration(generation, population))
	            changed = true;
	    }

	    return changed;
	}

    /**
     * Return the updated log fitness that the given genome will
     * have when the given mutation will be applied to it.
     */
    public void updateLogFitness(Genome genome, SortedSet<Mutation> mutations) {
        double result = 0;

        FitnessGenomeCache cache = genome.getFitnessCache();

        int i = 0;
        for (FitnessFactor f : factors) {
            double contrib = cache.factorContributions[i];

	        Feature feature = f.getFeature();
	        Set<Integer> sites = f.getSites();

	        byte[] sequence = genome.getStates(feature);
	        boolean potentiallyHasFailed = false;

	        for (Mutation m : mutations) {
		        if (sites.contains(m.position)) {
			        double delta = f.getLogFitnessChange(m.position, sequence[m.position], m.state);

			        if (delta == Double.NEGATIVE_INFINITY) {
				        // If the fitness has changed to -Inf (perhaps because this mutation has induced a stop codon) then
				        // a future mutation may reverse it if it is in the same codon. However, you can't subtract the
				        // -Inf so we flag this situation and then recalculate the entire fitness when all the mutations have
				        // been applied.
				        potentiallyHasFailed = true;
				        break;
			        }

			        contrib += delta;
		        }
	        }

	        if (potentiallyHasFailed) {
		        contrib = f.computeLogFitness(sequence);
	        }

            cache.factorContributions[i++] = contrib;
            result += contrib;
        }

        genome.setLogFitness(result);



    }

    // I have commented this out because I think we need just 2 functions, 1 that takes
	// set of mutations and 1 that recomputes the fitness from scratch

//    /**
//     * Update the log fitness for the genome, given that a new generation is
//     * started and one of the components has indicated that all fitnesses need
//     * to be recomputed.
//     */
//    public void updateLogFitness(Genome genome) {
//        FitnessGenomeCache cache = genome.getFitnessCache();
//
//        double result = 0;
//
//        int i = 0;
//        for (FitnessFactor f : factors) {
//            double contrib = cache.factorContributions[i];
//            contrib = f.updateLogFitness(genome, contrib);
//            cache.factorContributions[i++] = contrib;
//            result += contrib;
//        }
//
//        genome.setLogFitness(result);
//    }
//
	/**
	 * Compute and set the fitness of a genome from scratch, and store the cached
	 * values for each factor.
	 */
	public double computeLogFitness(Genome genome) {
	    double result = 0;

	    FitnessGenomeCache cache = new FitnessGenomeCache();
	    genome.setFitnessCache(cache);


	    int i = 0;
	    for (FitnessFactor f : factors) {
		    Feature feature = f.getFeature();

		    byte[] sequence = genome.getStates(feature);
	        double contrib = f.computeLogFitness(sequence);
	        cache.factorContributions[i++] = contrib;
	        result += contrib;
	    }

	    genome.setLogFitness(result);

	    return result;
	}

}
