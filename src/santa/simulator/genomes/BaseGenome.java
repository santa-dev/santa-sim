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

	public List<StateChange> getChanges(Feature feature, SortedSet<Mutation> mutations) {
		List<StateChange> changes = new ArrayList<StateChange>();
		int[] featureSiteTable = GenomeDescription.getFeatureSiteTable(feature);

		for (Mutation m : mutations) {
			// convert the mutations into a list of state changes for the
			byte oldState = getNucleotide(m.position);
			StateChange c = new StateChange(featureSiteTable[m.position], oldState, m.state);
			if(c.position >= 0) //-1 => mutation outside of feature
				changes.add(c);
            
            //System.err.println("m: " + m.position + " " + m.state + " -> " + featureSiteTable[m.position] + " " + oldState + " " + m.state);
		}

		if (feature.getAlphabet() == SequenceAlphabet.AMINO_ACIDS) {
			int[] genomeSiteTable = GenomeDescription.getGenomeSiteTable(feature);
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
	

	// private members

	private double logFitness = 0.0;
	private double fitness = 1.0;
	private boolean fitnessKnown = false;

	private int frequency = 0;
	private int totalMutationCount = 0;

	FitnessFunction.FitnessGenomeCache fitnessCache;
}
