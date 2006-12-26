package org.virion.simulator;

import java.util.Arrays;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: TestSampler.java,v 1.2 2006/02/16 14:50:37 rambaut Exp $
 */
public class TestSampler {

    static int popSize = 100;
    static int repCount = 100000;


    public TestSampler() {
        double[] fitnesses = new double[popSize];
        for (int i = 0; i < fitnesses.length; i++) {
            fitnesses[i] = Random.nextUniform(0.0, 1.0);
        }

        double[] cumulativeFitness = calculateCumulativeFitness(fitnesses);

        int[] selections1 = new int[popSize];
        int[] selections2 = new int[popSize];

	    long startTime = System.currentTimeMillis();
	    for (int r = 0; r < repCount; r++) {
	        proportionalSelection1(cumulativeFitness, selections1);

	        if (r % 50000 == 0) System.err.print(".");
	    }
	    System.err.println();
	    long time = System.currentTimeMillis() - startTime;
	    System.err.println("proportionalSelection1: " + time);

	    startTime = System.currentTimeMillis();
        for (int r = 0; r < repCount; r++) {
            fastSelection5(fitnesses, selections2);

            if (r % 50000 == 0) System.err.print(".");
        }
	    System.err.println();
	    time = System.currentTimeMillis() - startTime;
	    System.err.println("fastSelection5: " + time);

        System.out.println("fitness\tproportional\tfast");
        for (int i = 0; i < fitnesses.length; i++) {
            System.out.println(fitnesses[i] + "\t" + (((double)selections1[i])/repCount) + "\t" + (((double)selections2[i])/repCount));
        }
    }

    double[] calculateCumulativeFitness(double[] fitnesses) {

        double[] cumulativeFitness = new double[popSize];

        cumulativeFitness[0] = fitnesses[0];

        for (int i = 1; i < popSize; i++) {
            cumulativeFitness[i] = cumulativeFitness[i-1] + fitnesses[i];
        }

        return cumulativeFitness;
    }

    void proportionalSelection1(double[] cumulativeFitness, int[] selections) {

        for (int i = 0; i < popSize; i++) {
            double r = Random.nextUniform(0.0, cumulativeFitness[cumulativeFitness.length - 1]);
            int index = Arrays.binarySearch(cumulativeFitness, r);
            int index2 = -index - 1;
            selections[index2] ++;
        }
    }

    void proportionalSelection2(double[] cumulativeFitness, int[] selections) {

        double[] randomNumbers = new double[popSize];

        for (int i = 0; i < popSize; i++) {
            randomNumbers[i] = Random.nextUniform(0.0, cumulativeFitness[popSize - 1]);
        }

        Arrays.sort(randomNumbers);

        int i = 0;
        int j = 0;
        do {
            while (j < popSize && randomNumbers[j] < cumulativeFitness[i]) {
                selections[i] ++;
                j++;
            }
            i++;
        } while (i < popSize);
    }

    private void fastSelection5(double[] fitnesses, int[] selections) {

        double[] logFitnesses = new double[popSize];
        double maxFitness = fitnesses[0];
        logFitnesses[0] = Math.log(fitnesses[0]);
        for (int i = 1; i < popSize; i++) {
            if (fitnesses[i] > maxFitness) {
                maxFitness = fitnesses[i];
            }
            logFitnesses[i] = Math.log(fitnesses[i]);
        }

        int count = 0;
        int index = Random.nextInt(0, popSize - 1);
        while (count < popSize) {
            double r = Math.log(Random.nextUniform(0.0, maxFitness));
            if (r < logFitnesses[index]) {
                selections[index] ++;
                count++;
            }
            index = (index + 1) % popSize;
        }
    }

    public static void main(String[] args) {
        new TestSampler();
    }
}
