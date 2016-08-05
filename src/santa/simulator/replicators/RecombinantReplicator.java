package santa.simulator.replicators;

import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.*;

import org.apache.commons.math3.distribution.BinomialDistribution;

import santa.simulator.EventLogger;
import santa.simulator.Random;
import santa.simulator.Virus;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.GenePool;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Mutation;
import santa.simulator.genomes.Sequence;
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
		Logger logger = Logger.getLogger("santa.simulator.replicators");


        if (Random.nextUniform(0.0, 1.0) < dualInfectionProbability * recombinationProbability) {

            // dual infection and recombination
			List<Genome> parents = Arrays.asList(vparents).stream().map(Virus::getGenome).collect(Collectors.toList());

			// sort the parents by increasing genome length
			parents.sort((p1, p2) -> p1.getLength() - p2.getLength());

			// get minimum length of the parents
			int length = parents.stream().map(g -> g.getLength()).reduce(Integer::min).get() - 1 ;

			// pick number of breakpoints
			BinomialDistribution binomialDeviate = new BinomialDistribution(Random.randomData.getRandomGenerator(), length, recombinationProbability);
			int nbreaks = binomialDeviate.sample();
			
			// Then draw the positions.
			// Don't repeat a breakpoint, and only break at codon boundaries.
			SortedSet<Integer> breakPoints = new TreeSet<Integer>();
			for (int i = 0; i < nbreaks; i++) {
				int bp = Random.nextInt(1, length);
				if (bp % 3 == 0) {
					breakPoints.add(bp);
				}
			}

			logger.finest("recombination: " + breakPoints.size() + "@" + breakPoints);

			// create the recombinant genome description
			GenomeDescription[] gd_parents = parents.stream().map(Genome::getDescription).toArray(GenomeDescription[]::new);
;
			GenomeDescription recombinantGenome = GenomeDescription.recombine(gd_parents, breakPoints);
			
			Sequence recombinantSequence = getRecombinantSequence(parents, breakPoints);

			Genome genome = genePool.createGenome(recombinantSequence, recombinantGenome);
			
	        SortedSet<Mutation> mutations = mutator.mutate(genome);

	        genome.setFrequency(1);

	        genome.applyMutations(mutations);

	        // we can't just update some of the fitness so recompute...
	        fitnessFunction.computeLogFitness(genome);

            virus.setGenome(genome);
            virus.setParent(vparents[0]);
			
            String fitnessStr = parents.stream().map(Genome::getLogFitness).map(Object::toString).collect(Collectors.joining(", "));
            EventLogger.log("Recombination: (" + fitnessStr + ") -> " + genome.getLogFitness());

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
    private static Sequence getRecombinantSequence(List<Genome> parents, SortedSet<Integer> breakPoints) {
		return parents.stream().map(Genome::getSequence).reduce((s1, s2) -> s1.recombineWith(s2, breakPoints)).get();
	}


    private final double dualInfectionProbability;
    private final double recombinationProbability;
}
