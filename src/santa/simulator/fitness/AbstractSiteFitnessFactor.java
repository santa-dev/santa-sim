/*
 * EmpiricalFitnessFunction.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.fitness;

import santa.simulator.genomes.*;
import santa.simulator.population.Population;

import java.util.*;

/**
 * This is an implementation of FitnessFunction which encapsulates empirical estimates of the
 * fitness effects of different states.
 */
public abstract class AbstractSiteFitnessFactor extends AbstractFitnessFactor {

	public AbstractSiteFitnessFactor(Feature feature, Set<Integer> sites) {
		super(feature, sites);
	}

	/**
	 * Set the fitnesses for every state at every site included.
	 */
	protected void initialize(double[][] logFitness) {
		this.logFitness = logFitness;
	}

	public boolean updateGeneration(int generation, Population population) {
		return false;
	}

	public double computeLogFitness(byte[] sequence) {

		double logFitness = 0.0;

		// catch IndexOutOfBoundsException b/c indels may have caused site index to shift out of range.
		if (this.logFitness.length != sequence.length) {
			logFitness = Double.NEGATIVE_INFINITY;
		} else {
			for (int site = 0; site < this.logFitness.length; site++) {
				logFitness += this.logFitness[site][sequence[site]];
				if (logFitness == Double.NEGATIVE_INFINITY) {
					break;
				}
			}
		}

		return logFitness;
	}

	public double getLogFitnessChange(StateChange change) {
		// we ignore the sites list here because it is probably cheaper to look
		// up in the table and get a zero change than check the site list.
		// It does mean the logFitness array should have zeros in sites that
		// are not handled by this factor.

		// note: a position not covered by the 'logFitness' array
		// (i.e. generates IndexOutOfBoundsException) also indicates a
		// site not handled by this factor.

		double fit = 0;	 // neutral fitness
		double[] logFitnessPosition = null;
		try {
			logFitnessPosition = logFitness[change.position];
		} catch(IndexOutOfBoundsException e) {
			// catch IndexOutOfBoundsException b/c insertions may have
			// caused site index to shift out of range.

			// Note: Feature coordinates may shift or shrink due to indels.
			// FitnessFactor coordinates never change!
			//
			// Imagine a Feature defined over nt [1,9] with a
			// FitnessFactor defined on nt [1,3].  If an insertion
			// extends the feature to [1,11], attempts acces 
			// fitness values beyond the original bounds of the
			// Feature will result in an IndexOutOfBoundsException.
			// This is treated the same as if the FitnessFactor did
			// not handle that portion of the Feature.

			// To see how often this happens, uncomment the next line.
			// System.err.format("IndexOutOfBoundsException: %d is not within feature %s\n", change.position, getFeature().getName());
		}
		if (logFitnessPosition != null)
			fit = logFitnessPosition[change.newState] - logFitnessPosition[change.oldState];

		return fit;

	}

	public double getLogFitness(int i, byte state) {
		// catch IndexOutOfBoundsException b/c indels may have caused site index to shift out of range.
		double fit;
		try {
			fit = logFitness[i][state];
		} catch(IndexOutOfBoundsException e) {
			fit = 0; // neutral fitness
		}
		return fit;
	}

	protected void setLogFitness(int i, byte state, double f) {
		logFitness[i][state] = f;
	}

	private double[][] logFitness;
}
