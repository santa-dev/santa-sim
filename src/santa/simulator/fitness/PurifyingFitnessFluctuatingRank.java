/*
 * Created on Mar 13, 2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import santa.simulator.Population;
import santa.simulator.Random;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.SequenceAlphabet;

public class PurifyingFitnessFluctuatingRank implements PurifyingFitnessRank {

    private Set<Integer> sites;
    private SequenceAlphabet alphabet;
    private double lambda;
    byte currentRank[][];

    public PurifyingFitnessFluctuatingRank(Set<Integer> sites,
            SequenceAlphabet alphabet, double lambda) {
        this.sites = sites;
        this.alphabet = alphabet;
        this.lambda = lambda;

        currentRank
            = new byte[GenomeDescription.getGenomeLength(alphabet)]
                      [alphabet.getStateCount()];

        List<Integer> shuffled = new ArrayList<Integer>();
        for (int i = 0; i < alphabet.getStateCount(); ++i)
            shuffled.add(i);
        
        for (int i = 0; i < currentRank.length; ++i) {
            Collections.shuffle(shuffled);

            for (int j = 0; j < alphabet.getStateCount(); ++j) {
                int s = shuffled.get(j);
                currentRank[i][j] = (byte)s;
            }
        }
    }
    
    public byte[] getStates(int site) {
        return currentRank[site];
    }

    public boolean updateGeneration(int generation, Population population) {
        boolean changed = false;

        for (int site:sites) {
            double p = Random.nextUniform(0, 1);
            if (p < lambda) {
                changeFitnessAt(site - 1);
                changed = true;
            }
        }

        return changed;
    }

    private void changeFitnessAt(int site) {
        List<Byte> states = new ArrayList<Byte>();

        for (int j = 0; j < alphabet.getStateCount(); ++j)
            states.add(currentRank[site][j]);

        Collections.shuffle(states);

        for (int j = 0; j < alphabet.getStateCount(); ++j)
            currentRank[site][j] = states.get(j);
    }

}
