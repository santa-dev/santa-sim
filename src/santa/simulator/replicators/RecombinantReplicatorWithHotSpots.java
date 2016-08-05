package santa.simulator.replicators;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.ArrayList;

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
import santa.simulator.genomes.SimpleSequence;
import santa.simulator.genomes.RecombinationHotSpot;
import santa.simulator.mutators.Mutator;
/**
 * @author Abbas Jariani
 * @author Andrew Rambaut
 * @version $Id$
 */
/*
 * Abbas:
 * The regions defined as hot spots will be more likely to contain breakpoints per segment with the boost factor defined.
 */
public class RecombinantReplicatorWithHotSpots implements Replicator {
	
    public RecombinantReplicatorWithHotSpots(double dualInfectionProbability, double recombinationProbability) {
        this.dualInfectionProbability = dualInfectionProbability;
        this.recombinationProbability = recombinationProbability;
        this.recombinationHotSpots = GenomeDescription.getHotSpots();
    }

	public int getParentCount() {
		return 2;
	}
	
	public void replicate(Virus virus, Virus[] parents, Mutator mutator,
			FitnessFunction fitnessFunction, GenePool genePool) {
		
        if (Random.nextUniform(0.0, 1.0) < dualInfectionProbability * recombinationProbability) {
            // dual infection and recombination
            Genome parent1Genome = parents[0].getGenome();
            Genome parent2Genome = parents[1].getGenome();

			if (parent1Genome.getDescription() !=  parent2Genome.getDescription()) {
				// Mismatched GenomeDescriptions is a sign that indels
				// support is turned on.  Can't use Recombination Hot
				// Spots at the same time.  Hotspot definition depends
				// on feature position, which might move under indels.
				//
				// This would be better reported at parse time but is
				// difficult to detect then.
				throw new RuntimeException("Cannot use recombination hotspots with non-zero indel probabilities.");

			}
			
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
	//Logic: relative probability of having break point in a (hot) segment = (length of hot segment) * (probability boost factor) / (genome length)
	//Then we normalize relative probabilities so they sum up to unity.
    private Sequence getRecombinantSequence(Genome parent1Genome, Genome parent2Genome) {
		assert(parent1Genome.getLength() == parent2Genome.getLength());
		assert(parent1Genome.getDescription() == parent2Genome.getDescription());
		

		int length = parent1Genome.getLength() - 1;
        int[] breakPoints = getBreakPoints(length);
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
	
    private int[] getBreakPoints(int length) {
		
		BinomialDistribution binomialDeviate = new BinomialDistribution(Random.randomData.getRandomGenerator(), length, recombinationProbability);
        int n = binomialDeviate.sample();
        int[] breakPoints = new int[n];
    	int nHotSegments = recombinationHotSpots.size();
        //Array containing start and end positions of hot segments
        int[] startPoints = new int[nHotSegments+1];
        int[] endPoints = new int[nHotSegments+1];
        startPoints[nHotSegments] = length;
        endPoints[nHotSegments] = length;
        for (int i = 0; i< nHotSegments;i++){
        	startPoints[i] = recombinationHotSpots.get(i).startPosition;
        	endPoints[i] = recombinationHotSpots.get(i).endPosition;
       	}
        Arrays.sort(startPoints);
        Arrays.sort(endPoints);
        int nonHotSegmentsCount = nHotSegments+1;
    	if (startPoints[0] == 0){
    		nonHotSegmentsCount -= 1;
    	}
    	if (endPoints[nHotSegments-1] == length) {
    		nonHotSegmentsCount -= 1;
    	}
    	//Assigning length to non-hot segments:
    	int[] nonHotStartPoints = new int[nonHotSegmentsCount];
    	int[] nonHotEndPoints = new int[nonHotSegmentsCount];
    	int nextHotSegmentIndex = 0;
    	if(startPoints[0] != 0){
    		nonHotStartPoints[0] = 0;
    		nonHotEndPoints[0] = startPoints[0] -1 ;
    	}
    	else {
    		nextHotSegmentIndex += 1;
    		nonHotStartPoints[0] = endPoints[0] + 1;
    		nonHotEndPoints[0] = startPoints[1] -1 ;
    	}
    	if (endPoints[nHotSegments-1] != length) {
    		nonHotStartPoints[nonHotSegmentsCount-1] = endPoints[nHotSegments-1]+1;
    		nonHotEndPoints[nonHotSegmentsCount-1] = length;
    	}
    	else {
    		nonHotStartPoints[nonHotSegmentsCount-1] = endPoints[nHotSegments-2]+1;
    		nonHotEndPoints[nonHotSegmentsCount-1] = startPoints[nHotSegments-1]-1;
    	}
    	for (int i = 1; i < nonHotSegmentsCount-1; i++) {
    		nonHotStartPoints[i] = endPoints[nextHotSegmentIndex]+1;
    			nonHotEndPoints[i] = startPoints[nextHotSegmentIndex+1]-1;
    		if (nextHotSegmentIndex < nonHotSegmentsCount -1)
    			nextHotSegmentIndex += 1;   	
    	}
        int overallSegmentsCount = nonHotSegmentsCount + nHotSegments;
        //The following array would contain probability of containing breakpoints at each segment: Hot segments + normal segments
        double[]  segmentRelativeProbabilities = new double[overallSegmentsCount];
        double[]  segmentProbabilities = new double[overallSegmentsCount];
        double[] cumulativeSegmentProbabilities = new double[overallSegmentsCount];
        Integer overallHotSegmentsLength = 0;
        Integer segmentLength;
        double relativeProbabilitiesSum = 0;
        double boostFactor;    	
        for (int i = 0 ; i < nHotSegments ; i++ ) {
        	segmentLength =  recombinationHotSpots.get(i).endPosition -recombinationHotSpots.get(i).startPosition+1;        	
        	overallHotSegmentsLength += segmentLength;
        	boostFactor = recombinationHotSpots.get(i).probBoost;
        	segmentRelativeProbabilities[i] = segmentLength * boostFactor /length ;
        	relativeProbabilitiesSum += segmentRelativeProbabilities[i]; 
        }
        for (int i =0 ; i< overallSegmentsCount- nHotSegments ; i++) {
        	segmentLength = nonHotEndPoints[i] - nonHotStartPoints[i] + 1;
        	segmentRelativeProbabilities[i+nHotSegments] =  (double)segmentLength / (double)length ;
        	relativeProbabilitiesSum += segmentRelativeProbabilities[i+nHotSegments];
    	}
        int[] nBreakPointsPerSegment = new int[overallSegmentsCount];
    	//Getting absolute & cumulative probabilities:
    	cumulativeSegmentProbabilities[0] = 0; 
    	for (int i = 0 ; i < overallSegmentsCount ; i++ ){
    		segmentProbabilities[i] = segmentRelativeProbabilities[i] / relativeProbabilitiesSum;
    		
    		if(i == 0) {
    			cumulativeSegmentProbabilities[i] = segmentProbabilities[i]; 
    		}
    		else {
    			cumulativeSegmentProbabilities[i] = cumulativeSegmentProbabilities[i-1] + segmentProbabilities[i]; 
    		}
			nBreakPointsPerSegment[i] = 0;
    	}
    	double r;
    	boolean set;
    	int sumNBreakPoints = 0;
    	for(int i = 0 ; i < n ; i++) {
    		r = santa.simulator.Random.nextUniform(0.0, 1.0);
    		set = false;
    		for(int j = 0; j< overallSegmentsCount; j++) {
    			if ((r < cumulativeSegmentProbabilities[j]) && set == false) {
        			nBreakPointsPerSegment[j] +=1;
        			sumNBreakPoints += 1;
        			set = true;
    			}
    			else {
    				//Do nothing, be cool
    			}	
    		}
    	}
    	if (sumNBreakPoints != n){
    		throw new RuntimeException("Error in Assignment of break point positions");
    	}
    	//Now assigning breakpoints within each hot segment:
    	int breakPointIndex = 0;
    	for (int i = 0; i < nHotSegments ; i++) {
    		
    		for (int j = 0; j< nBreakPointsPerSegment[i]; j++) {
    			breakPoints[breakPointIndex] = Random.nextSecureInt(recombinationHotSpots.get(i).startPosition, recombinationHotSpots.get(i).endPosition);
    			breakPointIndex += 1;
    		}
    	}
    	for (int i = 0; i < nonHotSegmentsCount ; i++) {
    		for(int j = 0; j<nBreakPointsPerSegment[i+nHotSegments];j++) {
    			breakPoints[breakPointIndex] = Random.nextInt(nonHotStartPoints[i], nonHotEndPoints[i]);
    			breakPointIndex += 1;
    		}
    	}
        Arrays.sort(breakPoints);
        
        return breakPoints;
    }
	
    private final double dualInfectionProbability;
    private final double recombinationProbability;
    private List<RecombinationHotSpot> recombinationHotSpots= new ArrayList<RecombinationHotSpot>();
}
