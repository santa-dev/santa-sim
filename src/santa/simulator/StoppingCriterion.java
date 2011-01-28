package santa.simulator;

import santa.simulator.population.Population;

/**
 * @author Andrew Rambaut
 * @version $Id: StoppingCriterion.java,v 1.2 2006/02/17 15:28:07 kdforc0 Exp $
 */
public abstract class StoppingCriterion {

    public abstract boolean stop(int generation, Population population);

    public static StoppingCriterion generationCount(final int count) {
        return new StoppingCriterion() {

            public boolean stop(int generation, Population population) {
                return generation >= count;
            }
        };
    }

    public static StoppingCriterion orCriterion(final StoppingCriterion[] criteria) {
        return new StoppingCriterion() {

            public boolean stop(int generation, Population population) {
                for (StoppingCriterion criterion : criteria) {
                    if (criterion.stop(generation, population)) return true;
                }
                return false;
            }
        };
    }

    public static StoppingCriterion andCriterion(final StoppingCriterion[] criteria) {
        return new StoppingCriterion() {

            public boolean stop(int generation, Population population) {
                for (StoppingCriterion criterion : criteria) {
                    if (!criterion.stop(generation, population)) return false;
                }
                return true;
            }
        };
    }

    public static StoppingCriterion fitnessCriterion(final double lowerFitnessThreshold, final double upperFitnessThreshold) {
    	return new StoppingCriterion() {

			public boolean stop(int generation, Population population) {
				double meanFitness = population.getMeanFitness();

				return (meanFitness >= upperFitnessThreshold || meanFitness <= lowerFitnessThreshold);
			}
    	};
    }
}
