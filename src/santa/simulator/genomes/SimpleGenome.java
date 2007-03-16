package santa.simulator.genomes;

import java.util.SortedSet;

import santa.simulator.fitness.FitnessFunction;


/**
 * @author Andrew Rambaut
 */
public class SimpleGenome extends BaseGenome {

    public SimpleGenome() {
    }

    public void duplicate(SimpleGenome source) {
        this.sequence = new SimpleSequence(source.sequence);
        this.fitnessCache = source.fitnessCache.clone();
        setLogFitness(source.getLogFitness());
    }

    /**
     * Apply an array of mutations to the genome. The new mutation array may not have the mutations
     * in positional order. The mutation array for the genome (mutations from the master sequence)
     * must be in positional order.
     * @param newMutations the array of new mutations in positional order
     * @param fitnessFunction
     */
    public void applyMutations(SortedSet<Mutation> newMutations, FitnessFunction fitnessFunction) {
        boolean potentiallyHasFailed = false;

        for (Mutation mutation : newMutations) {
            byte oldState = sequence.getNucleotide(mutation.position);

            if (mutation.state != oldState) {
                incrementTotalMutationCount();

                if (!potentiallyHasFailed && fitnessFunction.updateLogFitness(this, mutation) == Double.NEGATIVE_INFINITY) {
                    // If the fitness has changed to -Inf (perhaps because this mutation has induced a stop codon) then
                    // a future mutation may reverse it if it is in the same codon. However, you can't subtract the
                    // -Inf so we flag this situation and then recalculate the entire fitness when all the mutations have
                    // been applied.
                    potentiallyHasFailed = true;
                }

                sequence.setNucleotide(mutation.position, mutation.state);
            }
        }

        if (potentiallyHasFailed) {
            fitnessFunction.computeLogFitness(this);
        }

    }

    public int getLength() {
        return sequence.getLength();
    }

    public byte getNucleotide(int site) {
        return sequence.getNucleotide(site);
    }

    public void setSequence(Sequence sequence) {
        this.sequence = new SimpleSequence(sequence);
    }

    /**
     * Gets a byte array representing the entire sequence. If the genome stores
     * differences rather than a complete sequence, then this may be an inefficient
     * way of accessing each state.
     *
     * @return a byte array containing sequence states
     */
    public Sequence getSequence() {
        return sequence;
    }

    // private members

    private SimpleSequence sequence = null;

}