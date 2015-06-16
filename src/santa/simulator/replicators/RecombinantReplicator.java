package santa.simulator.replicators;

import java.util.Arrays;
import java.util.SortedSet;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.distribution.BinomialDistribution;

import santa.simulator.EventLogger;
import santa.simulator.Random;
import santa.simulator.NotImplementedException;
import santa.simulator.Virus;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Mutation;
import santa.simulator.genomes.Sequence;
import santa.simulator.genomes.SimpleSequence;
import santa.simulator.mutators.Mutator;

/**
 * @author rambaut
 *         Date: Apr 27, 2005
 *         Time: 9:40:33 AM
 */
public class RecombinantReplicator implements Replicator {

    public RecombinantReplicator(double dualInfectionProbability, double recombinationProbability) {
        this.dualInfectionProbability = dualInfectionProbability;
        this.recombinationProbability = recombinationProbability;
    }

	public int getParentCount() {
		return 2;
	}

    public void replicate(Virus virus, Virus[] parents, Mutator mutator, FitnessFunction fitnessFunction, GenePool genePool) {

		if (true) {
			// need to generate a new genome descrioption for the recombinate genome that may be a different length than either parent.
			// It is likely that creating a new binomial RNG for each recombination will be too slow.  Need to find a way to cache and reuse the RNG.

			throw new NotImplementedException();
		}

		
        if (Random.nextUniform(0.0, 1.0) < dualInfectionProbability * recombinationProbability) {
            // dual infection and recombination

            Genome parent1Genome = parents[0].getGenome();
            Genome parent2Genome = parents[1].getGenome();

            Sequence recombinantSequence = getRecombinantSequence(parent1Genome, parent2Genome);

            Genome genome = genePool.createGenome(recombinantSequence);

	        SortedSet<Mutation> mutations = mutator.mutate(genome);

	        genome.setFrequency(1);

	        genome.applyMutations(mutations);

	        // we can't just update some of the fitness so recompute...
	        fitnessFunction.computeLogFitness(genome);

            virus.setGenome(genome);
            virus.setParent(parents[0]);

            EventLogger.log("Recombination: (" + parent1Genome.getLogFitness() + ", " + parent2Genome.getLogFitness() + ") -> " + genome.getLogFitness());

        } else {
            // single infection - no recombination...
            Genome parentGenome = parents[0].getGenome();

            SortedSet<Mutation> mutations = mutator.mutate(parentGenome);

            Genome genome = genePool.duplicateGenome(parentGenome, mutations, fitnessFunction);

            virus.setGenome(genome);
            virus.setParent(parents[0]);
        }

    }

    private Sequence getRecombinantSequence(Genome parent1Genome, Genome parent2Genome) {

        // First draw the number of break points
		int length = Math.min(parent1Genome.getLength() - 1, parent2Genome.getLength() - 1);
		BinomialDistribution binomialDeviate = new BinomialDistribution(length, recombinationProbability);
        int n = binomialDeviate.sample();
        int[] breakPoints = new int[n];

        // Then draw the positions
        for (int i = 0; i < breakPoints.length; i++) {
            breakPoints[i] = Random.nextInt(1, length);
        }
        Arrays.sort(breakPoints);

        // now create the recombinant by getting the list of mutations for
        // the recombinant segments donated by the second parent.

        int lastBreakPoint = 0;
        int currentGenome = 0;

	    SimpleSequence recombinantSequence = new SimpleSequence(parent1Genome.getSequence());
        for (int i = 0; i < breakPoints.length; i++) {
            if (currentGenome == 1) {
                // If this segment is given by the second parent...
                for (int j = lastBreakPoint; j < breakPoints[i]; j++) {
                    recombinantSequence.setNucleotide(j, parent2Genome.getNucleotide(j));
                }
            }

            lastBreakPoint = breakPoints[i];
            currentGenome = 1 - currentGenome;
        }

        return recombinantSequence;
    }

    private final double dualInfectionProbability;
    private final double recombinationProbability;
}
