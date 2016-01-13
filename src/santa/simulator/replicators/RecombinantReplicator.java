package santa.simulator.replicators;

import java.util.Arrays;
import java.util.Comparator;
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

    public void replicate(Virus virus, Virus[] vparents, Mutator mutator, FitnessFunction fitnessFunction, GenePool genePool) {

        if (Random.nextUniform(0.0, 1.0) < dualInfectionProbability * recombinationProbability) {
            // dual infection and recombination
			Genome[] parents = { vparents[0].getGenome(), vparents[1].getGenome() };

			// sort the parents by increasing genome length
			Arrays.sort(parents, new Comparator<Genome>() {
				@Override
				public int compare(Genome g1, Genome g2) {
					return(g1.getLength() - g2.getLength());
				}
			});
			assert(parents[0].getLength() <= parents[1].getLength());
			
			int length = Math.min(parents[0].getLength() - 1, parents[1].getLength() - 1);
			BinomialDistribution binomialDeviate = new BinomialDistribution(length, recombinationProbability);
			int n = 1; // binomialDeviate.sample();
			int[] breakPoints = new int[n];
			
			// Then draw the positions
			for (int i = 0; i < breakPoints.length; i++) {
				breakPoints[i] = 100; // Random.nextInt(1, length);
			}
			Arrays.sort(breakPoints);

			// create the recombinant genome description
			GenomeDescription[] gd_parents = { parents[0].getDescription(), parents[1].getDescription() };
			GenomeDescription gd_recomb = GenomeDescription.recombine(gd_parents, breakPoints);
			
			Sequence recombinantSequence = getRecombinantSequence(parents, breakPoints, gd_recomb.getGenomeLength());

			Genome genome = genePool.createGenome(recombinantSequence, gd_recomb);
			
	        SortedSet<Mutation> mutations = mutator.mutate(genome);

	        genome.setFrequency(1);

	        genome.applyMutations(mutations);

	        // we can't just update some of the fitness so recompute...
	        fitnessFunction.computeLogFitness(genome);

            virus.setGenome(genome);
            virus.setParent(vparents[0]);
			
            EventLogger.log("Recombination: (" + parents[0].getLogFitness() + ", " + parents[1].getLogFitness() + ") -> " + genome.getLogFitness());

        } else {
            // single infection - no recombination...
            Genome parentGenome = vparents[0].getGenome();

            SortedSet<Mutation> mutations = mutator.mutate(parentGenome);

            Genome genome = genePool.duplicateGenome(parentGenome, mutations, fitnessFunction);

            virus.setGenome(genome);
            virus.setParent(vparents[0]);
        }

    }


	/**
	 * Create a recombined nucleotide sequence from two parents.
	 *
	 * Given a pair of parent genomes and a set of breakpoints, create
	 * a new sequence that is a combination of fragments from both
	 * parents.  breakPoints describes the positions at which we
	 * switch from one template to the other.  

	 * 'len' is the length of the recombined sequence.  If
	 * 'breakPoints' is empty, this routine simply copies the sequence
	 * from first genome in 'parents'.
	 **/
    private Sequence getRecombinantSequence(Genome[] parents, int[] breakPoints, int len) {
		assert(parents.length == 2);

		int lastBreakPoint = 0;
		int currentGenome = 0;
		Genome gd = parents[currentGenome];
		SimpleSequence recombinantSequence = new SimpleSequence(len);
		
		for (int nextBreakPoint : breakPoints) {
			for (int j = lastBreakPoint; j < nextBreakPoint; j++) {
				recombinantSequence.setNucleotide(j, gd.getNucleotide(j));
			}
			lastBreakPoint = nextBreakPoint;
			currentGenome = 1 - currentGenome;
			gd = parents[currentGenome];
		}
		int nextBreakPoint =  gd.getLength();
		for (int j = lastBreakPoint; j < nextBreakPoint; j++) {
			recombinantSequence.setNucleotide(j, gd.getNucleotide(j));
		}
		return(recombinantSequence);
	}
	
    private final double dualInfectionProbability;
    private final double recombinationProbability;
}
