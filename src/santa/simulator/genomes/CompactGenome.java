
package santa.simulator.genomes;

import java.util.SortedSet;
import java.util.TreeSet;
import santa.simulator.NotImplementedException;

/**
 * @author Andrew Rambaut
 */
public class CompactGenome extends BaseGenome {

    /**
     * This is an empty constructor - duplicate() will be called next to duplicate
     * an existing genome.
     */
    public CompactGenome() {

	}

    /**
     * This constructor takes an initial sequence. This will only be used
     * for setting up the initial population.
     * @param sequence
     */
    public CompactGenome(Sequence sequence) {
        mutations = new TreeSet<Mutation>();

        if (masterSequence == null) {
            throw new IllegalArgumentException("The master sequence has not been set");
        } else {
            if (sequence.getLength() != masterSequence.getLength()) {
                throw new IllegalArgumentException("Initializing sequence length is different to the master sequence");
            }

            for (int i = 0; i < sequence.getLength(); i++) {
                if (sequence.getNucleotide(i) != masterSequence.getNucleotide(i)) {
                    mutations.add(Mutation.getMutation(i, sequence.getNucleotide(i)));
                }
            }
        }
    }

    public void duplicate(CompactGenome source) {
        setTotalMutationCount(source.getTotalMutationCount());
        mutations = source.mutations;
        setLogFitness(source.getLogFitness());
    }

    public void deleteSubSequence(int pos, int count) {
    	throw new NotImplementedException();
    }
    public void insertSequence(int pos, Sequence s) {
    	throw new NotImplementedException();
    }
    	    
    public int getLength() {
        return masterSequence.getLength();
    }

   /**
     * Gets the state at a given position. If no mutation exists then the masterSequence state is returned.
     * This involves a bisection search of the mutation list so may be less efficient than a getFirstState/
     * getNextState iteration.
     * @param position the position in the sequence
     * @return the state
     */
    public byte getNucleotide(int position) {
        Mutation mutation = getMutation(position);

        if (mutation != null) {
            return mutation.state;
        }

        return masterSequence.getNucleotide(position);
    }

    public Mutation getMutation(int position) {
        Mutation m = Mutation.getMutation(position, Nucleotide.A);

        SortedSet<Mutation> find = mutations.tailSet(m);
        if (!find.isEmpty() && (find.first().position == position)) {
            return find.first();
        } else
            return null;
    }

    public SortedSet<Mutation> getMutations() {
        return mutations;
    }

    /**
     * Apply an array of mutations to the genome. The new mutation array may not have the mutations
     * in positional order. The mutation array for the genome (mutations from the master sequence)
     * must be in positional order.
     * @param newMutations the array of new mutations in positional order
     */
	public void applyMutations(SortedSet<Mutation> newMutations) {
        for (Mutation m : newMutations) {

            Mutation current = getMutation(m.position);

            if (current != null) {
                if (current.state != m.state) {
                    mutations.remove(current);
                    if (m.state != masterSequence.getNucleotide(m.position)) {
                        mutations.add(m);
                    }
                }
            } else {
                if (m.state != masterSequence.getNucleotide(m.position))
                    mutations.add(m);
            }
        }
    }

    void changeMasterSequence(Sequence newMasterSequence) {
        TreeSet<Mutation> newMutations = new TreeSet<Mutation>();

        for (int position = 0; position < newMasterSequence.getLength(); position++) {
            byte state = getNucleotide(position);

            if (state != newMasterSequence.getNucleotide(position)) {
                newMutations.add(Mutation.getMutation(position, state));
            }
        }

        this.mutations = newMutations;
    }

    /**
     * Gets a byte array representing the entire sequence. If the genome stores
     * differences rather than a complete sequence, then this may be an inefficient
     * way of accessing each state.
     *
     * @return a byte array containing sequence states
     */
	public Sequence getSequence() {
        SimpleSequence s = new SimpleSequence(masterSequence);

		for (Mutation mutation : mutations) {
			s.setNucleotide(mutation.position, mutation.state);
		}

        return s;
	}
	
	
	public boolean substitute(int position, byte state) {
		throw new NotImplementedException();
	}


	public boolean delete(int position, int count) {
		throw new NotImplementedException();
	}


	public boolean insert(int position, SimpleSequence seq) {
		throw new NotImplementedException();
	}

    // private members
    private SortedSet<Mutation> mutations = null;

    // static members
    public Sequence getMasterSequence() {
        return masterSequence;
    }

    static void setMasterSequence(Sequence masterSequence) {
        CompactGenome.masterSequence = masterSequence;
    }

    private static Sequence masterSequence = null;
    
}
