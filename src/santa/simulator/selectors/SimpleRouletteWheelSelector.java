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

	public void selectParents(Virus[] currentGeneration, int[] selectedParents) {
	    calculateCumulativeFitness(currentGeneration);

		for (int i = 0; i < selectedParents.length; i++) {
			double r = Random.nextUniform(0.0, 1.0);
			int selected = 0;
			while (r > cumulativeFitness[selected]) {
				selected ++;
			}
			selectedParents[i] = selected;
		}
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
}
