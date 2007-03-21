/*
 * Population.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.selectors;

import santa.simulator.Random;
import santa.simulator.Virus;

import java.util.*;

/**
 * @author rambaut
 *         Date: Apr 22, 2005
 *         Time: 9:12:27 AM
 */
public class RouletteWheelSelector implements Selector {
    public RouletteWheelSelector() {
    }

	public void selectParents(Virus[] currentGeneration, int[] selectedParents) {
        int populationSize = currentGeneration.length;
        int sampleSize = selectedParents.length;

        calculateCumulativeFitness(currentGeneration);

        if (randomNumbers == null) {
            randomNumbers = new double[sampleSize];
        }

        for (int i = 0; i < sampleSize; i++) {
            randomNumbers[i] = Random.nextUniform(0.0, 1.0);
        }

        Arrays.sort(randomNumbers);

        int i = 0;
        int j = 0;
        do {
            while (j < sampleSize && randomNumbers[j] < cumulativeFitness[i]) {
                selectedParents[j] = i;
                j++;
            }
            i++;
        } while (i < populationSize);

		Random.shuffle(selectedParents);
    }

    private final void calculateCumulativeFitness(Virus[] currentGeneration) {

        int populationSize = currentGeneration.length;

        if (cumulativeFitness == null) {
            cumulativeFitness = new double[populationSize];
        }

        cumulativeFitness[0] = currentGeneration[0].getFitness();

        for (int i = 1; i < populationSize; i++) {
            cumulativeFitness[i] = cumulativeFitness[i-1] + currentGeneration[i].getFitness();
        }

        double totalFitness = cumulativeFitness[populationSize-1];

        if (totalFitness == 0.0) {
            throw new RuntimeException("Population crashed! No viable children.");
        }

        for (int i = 0; i < populationSize; i++) {
            cumulativeFitness[i] /= totalFitness;
        }
    }

    private double[] cumulativeFitness = null;
    private double[] randomNumbers;
}