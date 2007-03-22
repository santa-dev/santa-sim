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
    private boolean[] factorsChanged;

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
        factorsChanged = new boolean[factors.size()];
    }

    public void updateGeneration(int generation, Population population) {
        int i = 0;
        for (FitnessFactor f : factors) {
            factorsChanged[i] = f.updateGeneration(generation, population);
            i++;
        }
    }

// This site by site update system needs careful thinking about. For the moment,
// I am simply forcing a recalculation of each fitnessFactor.


//    /**
//     * Try to update all the fitness factors with the set of
//     */
//    public void updateLogFitness(Genome genome, SortedSet<Mutation> mutations) {
//        double result = 0;
//
//        FitnessGenomeCache cache = genome.getFitnessCache();
//
//        int i = 0;
//        for (FitnessFactor factor : factors) {
//            double contrib = cache.factorContributions[i];
//
//            Feature feature = factor.getFeature();
//
//            byte[] sequence = genome.getStates(feature);
//
//            if (factorsChanged[i]) {
//                contrib = factor.computeLogFitness(sequence);
//            } else {
//                // At the moment, there is no optimization here for individual mutations.
//
//
//
//                contrib = factor.computeLogFitness(sequence);
//
//                boolean potentiallyHasFailed = false;
//
//                for (Mutation m : mutations) {
//                    if (sites.contains(m.position)) {
//                        double delta = factor.getLogFitnessChange(m.position, sequence[m.position], m.state);
//
//                        if (delta == Double.NEGATIVE_INFINITY) {
//                            // If the fitness has changed to -Inf (perhaps because this mutation has induced a stop codon) then
//                            // a future mutation may reverse it if it is in the same codon. However, you can't subtract the
//                            // -Inf so we flag this situation and then recalculate the entire fitness when all the mutations have
//                            // been applied.
//                            potentiallyHasFailed = true;
//                            break;
//                        }
//
//                        contrib += delta;
//                    }
//                }
//
//                if (potentiallyHasFailed) {
//                    contrib = factor.computeLogFitness(sequence);
//                }
//            }
//
//            cache.factorContributions[i] = contrib;
//            result += contrib;
//            i++;
//        }
//
//        genome.setLogFitness(result);
//
//    }

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
