/*
 * Population.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.selectors;

import santa.simulator.Virus;
import santa.simulator.Random;

/**
 * This is an experimental selector that which attempts to draw parents proportional
 * to their fitness. The main advantage of this is that it can do it with the fitnesses
 * in log space to avoid having to exponentiate them however, it has to log the random
 * number instead so it probably makes little difference.
 *
 * Koen thinks it doesn't sample correctly but it gets a very high correlation in the
 * frequency picked to fitness using the TestSelectors class.
 *
 * @author Andrew Rambaut
 */
public class MonteCarloSelector implements Selector {

    public MonteCarloSelector() {
        // Nothing to do
        System.err.println("Koen thinks the MonteCarloSelector is not sampling correctly.");
    }

    public void initializeSelector(Virus[] currentGeneration, int parentCount) {
        this.currentGeneration = currentGeneration;

        double maxLogFitness = currentGeneration[0].getLogFitness();
        for (int i = 1; i < currentGeneration.length; i++) {
            double f = currentGeneration[i].getLogFitness();
            if (f > maxLogFitness) {
                maxLogFitness = f;
            }
        }

        maxFitness = Math.exp(maxLogFitness);

        if (maxFitness == 0.0) {
            throw new RuntimeException("Population crashed! No viable children.");
        }

        currentVirus = Random.nextInt(0, currentGeneration.length - 1);

    }

	/**
	 * Sample a virus from a precalculated set of parents.
	 */
	public Virus nextSelection() {
	    return currentGeneration[nextSelectionIndex()];
	}

    public int nextSelectionIndex() {
        int selected = -1;
        do {
	        currentVirus = Random.nextInt(0, currentGeneration.length - 1);

            double r = Math.log(Random.nextUniform(0.0, maxFitness));
            if (r < currentGeneration[currentVirus].getLogFitness()) {
                selected = currentVirus;
            }

      //    currentVirus = (currentVirus + 1) % currentGeneration.length;
        } while (selected < 0);

        return selected;
    }

    private double maxFitness;
    private Virus[] currentGeneration;
    private int currentVirus;
}
