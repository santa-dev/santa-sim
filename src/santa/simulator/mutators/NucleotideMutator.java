package santa.simulator.mutators;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.*;

import santa.simulator.Random;
import santa.simulator.IndelModel;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.Mutation;
import santa.simulator.genomes.Insertion;
import santa.simulator.genomes.Deletion;
import santa.simulator.genomes.SimpleSequence;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class NucleotideMutator extends AbstractMutator {

    /**
     * Constructor
     *
     * @param mutationRate mutation rate per nucleotide site
     * @param transitionBias probability of a transition
     */
    public NucleotideMutator(double mutationRate, double transitionBias, double[] rateBiases,  double insertProb, double deleteProb, IndelModel indelModel) {

        super(mutationRate);

        this.rateBiases = rateBiases;
		this.indelModel = indelModel;
		this.insertProb = insertProb;
		this.deleteProb = deleteProb;

        if (rateBiases != null) {
            ti = 0;
            tv = 0;

            rateBiasMatrix = new double[4][4];
            double maxFromRate = 0;
            int i = 0;
            for (int from = 0; from < 4; ++from) {
                double fromRate = 0;
                for (int to = 0; to < 4; ++to) {
                    if (from != to)
                        fromRate += rateBiases[i++];
                }
                if (fromRate > maxFromRate)
                    maxFromRate = fromRate;
            }

            i = 0;
            for (int from = 0; from < 4; ++from) {
                for (int to = 0; to < 4; ++to) {
                    if (from != to) {
                        rateBiasMatrix[from][to] = rateBiases[i++] / maxFromRate;
                    }
                }

                /* convert probabilities to cumulative probabilities,
                 * starting right after the from state, and wrapping
                 * around, ending with '1' in the from state, which means
                 * no mutation */
                double fromRate = 0;
                for (int k = 1; k < 4; ++k) {
                    int to = (from + k) % 4;
                    fromRate += rateBiasMatrix[from][to];
                    rateBiasMatrix[from][to] = fromRate;
                }

                rateBiasMatrix[from][from] = 1;
            }
        } else {
            ti = transitionBias / (transitionBias + 2.0);
            tv = (1.0 - ti) / 2.0;
        }
    }

    /**
     * Returns a new state given an existing state.
     *
     * @param state the existing state
     * @return the new state
     */
    public byte mutate(byte state) {
        if (rateBiases == null) {
           double p = Random.nextUniform(0, 1.0);

           if (p < tv) {
                // is the first transversion: A->C, C->G, T->A
                return (byte)((state + 1) % 4);
            } else if (p < (tv + ti)) {
                // is the transition: A->G, C->T, G->A, T->C
                return (byte)((state + 2) % 4);
            } else {
                // is the second transversion: A->T, C->A, G->C, T->G
                return (byte)((state + 3) % 4);
            }
        } else {
            // the bias reflects the chance of generating a mis-incorporation
            // of a wrong nucleotide given the right nucleotide
            double p = santa.simulator.Random.nextUniform(0, 1.0);

            for (int i = 0; i < 4; ++i) {
                int to = (state + 1 + i) % 4;
                if (p < rateBiasMatrix[state][to])
                    return (byte)to;
            }

            throw new RuntimeException("RateBiasMatrix is corrupt!");
        }
    }


	@Override public SortedSet<Mutation> mutate(Genome genome) {
		Logger mutlogger = Logger.getLogger("santa.simulator.mutators");
		SortedSet<Mutation> mutations;

		// total probability space, { insertion, deletion, neither }
		// probability of getting an insertion *or* deletion.
		double indelProb = insertProb + deleteProb;

		// sample from uniform distribution over total probability.
		double r = santa.simulator.Random.nextUniform(0.0, 2.0);

		
		if (r < indelProb) {
			mutations = new TreeSet<Mutation>();
			// Doing an indel....
			if (r < insertProb) {	// Insertion!
				int count = indelModel.nextLength();				// insertion length

				// only allow indels that preserve reading frame.
				// Filter out any indel that is not a multiple of three nucleotides.
				if (count != 0 && (count % 3) == 0) {
					// what is it filled with?
					// generate a random sequence of the appropriate length
					// See Issue #2 - https://github.com/matsengrp/santa-sim/issues/2
					byte states[] = new byte[count];
					for (int i = 0; i < count; i++) {
						states[i] = (byte) Random.nextInt(0,  3);
					}
					SimpleSequence seq = new SimpleSequence(states);
					int pos = Random.nextInt(0, genome.getLength());	// start position
					mutlogger.finest("insert: " + count + "@" + pos + " on len " + genome.getLength());
					mutations.add(new Insertion(pos, seq));
				}
			} else {	// Deletion!
				int pos = 0;	// start position
				if (genome.getLength() > 1)
					pos = Random.nextInt(0, genome.getLength() - 1);

				int count = indelModel.nextLength(); // deletion length
				if (count != 0 && (count % 3) == 0) {
					mutlogger.finest("delete: " + count + "@" + pos + " on len " + genome.getLength());
					mutations.add(new Deletion(pos, count));
				}
			}
		} else {
			// doing a substitution
			mutations = super.mutate(genome);
		}
		return mutations;
    }

	

    private final double ti, tv;
    private double[] rateBiases;
    private double[][] rateBiasMatrix;
    private IndelModel indelModel;
    private double insertProb;
	private double deleteProb;

}
