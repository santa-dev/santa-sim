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
import santa.simulator.Population;

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

		Iterator<Integer> it = getSites().iterator();

		while (it.hasNext()) {
			int site = it.next();

			if (sequence[site - 1] >= getAlphabet().getStateCount())
				logFitness += Double.NEGATIVE_INFINITY;
			else
				logFitness += this.logFitness[site - 1][sequence[site - 1]];
		}

		return logFitness;
	}

    public double getLogFitnessChange(int position, byte oldState, byte newState) {
        return logFitness[position][newState] - logFitness[position][oldState];
    }

	public double getLogFitness(int i, byte state) {
		return logFitness[i][state];
	}

	protected void setLogFitness(int i, byte state, double f) {
		logFitness[i][state] = f;
	}

	private double[][] logFitness;
}