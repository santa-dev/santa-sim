/*
 * Created on Mar 13, 2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import santa.simulator.Population;
import santa.simulator.genomes.Sequence;
import santa.simulator.genomes.SequenceAlphabet;

public class PurifyingFitnessAlignmentRank implements PurifyingFitnessRank {
    private SequenceAlphabet alphabet;
    private List<Sequence> alignment;
    private boolean breakTiesRandom;

    public PurifyingFitnessAlignmentRank(SequenceAlphabet alphabet,
                                         List<Sequence> alignment,
                                         boolean breakTiesRandom) {
        this.alphabet = alphabet;
        this.alignment = alignment;
        this.breakTiesRandom = breakTiesRandom;
    }

    static private class HistogramEntry implements Comparable<HistogramEntry> {
        byte state;
        int count;

        public HistogramEntry(byte state, int count) {
            this.state = state;
            this.count = count;
        }

        public int compareTo(HistogramEntry other) {
            return other.count - count;
        }
    }
    
    public byte[] getStates(int site) {
        List<HistogramEntry> counts = new ArrayList<HistogramEntry>();
        
        for (int i = 0; i < alphabet.getStateCount(); ++i) {
            counts.add(new HistogramEntry((byte)i, 0));
        }

        for (Sequence s:alignment) {
            byte state = s.getState(alphabet, site);
            ++counts.get(state).count;
        }

        if (breakTiesRandom)
            Collections.shuffle(counts);

        Collections.sort(counts);

        byte[] result = new byte[alphabet.getStateCount()];

        for (int i = 0; i < result.length; ++i) {
            result[i] = counts.get(i).state;
        }

        return result;
    }

    public boolean updateGeneration(int generation, Population population) {
        return false;
    }

}
