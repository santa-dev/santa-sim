package santa.simulator.selectors;

import santa.simulator.Random;
import santa.simulator.Virus;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: TestSampler.java,v 1.2 2006/02/16 14:50:37 rambaut Exp $
 */
public class TestSelectors {

	static int popSize = 1000;
	static int repCount = 100000;


	public TestSelectors() {
		Virus[] population = new Virus[popSize];
		for (int i = 0; i < population.length; i++) {
			final double r = Random.nextUniform(0.0, 1.0);
			final double lr = Math.log(r);
			population[i] = new Virus() {
				public double getFitness() {
					return Math.exp(lr);
				}

				public double getLogFitness() {
					return lr;
				}
			};
		}

		int[] simpleSelection = new int[popSize];
		int[] rouletteSelection = new int[popSize];
		int[] discreteRouletteSelection = new int[popSize];
		int[] monteCarloSelection = new int[popSize];

		testSelector(new SimpleRouletteWheelSelector(), population, simpleSelection);
		testSelector(new RouletteWheelSelector(), population, rouletteSelection);
		testSelector(new MonteCarloSelector(), population, monteCarloSelection);

		System.out.println("fitness\tsimple\troulette\tdiscrete\tmonteCarlo");
		for (int i = 0; i < population.length; i++) {
			System.out.println(population[i].getFitness() +
					"\t" + (((double)simpleSelection[i])/repCount) +
					"\t" + (((double)rouletteSelection[i])/repCount) +
					"\t" + (((double)discreteRouletteSelection[i])/repCount) +
					"\t" + (((double)monteCarloSelection[i])/repCount));
		}
	}

	void testSelector(Selector selector, Virus[] population, int[] selections) {
		long startTime = System.currentTimeMillis();

		int[] selected = new int[popSize];

		for (int r = 0; r < repCount; r++) {
			selector.selectParents(population, selected);
			for (int i = 0; i < popSize; i++) {
				selections[selected[i]] ++;
			}
			if (r % 10000 == 0) System.err.print(".");
		}
		System.err.println();
		long time = System.currentTimeMillis() - startTime;
		System.err.println(selector.getClass().getName() + ": " + time);
	}

	public static void main(String[] args) {
		new TestSelectors();
	}
}
