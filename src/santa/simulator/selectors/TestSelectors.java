package santa.simulator.selectors;

import java.util.ArrayList;
import java.util.List;

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
		List<Virus> population = new ArrayList<Virus>(popSize);
		for (int i = 0; i < popSize; i++) {
			final double r = Random.nextPoisson(1.0);
			final double lr = Math.log(r);
			population.add(new Virus() {
				public double getFitness() {
					return Math.exp(lr);
				}

				public double getLogFitness() {
					return lr;
				}
			});
		}

		int[] simpleSelection = new int[popSize];
		int[] rouletteSelection = new int[popSize];
		int[] discreteRouletteSelection = new int[popSize];
		int[] monteCarloSelection = new int[popSize];

		testSelector(new SimpleRouletteWheelSelector(), population, simpleSelection);
		testSelector(new RouletteWheelSelector(), population, rouletteSelection);
		testSelector(new MonteCarloSelector(), population, monteCarloSelection);

		System.out.println("fitness\tsimple\troulette\tdiscrete\tmonteCarlo");
		for (int i = 0; i < population.size(); i++) {
			System.out.println(population.get(i).getFitness() +
					"\t" + (((double)simpleSelection[i])/repCount) +
					"\t" + (((double)rouletteSelection[i])/repCount) +
					"\t" + (((double)discreteRouletteSelection[i])/repCount) +
					"\t" + (((double)monteCarloSelection[i])/repCount));
		}
	}

	void testSelector(Selector selector, List<Virus> population, int[] selections) {
		long startTime = System.currentTimeMillis();

		List<Integer> selected = new ArrayList<Integer>(popSize);

		for (int r = 0; r < repCount; r++) {
			selector.selectParents(population, selected, popSize);
			for (int i = 0; i < popSize; i++) {
				selections[selected.get(i)] ++;
			}
			if (r % 10000 == 0) System.err.print(".");
			selected.clear();
		}
		System.err.println();
		long time = System.currentTimeMillis() - startTime;
		System.err.println(selector.getClass().getName() + ": " + time);
	}

	public static void main(String[] args) {
		new TestSelectors();
	}
}
