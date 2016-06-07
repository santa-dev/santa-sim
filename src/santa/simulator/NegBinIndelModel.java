
package santa.simulator;

import santa.simulator.Random;
import org.apache.commons.math3.distribution.PascalDistribution;

public class NegBinIndelModel extends IndelModel {
    public NegBinIndelModel(int r, double q) {
		// use the same preconfigured pseudo-random number generator
		// across all sampling routines so we get consistent results
		// when explicitly setting the seed!
		rng = new PascalDistribution(Random.randomData.getRandomGenerator(), r, q);
	}

	public int nextLength() {
		// return the next random number from negative-binomial distribution
		int p = rng.sample();
		assert(p >= 0);
		return(p);
	}

	private PascalDistribution rng;
}
