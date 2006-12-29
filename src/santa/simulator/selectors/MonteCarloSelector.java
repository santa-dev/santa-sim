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
 * @author rambaut
 *         Date: Apr 22, 2005
 *         Time: 9:12:27 AM
 */
public class MonteCarloSelector implements Selector {

    public MonteCarloSelector() {
        // Nothing to do
        System.err.println("Koen thinks the MonteCarloSelector is not sampling correctly.");
    }

    public void initializeSelector(Virus[] currentGeneration, int parentCount) {
        this.currentGeneration = currentGeneration;

        maxLogFitness = currentGeneration[0].getLogFitness();
        for (int i = 1; i < currentGeneration.length; i++) {
            double f = currentGeneration[i].getLogFitness();
            if (f > maxLogFitness) {
                maxLogFitness = f;
            }
        }

        maxLogFitness = Math.exp(maxLogFitness);

        if (maxLogFitness == 0.0) {
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
            double r = Math.log(Random.nextUniform(0.0, maxLogFitness));
            if (r < currentGeneration[currentVirus].getLogFitness()) {
                selected = currentVirus;
            }
            currentVirus = (currentVirus + 1) % currentGeneration.length;
        } while (selected < 0);

        return selected;
    }

    private double maxLogFitness;
    private Virus[] currentGeneration;
    private int currentVirus;
}
