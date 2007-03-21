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

public abstract class AbstractSignatureFitnessFactor implements FitnessFactor {

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

    protected final Set<Integer> sites;
    protected final SequenceAlphabet alphabet;

    public AbstractSignatureFitnessFactor(Set<Integer> sites, SequenceAlphabet alphabet) {
        this.sites = sites;
        this.alphabet = alphabet;
    }

    protected Signature createSignature(Genome genome) {
        Sequence seq = genome.getSequence();

        byte state[] = new byte[sites.size()];
        int i = 0;
        for (Integer site : sites) {
            byte b = seq.getState(alphabet, site - 1);

            state[i++] = b;
        }

        return new Signature(state);
    }

    public SequenceAlphabet getAlphabet() {
        return alphabet;
    }

    public Set<Integer> getSites() {
        return sites;
    }

}
