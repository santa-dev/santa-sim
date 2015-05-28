
package santa.simulator;
import org.apache.commons.math3.distribution.PascalDistribution;

public class NegBinIndelModel extends IndelModel {
    public NegBinIndelModel(int r, double q) {
		rng = new PascalDistribution(r, q);
	}

	public int nextLength() {
		// return the next random number from negative-binomial distribution
		int p = rng.sample();
		assert(p >= 0);
		return(p);
	}

	private PascalDistribution rng;
}
