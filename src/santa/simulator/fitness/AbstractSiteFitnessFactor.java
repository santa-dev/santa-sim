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
		try {
			for (int site : getSites()) {
				logFitness += this.logFitness[site][sequence[site]];
			}
		} catch(IndexOutOfBoundsException e) { /* ignore */ }

		return logFitness;
	}

    public double getLogFitnessChange(StateChange change) {
	    // we ignore the sites list here because it is probably cheaper to look
	    // up in the table and get a zero change than check the site list.
	    // It does mean the logFitness array should have zeros in sites that
	    // are not handled by this factor.
		//
		// catch IndexOutOfBoundsException b/c indels may have caused site index to shift out of range.
		double fit;
		try {
			fit = logFitness[change.position][change.newState] - logFitness[change.position][change.oldState];
		} catch(IndexOutOfBoundsException e) {
			fit = 0; // neutral fitness
		}
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
