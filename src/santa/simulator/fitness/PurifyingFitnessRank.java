package santa.simulator.fitness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.Sequence;
import santa.simulator.genomes.SequenceAlphabet;

public class PurifyingFitnessRank {
    private SequenceAlphabet alphabet;
    private List<Sequence> alignment;
    private List<Byte> stateOrder;
    private boolean breakTiesRandom;

    private byte[][] rank;
    private byte[] observedCount;
    
    public PurifyingFitnessRank(SequenceAlphabet alphabet,
                                List<Sequence> alignment, List<Byte> stateOrder,
                                boolean breakTiesRandom) {
        this.alphabet = alphabet;
        this.alignment = alignment;
        this.stateOrder = stateOrder;
        this.breakTiesRandom = breakTiesRandom;

        preCompute();
    }

    public PurifyingFitnessRank(SequenceAlphabet alphabet, double[] fitnesses) {
        this.alphabet = alphabet;

        List<HistogramEntry> counts = new ArrayList<HistogramEntry>();

        for (int i = 0; i < alphabet.getStateCount(); ++i) {
            counts.add(new HistogramEntry((byte)i, fitnesses[i]));
        }
        
        Collections.sort(counts);

        rank = new byte[GenomeDescription.getGenomeLength(alphabet)][alphabet.getStateCount()];
        observedCount = null;

        for (int i = 0; i < GenomeDescription.getGenomeLength(alphabet); ++i) {
            for (int j = 0; j < alphabet.getStateCount(); ++j) {
                rank[i][j] = counts.get(j).state;
            }
        }       
    }

    private void preCompute() {
        rank = new byte[GenomeDescription.getGenomeLength(alphabet)][alphabet.getStateCount()];
        observedCount = new byte[GenomeDescription.getGenomeLength(alphabet)];
        
        for (int i = 0; i < GenomeDescription.getGenomeLength(alphabet); ++i) {
            List<HistogramEntry> counts = createHistogram(i);

            for (int j = 0; j < alphabet.getStateCount(); ++j) {
                rank[i][j] = counts.get(j).state;
            }

            observedCount[i] = 0;
            for (int j = 0; j < counts.size(); ++j)
                if (counts.get(j).count != 0)
                    observedCount[i] = (byte)(j+1);
        }
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
    
    public byte[] getStatesOrder(int site) {
        return rank[site];
    }

    /**
     * @param site: 0-based site in the sequence
     * @return an ordering of the states for that site
     */
    private List<HistogramEntry> createHistogram(int site) {
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

        if (breakTiesRandom)
            Collections.shuffle(counts);

        Collections.sort(counts);
        return counts;
    }

    /**
     * @param site: 0-based site in the sequence
     * @return the number of observed states for that site
     */
    int getObservedStatesCount(int site) {
        return observedCount[site];
   }
}