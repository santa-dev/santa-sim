package santa.simulator.selectors;

import java.util.List;

import santa.simulator.Random;
import santa.simulator.Virus;

/**
 * An attempt to replace the SimpleRouletteWheenSelector
 * with a faster binary search algorithm.
 * @author Abbas
 */
public class BinarySearchSelector implements Selector {
    
	public void selectParents(List<Virus> currentGeneration, List<Integer> selectedParents, int sampleSize) {
	    calculateCumulativeFitness(currentGeneration);

		for (int i = 0; i < sampleSize; i++) {
			double r = Random.nextUniform(0.0, 1.0);
			int selected = 0;

			int highIndex = currentGeneration.size() -1;
			int lowIndex = -1;
			int middleIndex = 0; 			
			while(highIndex > lowIndex +1){

				middleIndex  = (highIndex + lowIndex) / 2; 

				if(cumulativeFitness[middleIndex] == r) {
					lowIndex = middleIndex;
					highIndex = middleIndex;
					selected = middleIndex + 1;
				}

				else if(cumulativeFitness[middleIndex] < r) {
					lowIndex = middleIndex;
					selected = lowIndex + 1;
				}
				else if(cumulativeFitness[middleIndex] > r) {
					highIndex = middleIndex;
					selected = highIndex;
				}

			}

			selectedParents.add(selected);
		}
    }

    private final void calculateCumulativeFitness(List<Virus> currentGeneration) {

        int populationSize = currentGeneration.size();

        if (cumulativeFitness == null) {
            cumulativeFitness = new double[populationSize];
        }

        cumulativeFitness[0] = currentGeneration.get(0).getFitness();

        for (int i = 1; i < populationSize; i++) {
	if (Double.isNaN(currentGeneration.get(i).getFitness())){
		System.err.println("YY");
		}
            cumulativeFitness[i] = cumulativeFitness[i-1] + currentGeneration.get(i).getFitness();
        }

        double totalFitness = cumulativeFitness[populationSize-1];

        if (totalFitness == 0.0) {
            throw new RuntimeException("Population crashed! No viable children.");
        }

        for (int i = 0; i < populationSize; i++) {
            cumulativeFitness[i] /= totalFitness;
        }
	if (Double.isNaN(cumulativeFitness[0] )){
		System.err.println("XX");
	}
    }

    private double[] cumulativeFitness = null;
}
