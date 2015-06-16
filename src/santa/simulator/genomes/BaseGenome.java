package santa.simulator.genomes;

import santa.simulator.fitness.FitnessFunction;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: BaseGenome.java,v 1.5 2006/07/19 12:53:05 kdforc0 Exp $
 */
public abstract class BaseGenome implements Genome {

	public BaseGenome() {
		this.fitnessCache = null;
		this.descriptor = GenomeDescription.root;
	}

	public int getTotalMutationCount() {
		return totalMutationCount;
	}

	public void setTotalMutationCount(int totalMutationCount) {
		this.totalMutationCount = totalMutationCount;
	}

	protected void incrementTotalMutationCount() {
		totalMutationCount++;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public void incrementFrequency() {
		frequency++;
	}

	public double getLogFitness() {
		return logFitness;
	}

	public void setLogFitness(double logFitness) {
		if (logFitness != this.logFitness) {
			this.logFitness = logFitness;
			fitnessKnown = false;
		}
	}

	public double getFitness() {
		if (!fitnessKnown) {
			fitness = Math.exp(logFitness);
			fitnessKnown = true;
		}
		return fitness;
	}

	public FitnessFunction.FitnessGenomeCache getFitnessCache() {
		return fitnessCache;
	}

	public void setFitnessCache(FitnessFunction.FitnessGenomeCache fitnessCache) {
		this.fitnessCache = fitnessCache;
	}

	public byte[] getNucleotides(Feature feature) {
		byte[] nucleotides = new byte[feature.getNucleotideLength()];
		int k = 0;
		for (int i = 0; i < feature.getFragmentCount(); i++) {
			int start = feature.getFragmentStart(i);
			int finish = feature.getFragmentFinish(i);
			if (start < finish) {
				for (int j = start; j <= finish; j++) {
					nucleotides[k] = getNucleotide(j);
					k++;
				}
			} else {
				for (int j = finish; j >= start; j--) {
					nucleotides[k] = getNucleotide(j);
					k++;
				}
			}
		}

		return nucleotides;
	}

	public byte[] getStates(Feature feature) {
		Sequence seq = new SimpleSequence(getNucleotides(feature));
		return seq.getStates(feature.getAlphabet());
	}



	/**
	 * Compute feature-specific, mutation-induced changes.
	 * 
	 * Each change is a nucleotide pair (previous and current)
	 * along with a position. If the feature uses the AMINO_ACID
	 * alphabet, then the changes will be amino acids instead of
	 * nucleotides.
	 *
	 * These changes will be used to compute a change in fitness value (rather than recomputing the fitness from scratch).
	 * In fact, only subclasses of AbstractSiteFitnessFactor (which is only PurifyingFitness today) actually do anything with this information.
	 * Other fitness functions ignore the data computed here.  
	 *
	 * @param feature Feature object over which the changes should be computed.
	 * @param mutations set of mutations that are inducing changes.
	 * @return list of <StateChange> objects
	 **/
	public List<StateChange> getChanges(Feature feature, SortedSet<Mutation> mutations) {
		List<StateChange> changes = new ArrayList<StateChange>();

		feature = descriptor.getFeature(feature.getName());
		assert(feature != null);
		
		/* should pass in changes to this routine so we don't compute these multiple times. */
		int[] featureSiteTable = descriptor.getFeatureSiteTable(feature);
		for (Mutation m : mutations) {
			List<StateChange> c = m.getChanges(this, featureSiteTable);
			changes.addAll(c);
		}


		if (feature.getAlphabet() == SequenceAlphabet.AMINO_ACIDS) {
			int[] genomeSiteTable = descriptor.getGenomeSiteTable(feature);
			byte[] codon = new byte[3];
			int lastAA = -1;
			byte oldState = -1;

			List<StateChange> aaChanges = new ArrayList<StateChange>();

			for (StateChange change : changes) {
				int aa = change.position / 3;
				int cp = change.position % 3;

				if (aa != lastAA) {
					if (lastAA != -1) {
						// finish of the previous aa change...
						byte newState = AminoAcid.STANDARD_GENETIC_CODE[codon[0]][codon[1]][codon[2]];
						if (newState != oldState) {
							// don't include synonymous changes
							aaChanges.add(new StateChange(lastAA, oldState, newState));
                            
                            //System.err.println("aa: " + lastAA + " " + oldState + " " + newState);
						}
					}

					// and get on with a new one
					codon[0] = getNucleotide(genomeSiteTable[aa * 3]);
					codon[1] = getNucleotide(genomeSiteTable[aa * 3 + 1]);
					codon[2] = getNucleotide(genomeSiteTable[aa * 3 + 2]);

					oldState = AminoAcid.STANDARD_GENETIC_CODE[codon[0]][codon[1]][codon[2]];

					if (codon[cp] != change.oldState) {
						throw new RuntimeException("mismatch in state changes");
					}
					codon[cp] = change.newState;

					lastAA = aa;
				} else {
					if (codon[cp] != change.oldState) {
						throw new RuntimeException("mismatch in state changes");
					}
					codon[cp] = change.newState;
				}
			}

			if (lastAA != -1) {
				// finish of the last aa change...
				byte newState = AminoAcid.STANDARD_GENETIC_CODE[codon[0]][codon[1]][codon[2]];
				if (newState != oldState) {
					// don't include synonymous changes
					aaChanges.add(new StateChange(lastAA, oldState, newState));

                    //System.err.println("aa: " + lastAA + " " + oldState + " " + newState);
                }
			}

			return aaChanges;

		} else {
			return changes;
		}
	}


	public int binomialDeviate(double mutationRate) {
		return descriptor.binomialDeviate(mutationRate);
	}
	
	/**
	 * reference to an indel that forms one node in a tree of indel mutations.
	 * The tree can be navigated toward the root by getting the parent of the indel Node.
	 * The tree cannot be navigated toward the leaves.
	 */
	protected GenomeDescription descriptor;
	
	// private members

	private double logFitness = 0.0;
	private double fitness = 1.0;
	private boolean fitnessKnown = false;

	private int frequency = 0;
	private int totalMutationCount = 0;

	FitnessFunction.FitnessGenomeCache fitnessCache;
}
