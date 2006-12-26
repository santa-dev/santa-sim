/*
 * Population.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package org.virion.simulator.selectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.virion.simulator.Random;
import org.virion.simulator.Virus;

/**
 * @author rambaut
 *         Date: Apr 22, 2005
 *         Time: 9:12:27 AM
 */
public class RouletteWheelSelector implements Selector {
    public RouletteWheelSelector() {
    }

    public void initializeSelector(Virus[] currentGeneration) {
        this.currentGeneration = currentGeneration;
        selectParents();
    }

    /**
     * Direct sample of a virus to be selected -- very slow !
     */
    public Virus nextSelection_old() {
        double r = Random.nextUniform(0.0, 1.0);
        int selected = 0;
        while (r > cumulativeFitness[selected]) {
            selected ++;
        }

        return currentGeneration[selected];
    }

    /**
     * Sample a virus from a precalculated set of parents.
     */
    public Virus nextSelection() {
        
        /*
         * randomIntNumbers is a selection without replacement of all parents that have
         * been precalculated.
         */
        return currentGeneration[pickedParents[randomIntNumbers.get(currentIndex++)]];
    }

// This is the old way of doing it. This involved drawing the random numbers,
// sorting them, and then walking up the list of numbers and matching them off
// the cumulative fitnesses

    public final void selectParents() {
        int populationSize = currentGeneration.length;
        int sampleSize = 2 * populationSize; // maximum number needed when every child has two parents.

        calculateCumulativeFitness(currentGeneration);

        if (randomIntNumbers == null) {
            randomIntNumbers = new ArrayList<Integer>();
            for (int i = 0; i < sampleSize; ++i)
                randomIntNumbers.add(i);
        }

        // shuffle the selection of parents
        Collections.shuffle(randomIntNumbers);

        if (randomNumbers == null) {
            randomNumbers = new double[sampleSize];
        }

        for (int i = 0; i < sampleSize; i++) {
            randomNumbers[i] = Random.nextUniform(0.0, 1.0);
        }

        Arrays.sort(randomNumbers);

        if (pickedParents == null) {
            pickedParents = new int[sampleSize];
        }

        int i = 0;
        int j = 0;
        do {
            while (j < sampleSize && randomNumbers[j] < cumulativeFitness[i]) {
                pickedParents[j] = i;
                j++;
            }
            i++;
        } while (i < populationSize);
        
        currentIndex = 0;
    }

    private final void calculateCumulativeFitness(Virus[] currentGeneration) {

        int populationSize = currentGeneration.length;

        if (cumulativeFitness == null) {
            cumulativeFitness = new double[populationSize];
        }

        cumulativeFitness[0] = currentGeneration[0].getGenome().getFitness();

        for (int i = 1; i < populationSize; i++) {
            cumulativeFitness[i] = cumulativeFitness[i-1] + currentGeneration[i].getGenome().getFitness();
        }

        double totalFitness = cumulativeFitness[populationSize-1];

        if (totalFitness == 0.0) {
            throw new RuntimeException("Population crashed! No viable children.");
        }

        for (int i = 0; i < populationSize; i++) {
            cumulativeFitness[i] /= totalFitness;
        }
    }

    private Virus[] currentGeneration;
    private double[] cumulativeFitness = null;

    private double[] randomNumbers;
    private ArrayList<Integer> randomIntNumbers;
    private int[] pickedParents;
    int currentIndex;
}
