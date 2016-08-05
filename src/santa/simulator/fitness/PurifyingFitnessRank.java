package santa.simulator.fitness;

import santa.simulator.genomes.*;


import java.util.*;

/**
 * Specifies a ranking of the 20 amino acids (or 4 bases), and defines the size
 * of a probable set.
 * 
 * The rank may be derived from frequencies found in the sequences of the GenomeDescription,
 * possibly using chemical properties, or explicitly specified.
 * 
 * Different configuration options:
 * {@code
 *   <rank>
 *      <breakTies>random|ordered</breakTies>
 *      <order>observed|XYZ|chemical|hydropathy|volume</order>
 *      <probableSet>x</probableSet> <!-- optional -->
 *   </rank>
 * }
 */
public class PurifyingFitnessRank {

	/**
	 * Creates a ranking:
     *   - based on a specified state order
     *   - based on a specified probable set size
	 * @param feature  The feature over which this fitness function should be applied.
	 * @param stateOrder  List of states (amino acid or nucleotide), ordered by decreasing fitness.
	 * @param probableSetSize  
	 * @param breakTiesRandomly  True if ties should be broken randomly, False to break ties according to order in the list.
	 */
	public PurifyingFitnessRank(Feature feature,
	                            List<Byte> stateOrder,
	                            int probableSetSize,
	                            boolean breakTiesRandomly) {

		SequenceAlphabet alphabet = feature.getAlphabet();
		int siteCount = feature.getLength();
		if (probableSetSize < 1) {
			probableSetSize = stateOrder.size();
		}
        rank = new byte[siteCount][alphabet.getStateCount()];
		this.probableSetSize = new byte[siteCount];

		for (int i = 0; i < siteCount; ++i) {
			List<HistogramEntry> counts = createHistogram(alphabet, breakTiesRandomly, null, stateOrder, i);

            for (int j = 0; j < stateOrder.size(); ++j) {
                rank[i][j] = stateOrder.get(j);
            }
            
			for (int j = stateOrder.size(); j < alphabet.getStateCount(); ++j) {
                if (!stateOrder.contains(counts.get(j).state))
                    rank[i][j] = counts.get(j).state;
			}

			this.probableSetSize[i] = (byte)probableSetSize;
		}
	}

    /**
     * Creates a ranking based on a partition of the alphabet into several classes
     *   - a probable class is chosen as the class with the most frequent amino acid
     *   - a probable set size is the size of the class
     *   - the amino acids in both the probable set, and the non-probable set, or ranked
     *     based on frequency.
     */
	public PurifyingFitnessRank(Feature feature,
	                            List<Set<Byte>> stateClasses,
                                boolean breakTiesRandomly,
                                int probableSetSize) {

		SequenceAlphabet alphabet = feature.getAlphabet();
		int siteCount = feature.getLength();

		rank = new byte[siteCount][alphabet.getStateCount()];
		this.probableSetSize = new byte[siteCount];

        List<byte[]> alignment = getAlignment(feature);

		for (int i = 0; i < siteCount; i++) {
			List<HistogramEntry> counts = createHistogram(alphabet, breakTiesRandomly, alignment, null, i);

			for (Set<Byte> stateClassSet : stateClasses) {
				if (stateClassSet.contains(counts.get(0).state)) {
					this.probableSetSize[i] = (byte)stateClassSet.size();

					rank[i][0] = counts.get(0).state;
					int u = 1;
					int v = this.probableSetSize[i];
					for (int j = 1; j < alphabet.getStateCount(); j++) {
						if (stateClassSet.contains(counts.get(j).state)) {
							rank[i][u] = counts.get(j).state;
							u++;
						} else {
							rank[i][v] = counts.get(j).state;
							v++;
						}
					}

                    if (probableSetSize != -1)
                        this.probableSetSize[i] = (byte) probableSetSize;
                    
					break;
				}
			}

		}
	}

    /**
     * Creates a ranking:
     *  - using the frequencies found in the sequences of the GenomeDescription.
     *  - with probable set size given
     */
	public PurifyingFitnessRank(Feature feature,
	                            int probableSetSize,
	                            boolean breakTiesRandomly) {

		SequenceAlphabet alphabet = feature.getAlphabet();
		int siteCount = feature.getLength();

        rank = new byte[siteCount][alphabet.getStateCount()];
        this.probableSetSize = new byte[siteCount];

        List<byte[]> alignment = getAlignment(feature);

        for (int i = 0; i < siteCount; i++) {
			List<HistogramEntry> counts = createHistogram(alphabet, breakTiesRandomly, alignment, null, i);

			for (int j = 0; j < alphabet.getStateCount(); ++j) {
				rank[i][j] = counts.get(j).state;
			}

            if (probableSetSize != -1) {
                this.probableSetSize[i] = (byte)probableSetSize;
            } else {
                this.probableSetSize[i] = 0;
                for (int j = 0; j < counts.size(); ++j)
                    if (counts.get(j).count != 0)
                        this.probableSetSize[i] = (byte)(j+1);
            }
		}
	}

    private List<byte[]> getAlignment(Feature feature) {

        List<byte[]> alignment = new ArrayList<byte[]>();
        for (Sequence genomeSequence : GenomeDescription.getSequences()) {
            SimpleGenome genome = new SimpleGenome();
            genome.setSequence(genomeSequence);

            byte[] sequence = genome.getStates(feature);
            alignment.add(sequence);
        }
        return alignment;
    }

    public byte[] getStatesOrder(int site) {
		return rank[site];
	}

	/**
	 * @param site: 0-based site in the sequence
	 * @return the number of observed states for that site
	 */
	public int getProbableSetSize(int site) {
		return probableSetSize[site];
	}


	// PRIVATE STUFF

	/**
	 * Count abundance of symbols at a SINGLE SITE in a multiple alignment.
	 *
	 * 'alphabet' references a NUCLEOTIDES or AMINO_ACIDS
	 * alphabet.  Assuming NUCLEOTIDES, this routine would calculate
	 * the abundance of each nucleotide at a single column of a multiple
	 * alignment.
	 *
	 * The multiple alignment is usually taken from the initial
	 * population seed as specified in the config file.  It is not
	 * unusual for there to be only a single sequence in the
	 * alignment.
	 * 
	 * The abundances calculated here are used to shape a probability
	 * distribution affecting the fitness of various substitutions at
	 * each site.
	 *
	 * @param alphabet: NUCLEOTIDES or AMINO_ACIDS alphabet.
	 * @param breakTiesRandomly:  when TRUE, abundance ties will be randomly ordered in the results, otherwise according to order of states in alphabet. 
	 * @param alignment: multiple sequence alignment
	 * @param stateOrder: order of states to report back; relevant only  if 'alignment' is null.
	 * @param site: 0-based site in the sequence
	 * @return list of histogram elements in decreasing order of abundance.
	 */
	private List<HistogramEntry> createHistogram(SequenceAlphabet alphabet, boolean breakTiesRandomly,
	                                             List<byte[]> alignment, List<Byte> stateOrder, int site) throws PrematureStopException {
		List<HistogramEntry> counts = new ArrayList<HistogramEntry>();

		for (int i = 0; i < alphabet.getStateCount(); ++i) {
			counts.add(new HistogramEntry((byte)i, 0));
		}

		if (alignment != null) {
			int n = 1;  // sequence counter
			for (byte[] sequence : alignment) {
				/**
				 * cswarth - protect against stop codons in initial alignment.
				 **/
				int i = sequence[site];
				if (i < counts.size())
					++counts.get(i).count;
				else {
					throw new PrematureStopException(n, site);
					// String msg = String.format("PurifyingFitnessRank: Skipping feature site %d in alignment #%d.\nUsually this indicates a premature STOP codon in an Amino Acid feature.", site, n);
					// System.err.println(msg);
				}
				n += 1;
			}
		} else {
			for (int i = 0; i < stateOrder.size(); ++i) {
				counts.get(stateOrder.get(i)).count = stateOrder.size() - i;
			}
		}

		if (breakTiesRandomly)
			Collections.shuffle(counts);

		Collections.sort(counts);
		return counts;
	}

	static private class HistogramEntry implements Comparable<HistogramEntry> {
		byte state;
		double count;

		public HistogramEntry(byte state, double count) {
			this.state = state;
			this.count = count;
		}

		public int compareTo(HistogramEntry other) {
			double diff = other.count - count;
			if (diff < 0)
				return -1;
			else if (diff == 0)
				return 0;
			else
				return 1;
		}
	}

	private final byte[][] rank;
	private final byte[] probableSetSize;

}
