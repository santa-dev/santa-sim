/*
 * Created on Apr 19, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import santa.simulator.genomes.*;
import santa.simulator.population.Population;

import java.util.*;

/**
 * Computes the fitness as the product of different components.
 *
 * In addition, keeps track of the individual contributions of each component for
 * any Genome, so that factors may decide to recompute or update their individual
 * contribution.
 */
public final class FitnessFunction	{
	int generation;
	private List<FitnessFactor> factors;
	private boolean[] recomputeFactor;

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
		recomputeFactor = new boolean[factors.size()];
	}

	public void updateGeneration(int generation, Population population) {
		int i = 0;
		for (FitnessFactor f : factors) {
			recomputeFactor[i] = f.updateGeneration(generation, population);
			i++;
		}
	}

	/**
	 * Update all the fitness factors with the set of mutations
	 */
	public void updateLogFitness(Genome genome, SortedSet<Mutation> mutations) {
		double result = 0;

		FitnessGenomeCache cache = genome.getFitnessCache();

		int i = 0;
		for (FitnessFactor factor : factors) {
			double contrib = cache.factorContributions[i];

			if (!recomputeFactor[i]) {
				Feature feature = genome.getDescription().getFeature(factor.getFeature().getName());
				if (feature == null) {
					// note - if indels are active, it is possible for a feature to shrink to nothing,
					// then looking up a feature by name may fail!
					// When that happens, force the contribution of this factor to be neutral.
					contrib = 0;
					continue;
				}

				List<StateChange> changes = genome.getChanges(feature, mutations);

				for (StateChange change : changes) {
					double delta = factor.getLogFitnessChange(change);
					contrib += delta;
				}
			}

			cache.factorContributions[i] = contrib;
			result += contrib;
			i++;
		}

		genome.setLogFitness(result);

	}


	/**
	 * Update all the fitness factors that have to recompute
	 */
	public void updateLogFitness(Genome genome) {
		double result = 0;

		FitnessGenomeCache cache = genome.getFitnessCache();
		if (cache != null) {
			int i = 0;
			for (FitnessFactor factor : factors) {
				double contrib = cache.factorContributions[i];

				if (recomputeFactor[i]) {
					Feature feature = factor.getFeature();
					byte[] sequence = genome.getStates(feature);
					contrib = factor.computeLogFitness(sequence);
				}

				cache.factorContributions[i] = contrib;
				result += contrib;
				i++;
			}
		} else {
			// recompute the fitness function over this genome.
			computeLogFitness(genome);
		}


		// if the genome has shrunk below a single codon, it has -inf fitness.
		if (genome.getLength() < 3) 
			result = Double.NEGATIVE_INFINITY;

		genome.setLogFitness(result);
	}

	/**
	 * Compute and set the fitness of a genome from scratch, and store the cached
	 * values for each factor.
	 */
	public void computeLogFitness(Genome genome) {
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
	}

}
