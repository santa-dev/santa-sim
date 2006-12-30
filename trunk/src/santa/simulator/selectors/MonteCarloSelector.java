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

	public void selectParents(Virus[] currentGeneration, int[] selectedParents) {

		double maxLogFitness = currentGeneration[0].getLogFitness();
		for (int i = 1; i < currentGeneration.length; i++) {
			double f = currentGeneration[i].getLogFitness();
			if (f > maxLogFitness) {
				maxLogFitness = f;
			}
		}

		double maxFitness = Math.exp(maxLogFitness);

		if (maxFitness == 0.0) {
			throw new RuntimeException("Population crashed! No viable children.");
		}

		for (int i = 0; i < selectedParents.length; i++) {
			int selected = -1;
			do {
				int currentVirus = Random.nextInt(0, currentGeneration.length - 1);

				double r = Math.log(Random.nextUniform(0.0, maxFitness));
				if (r < currentGeneration[currentVirus].getLogFitness()) {
					selected = currentVirus;
				}
			} while (selected < 0);

			selectedParents[i] = selected;
		}
	}

}
