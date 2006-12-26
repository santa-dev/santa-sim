/*
 * Created on Apr 14, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.virion.simulator.fitness;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ChiSquaredDistribution;
import org.apache.commons.math.distribution.DistributionFactory;
import org.virion.simulator.Random;
import org.virion.simulator.genomes.*;

import java.util.Set;

/**
 * A Purifying fitness function performs puryfing selection. It is configured by
 * giving it a wild-type (fittest) sequence, and a selection strength.
 *
 * The selection strength k is used as a parameter to a chi-square function,
 * which is used to draw selection coefficients from for different amino-
 * acids. The (negative) log10 fitness difference is drawn from a chi-square
 * distribution with k degrees of freedom. This gives a mean log10 fitness
 * difference of -k, and a variation in log fitness difference of 2*k.
 *
 * k = 0 corresponds to neutral selection, and increasing values of k increase
 * the level of purifying selection. For example, k = 1 gives an average
 * log10 (fitness) of -1 for an amino acid change, compared to fitness=1 for WT.
 */
public class PurifyingFitnessFunction extends AbstractSiteFitnessFunction {
    private byte fittestStates[];
    private double purificationStrength;

    public PurifyingFitnessFunction(Sequence fittest, double purificationStrength,
                                    Set<Integer> sites, SequenceAlphabet alphabet) {
        super(sites, alphabet);

        this.purificationStrength = purificationStrength;

        int states = fittest.getLength(alphabet);

        fittestStates = new byte[states];
        for (int i = 0; i < fittestStates.length; ++i) {
            fittestStates[i] = fittest.getState(alphabet, i);
        }

        setFitnesses();
    }

    public PurifyingFitnessFunction(byte fittestStates[], double purificationStrength,
                                    Set<Integer> sites, SequenceAlphabet alphabet) {
        super(sites, alphabet);

        this.purificationStrength = purificationStrength;
        this.fittestStates = fittestStates;

        setFitnesses();
    }

    protected void setFitnesses() {
        int stateSize = getAlphabet().getStateCount();
        Set<Integer> sites = getSites();

        ChiSquaredDistribution distr = null;

        if (purificationStrength > 0)
            distr = DistributionFactory.newInstance().createChiSquareDistribution(purificationStrength);

        double average = 0;

        double[][] logFitness = new double[GenomeDescription.getGenomeLength(getAlphabet())][stateSize];

	    double x = 0.0;
	    System.out.println("Using purifying fitness function: " + purificationStrength);
	    System.out.println("Distribution:");
	    System.out.println(x + "\t0.0");
	    x += 0.05;
	    for (int j = 1; j < stateSize - 1; ++j) {
		    try {
		        double v = - distr.inverseCumulativeProbability(x);
		        v *= Math.log(10);
			    System.out.println(x + "\t" + v);
			    x += 0.05;
		    } catch (MathException e) {
		        throw new RuntimeException(e);
		    }
	    }
	    System.out.println();
	    System.out.println();

        for (int i = 0; i < logFitness.length; ++i) {
            if (sites.contains(i + 1)) {
                for (int j = 0; j < stateSize; ++j) {
                    boolean isFittest = (j == fittestStates[i]);

                    if (isFittest)
                        logFitness[i][j] = 0;
                    else {
                        if (distr != null) {
                            double p = Random.nextUniform(0, 1);
                            try {
                                double v = - distr.inverseCumulativeProbability(p);
                                v *= Math.log(10);
                                average += v;
                                logFitness[i][j] = v;
                            } catch (MathException e) {
                                throw new RuntimeException(e);
                            }
                        } else
                            logFitness[i][j] = 0;
                    }
                }
            } else {
                for (int j = 0; j < stateSize; ++j) {
                    logFitness[i][j] = 0;
                }
            }
        }

        average /= (sites.size() * (stateSize-1));
        System.err.println("10^(Average log10 amino acid fitness): " + Math.exp(average));

        initialize(logFitness);
    }

    protected double getPurificationStrength() {
        return purificationStrength;
    }

    protected void setPurificationStrength(double purificationStrength) {
        this.purificationStrength = purificationStrength;
    }

    public double updateLogFitness(Genome genome, double logFitness) {
        return logFitness;
    }
}
