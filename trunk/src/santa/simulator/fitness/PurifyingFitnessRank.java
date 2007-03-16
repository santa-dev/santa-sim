package santa.simulator.fitness;

import santa.simulator.genomes.*;

import java.util.*;

public class PurifyingFitnessRank {

    public PurifyingFitnessRank(SequenceAlphabet alphabet,
                                List<Sequence> alignment, List<Byte> stateOrder,
                                boolean breakTiesRandom) {

	    if ((alignment == null && stateOrder == null) || (alignment != null && stateOrder != null)) {
		    throw new IllegalArgumentException("Either alignment or stateOrder but not both should be specified");
	    }

        rank = new byte[GenomeDescription.getGenomeLength(alphabet)][alphabet.getStateCount()];
        probableSetSize = new byte[GenomeDescription.getGenomeLength(alphabet)];

        for (int i = 0; i < GenomeDescription.getGenomeLength(alphabet); ++i) {
            List<HistogramEntry> counts = createHistogram(alphabet, breakTiesRandom, alignment, stateOrder, i);

            for (int j = 0; j < alphabet.getStateCount(); ++j) {
                rank[i][j] = counts.get(j).state;
            }

            probableSetSize[i] = 0;
            for (int j = 0; j < counts.size(); ++j)
                if (counts.get(j).count != 0)
                    probableSetSize[i] = (byte)(j+1);
        }
    }

    public PurifyingFitnessRank(SequenceAlphabet alphabet, List<Set<Byte>> stateClasses,
                                List<Sequence> alignment, List<Byte> stateOrder,
                                boolean breakTiesRandom) {
		if ((alignment == null && stateOrder == null) || (alignment != null && stateOrder != null)) {
			throw new IllegalArgumentException("Either alignment or stateOrder but not both should be specified");
		}

        rank = new byte[GenomeDescription.getGenomeLength(alphabet)][alphabet.getStateCount()];
        probableSetSize = new byte[GenomeDescription.getGenomeLength(alphabet)];

        for (int i = 0; i < GenomeDescription.getGenomeLength(alphabet); ++i) {
            List<HistogramEntry> counts = createHistogram(alphabet, breakTiesRandom, alignment, stateOrder, i);

            for (Set<Byte> stateClassSet : stateClasses) {
                if (stateClassSet.contains(counts.get(0).state)) {
	                probableSetSize[i] = (byte)stateClassSet.size();

                    rank[i][0] = counts.get(0).state;
	                int u = 1;
	                int v = probableSetSize[i];
                    for (int j = 1; j < alphabet.getStateCount(); j++) {
	                    if (stateClassSet.contains(counts.get(j).state)) {
		                    rank[i][u] = counts.get(j).state;
		                    u++;
	                    } else {
		                    rank[i][v] = counts.get(j).state;
		                    v++;
	                    }
                    }

	                break;
                }
            }

        }
    }

    public PurifyingFitnessRank(SequenceAlphabet alphabet, List<Byte> stateOrder, int probableSetSize) {

        rank = new byte[GenomeDescription.getGenomeLength(alphabet)][alphabet.getStateCount()];
        this.probableSetSize = new byte[GenomeDescription.getGenomeLength(alphabet)];

        for (int i = 0; i < GenomeDescription.getGenomeLength(alphabet); ++i) {
            for (int j = 0; j < alphabet.getStateCount(); ++j) {
                rank[i][j] = stateOrder.get(j);
            }
            this.probableSetSize[i] = (byte)probableSetSize;
        }
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
     * @param site: 0-based site in the sequence
     * @return an ordering of the states for that site
     */
    private List<HistogramEntry> createHistogram(SequenceAlphabet alphabet, boolean breakTiesRandomly,
                                List<Sequence> alignment, List<Byte> stateOrder, int site) {
        List<HistogramEntry> counts = new ArrayList<HistogramEntry>();

        for (int i = 0; i < alphabet.getStateCount(); ++i) {
            counts.add(new HistogramEntry((byte)i, 0));
        }

        if (alignment != null) {
            for (Sequence s:alignment) {
                byte state = s.getState(alphabet, site);
                ++counts.get(state).count;
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