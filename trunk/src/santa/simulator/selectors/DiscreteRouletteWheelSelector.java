package santa.simulator.selectors;

import santa.simulator.Random;
import santa.simulator.Virus;

/**
 * This is an experimental selector that uses a number of discrete bins
 * proportional to the fitness of each individual. The next parent can
 * then be picked by simply drawing uniformly from the bins.
 *
 * The fewer the bins, the faster the selector but the more course-scaled an
 * approximation it will be. Probably not worth using.
 *
 * @author Andrew Rambaut
 */
public class DiscreteRouletteWheelSelector implements Selector {
    public DiscreteRouletteWheelSelector(int binCount) {
	    this.binCount = binCount;
    }

	public void initializeSelector(Virus[] currentGeneration, int parentCount) {
        this.currentGeneration = currentGeneration;
        selectParents(currentGeneration);
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

		int bin = Random.nextInt(0, parentBins.length - 1);
	    return parentBins[bin];
	}

    private final void selectParents(Virus[] currentGeneration) {
        int populationSize = currentGeneration.length;
        int totalBins = binCount * populationSize;

        double sumFitness = getSumFitness(currentGeneration);
		double scaleFactor = totalBins / sumFitness;

        if (parentBins == null) {
            parentBins = new int[totalBins];
        }

	    int k = 0;
        for (int i = 0; i < populationSize; i++) {
	        double fitness = currentGeneration[i].getFitness();
	        int bins = (int)(fitness * scaleFactor);
	        for (int j = 0; j < bins; j++) {
                parentBins[k] = i;
		        k++;
	        }
        }
    }

    private final double getSumFitness(Virus[] currentGeneration) {

		double maxFitness = currentGeneration[0].getFitness();
	    double totalFitness = maxFitness;
        for (int i = 1; i < currentGeneration.length; i++) {
            double fitness = currentGeneration[i].getFitness();
	        if (fitness > maxFitness) {
		        maxFitness = fitness;
	        }
	        totalFitness += fitness;
        }


        if (totalFitness == 0.0) {
            throw new RuntimeException("Population crashed! No viable children.");
        }

		return totalFitness;
    }

	private final int binCount;

    private Virus[] currentGeneration;

    private int[] parentBins;
}
