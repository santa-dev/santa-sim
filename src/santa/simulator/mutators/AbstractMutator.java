package santa.simulator.mutators;

import santa.simulator.Random;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.DistributionFactory;
import santa.simulator.genomes.*;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public abstract class AbstractMutator implements Mutator {

    public AbstractMutator(double mutationRate) {
        this.mutationRate = mutationRate;

        preCalculateBinomial(GenomeDescription.getGenomeLength(), mutationRate);
    }

    public SortedSet<Mutation> mutate(Genome genome) {
        // perhaps we could do this by working out the next mutated site
        // using an exponential distribution on the mutation rate and
        // iterate. This would avoid the sort at the end.

        int mutationCount = binomialDeviate();

        // We expect only a few mutations per genome. Therefore, simply check
        // by looping over the already generated mutations to avoid duplicates hits.

        SortedSet<Mutation> mutations = new TreeSet<Mutation>();

        while (mutations.size() != mutationCount) {
            int pos = Random.nextInt(0, genome.getLength() - 1);

            Mutation mutation = Mutation.getMutation(pos, mutate(genome.getNucleotide(pos)));

            mutations.add(mutation);
        }

        return mutations;
    }

    public SortedSet<Mutation> new_mutate(Genome genome) {

        SortedSet<Mutation> mutations = new TreeSet<Mutation>();

        for (int pos = 0; pos < genome.getLength(); pos++) {
            double r = santa.simulator.Random.nextUniform(0.0, 1.0);
            if (r < mutationRate) {
                byte newState = mutate(genome.getNucleotide(pos));
                mutations.add(Mutation.getMutation(pos, newState));
            }
        }


        return mutations;
    }

    protected int binomialDeviate() {
        double r = santa.simulator.Random.nextUniform(0.0, 1.0);
        for (int j = 0; j < binomial.length; j++) {
            if (r < binomial[j]) {
                return j;
            }
        }
        return binomial.length;
    }

    protected void preCalculateBinomial(int numExperiments, double eventRate) {
        binomial = new double[(int)(numExperiments * eventRate * 100)];

        BinomialDistribution distr
            = DistributionFactory.newInstance()
              .createBinomialDistribution(numExperiments, eventRate);

        for (int j = 0; j < binomial.length; ++j) {
            try {
                binomial[j] = distr.cumulativeProbability(j);
            } catch (MathException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns a new state given an existing state.
     * @param state the existing state
     * @return the new state
     */
    public abstract byte mutate(byte state);

    protected final double mutationRate;
    private double[] binomial;
}