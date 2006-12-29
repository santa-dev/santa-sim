package santa.simulator.selectors;

import santa.simulator.Random;
import santa.simulator.Virus;

/**
 * A simple but slow implementation
 * @author Andrew Rambaut
 */
public class SimpleRouletteWheelSelector implements Selector {
    public SimpleRouletteWheelSelector() {
    }

    public void initializeSelector(Virus[] currentGeneration, int parentCount) {
        this.currentGeneration = currentGeneration;
	    calculateCumulativeFitness(currentGeneration);
    }


	/**
	 * Sample a virus from a precalculated set of parents.
	 */
	public Virus nextSelection() {
	    return currentGeneration[nextSelectionIndex()];
	}

    /**
     * Sample a virus from a precalculated set of parents.
     */
    public int nextSelectionIndex() {

	    double r = Random.nextUniform(0.0, 1.0);
	    int selected = 0;
	    while (r > cumulativeFitness[selected]) {
	        selected ++;
	    }

	    return selected;
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

    private Virus[] currentGeneration;
    private double[] cumulativeFitness = null;
}
