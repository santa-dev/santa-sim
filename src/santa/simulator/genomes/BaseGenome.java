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

	public void setDescription(GenomeDescription gd) {
		this.descriptor = gd;
		assert(this.descriptor.getGenomeLength() == getLength());
	}

	// REMIND - do we still need this?
	public GenomeDescription getDescription() {
		return descriptor;
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

	/**
	 * Retrieve a contiguous array of nucleotides corresponding to a feature.
	 *
	 * @return byte array of nucleotides (encoded as integer states)
	 **/
	public byte[] getNucleotides(Feature feature) {
		byte[] nucleotides = null;
		Feature f = descriptor.getFeature(feature.getName());
		if (f != null) {
			int sites[] = descriptor.getGenomeSiteTable(descriptor.getFeature(feature.getName()));
			nucleotides = new byte[sites.length];

			int k = 0;
			for (int i: sites) {
				nucleotides[k] = getNucleotide(i);
				k++;
			}
		}
		return nucleotides;
	}


	public byte[] getStates(Feature feature) {
		Sequence seq = new SimpleSequence(getNucleotides(feature));
		return seq.getStates(feature.getAlphabet());
	}



	/**
	 * Convert list of mutations to list of changes that affect a specific feature.
	 * 
	 * Return a list of `StateChange` objects,
	 * each of which has a nucleotide pair (previous and current) and
	 * a feature-relative position. If the feature uses the AMINO_ACID
	 * alphabet, then the changes will be amino acids instead of
	 * nucleotides.
	 *
	 * These changes will be used to compute a change in fitness value
	 * (rather than recomputing the fitness from scratch).  In
	 * reality, only subclasses of AbstractSiteFitnessFactor (which is
	 * only PurifyingFitness today) actually do anything with this
	 * information.  Other fitness functions ignore the data computed
	 * here.
	 *
	 * @param feature Feature object over which the changes should be computed.
	 * @param mutations set of mutations that are inducing changes.
	 * @return list of <StateChange> objects
	 **/
	public List<StateChange> getChanges(Feature featureByName, SortedSet<Mutation> mutations) {
		List<StateChange> changes = new ArrayList<StateChange>();

		assert(descriptor.getGenomeLength() == getLength());
			
		// Note: 'Mutation.position' is in units of
		// nucleotide and relative to the start of the genome.
		// 'StateChange.position' units depend upon the
		// alphabet of the feature and are relative to the start of
		// the feature.
		// 
		
		Feature feature = descriptor.getFeature(featureByName.getName());
		assert(feature != null);
		
		// Convert mutations from genome-relative coordinates to
		// feature-relative coordinates.  Mutations that do not affect
		// a feature do not result in any changes.  Indels also do not
		// result in any changes - they are not appropriate to capture
		// via individual nucleotide changes.
		int[] featureSiteTable = descriptor.getFeatureSiteTable(feature);
		for (Mutation m : mutations) {
			List<StateChange> c = m.getChanges(this, featureSiteTable);
			changes.addAll(c);
		}

		// At this point 'changes' are positioned at nucleotides
		// relative to the start of the feature.  For AMINO_ACID
		// features, convert nucleotide positions to AA positions, and
		// convert the states from nucleotides to AA states.
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
						}
					}

					// We don't want to access beyond the end of genomeSiteTable, but we do want
					// to allow amino acid feature coordinates to terminate between codons.  Here
					// we skip the partial codons that appear at the end of AMINO_ACID features.
					// Doing so avoids IndexOutOfRange exceptions while supporting feature
					// boundaries that are temporarily pushed out-of-frame by indels.
					if (genomeSiteTable.length <= (aa * 3 + 2)) {
						continue;
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
	 * Reference to an GenomeDescription.  It is through the
	 * decription that one can obtain Feature coordinates and SiteMaps
	 * that are updated for the indel lineage.  GenomeDescription
	 * instances are linked together in a tree, but that hierarchy is
	 * not exposed.  In fact I haven't yet found a need to explore the
	 * tree structure but it is there in anticipation of being useful.
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
