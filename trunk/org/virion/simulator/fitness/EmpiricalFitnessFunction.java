/*
 * EmpiricalFitnessFunction.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package org.virion.simulator.fitness;

import java.util.Set;

import org.virion.simulator.Random;
import org.virion.simulator.genomes.Genome;
import org.virion.simulator.genomes.GenomeDescription;
import org.virion.simulator.genomes.SequenceAlphabet;

/**
 * This is an implementation of FitnessFunction which encapsulates empirical estimates of the
 * fitness effects of different states.
 */
public class EmpiricalFitnessFunction extends AbstractSiteFitnessFunction {
    /**
     * Constructor
     * @param mutationFitnesses an array of fitnesses for different states
     * @param isRandom should the fitness effects be assigned randomly to states?
     */
    public EmpiricalFitnessFunction(double[] mutationFitnesses, boolean isRandom,
                                    Set<Integer> sites, SequenceAlphabet alphabet) {
        super(sites, alphabet);
        setFitnesses(mutationFitnesses, isRandom);
    }

    /**
     * @param fitnesses
     * @param isRandom
     */
    public void setFitnesses(double[] fitnesses, boolean isRandom) {
        double[][] logFitness
            = new double[GenomeDescription.getGenomeLength(getAlphabet())][fitnesses.length];

        for (int i = 0; i < logFitness.length; i++) {
            if (getSites().contains(i + 1)) {
                int index = 0;
                if (isRandom) {
                    index = Random.nextInt(0, fitnesses.length - 1);
                }

                for (int j = 0; j < fitnesses.length; j++) {
                    logFitness[i][j] = Math.log(fitnesses[index]);
                    index = (index + 1) % fitnesses.length;
                }
            }
        }

        initialize(logFitness);
    }

    public double updateLogFitness(Genome genome, double logFitness) {
        return logFitness;
    }
}