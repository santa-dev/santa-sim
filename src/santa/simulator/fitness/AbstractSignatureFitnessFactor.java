/*
 * Created on Jul 18, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import santa.simulator.genomes.*;

import java.util.Arrays;
import java.util.Set;

public abstract class AbstractSignatureFitnessFactor extends AbstractFitnessFactor {


	public AbstractSignatureFitnessFactor(Feature feature, Set<Integer> sites) {
		super(feature, sites);
	}

	protected Signature createSignature(Genome genome) {
		Sequence seq = genome.getSequence();

		byte state[] = new byte[getSites().size()];
		int i = 0;
		for (Integer site : getSites()) {
			byte b = seq.getState(getAlphabet(), site - 1);

			state[i++] = b;
		}

		return new Signature(state);
	}

	static protected final class Signature {
		byte state[];

		Signature(byte state[]) {
			this.state = state;
		}

		@Override
		public boolean equals(Object o) {
			Signature other = (Signature) o;
			return Arrays.equals(state, other.state);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(state);
		}

		@Override
		public String toString() {
			return Arrays.toString(state);
		}


	}

}
