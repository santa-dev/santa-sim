package santa.simulator.mutators;

import santa.simulator.genomes.Genome;
import santa.simulator.genomes.Mutation;

import java.util.SortedSet;

/**
 * @author Alexei Drummond
 * @author Andrew Rambaut
 */
public interface Mutator {
    /**
     * Creates a sorted set of mutations for the given genome. The given genome is not changed.
     * The mutations must be returned in positional order. A maximum of one mutation per
     * site should be returned.
     * @param genome
     * @return an array of Mutations
     */
    SortedSet<Mutation> mutate(Genome genome);
}
