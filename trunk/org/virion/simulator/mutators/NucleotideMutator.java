package org.virion.simulator.mutators;

import org.virion.simulator.Random;
import org.virion.simulator.genomes.GenomeDescription;
import org.virion.simulator.genomes.Mutation;
import org.virion.simulator.genomes.Genome;

import java.util.SortedSet;
import java.util.Set;

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
    public NucleotideMutator(double mutationRate, double transitionBias, double[] rateBiases) {

        super(mutationRate);

        this.rateBiases = rateBiases;

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
            double p = org.virion.simulator.Random.nextUniform(0, 1.0);

            for (int i = 0; i < 4; ++i) {
                int to = (state + 1 + i) % 4;
                if (p < rateBiasMatrix[state][to])
                    return (byte)to;
            }

            throw new RuntimeException("RateBiasMatrix is corrupt!");
        }
    }

    private final double ti, tv;
    private double[] rateBiases;
    private double[][] rateBiasMatrix;

}