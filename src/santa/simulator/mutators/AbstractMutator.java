package santa.simulator.mutators;

import java.util.SortedSet;
import java.util.TreeSet;

import santa.simulator.Random;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.Mutation;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public abstract class AbstractMutator implements Mutator {

    public AbstractMutator(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public SortedSet<Mutation> mutate(Genome genome) {
        // perhaps we could do this by working out the next mutated site
        // using an exponential distribution on the mutation rate and
        // iterate. This would avoid the sort at the end.
        SortedSet<Mutation> mutations = new TreeSet<Mutation>();
        
        int mutationCount = genome.binomialDeviate(mutationRate);

        // We expect only a few mutations per genome. Therefore, simply check
        // by looping over the already generated mutations to avoid duplicates hits.

        while (mutations.size() != mutationCount) {
        	int pos = 0;
        	if (genome.getLength() > 1)
        		pos = Random.nextInt(0, genome.getLength() - 1);

            Mutation mutation = Mutation.getMutation(pos, mutate(genome.getNucleotide(pos)));

            if (mutation.state != genome.getNucleotide(pos))
                mutations.add(mutation);
        }

        return mutations;
    }


    /**
     * Returns a new state given an existing state.
     * @param state the existing state
     * @return the new state
     */
    public abstract byte mutate(byte state);

    protected final double mutationRate;
}
