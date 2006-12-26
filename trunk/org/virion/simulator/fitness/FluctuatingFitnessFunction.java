package org.virion.simulator.fitness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.virion.simulator.Population;
import org.virion.simulator.Random;
import org.virion.simulator.genomes.Genome;
import org.virion.simulator.genomes.GenomeDescription;
import org.virion.simulator.genomes.SequenceAlphabet;

/**
 * A fitness function that changes (shuffles) the fitness of different states
 * at one particular position, over time, following a Poisson process.
 *
 * @author kdf
 */
public class FluctuatingFitnessFunction extends PurifyingFitnessFunction {
    private double lambda;
    private boolean changed;

    public FluctuatingFitnessFunction(double purificationStrength, double lambda,
                                      Set<Integer> sites, SequenceAlphabet alphabet) {

        super(createRandomFittest(alphabet), purificationStrength, sites, alphabet);
        this.lambda = lambda;
    }

    private static byte[] createRandomFittest(SequenceAlphabet alphabet) {
        int genomeLength = GenomeDescription.getGenomeLength(alphabet);
 
        byte[] result = new byte[genomeLength];

        for (int i = 0; i < result.length; ++i) {
            result[i] = (byte) Random.nextInt(0, alphabet.getStateCount() - 1);
        }

        return result;
    }

    @Override
    public boolean updateGeneration(int generation, Population population) {
        changed = false;

        Iterator<Integer> it = getSites().iterator();

        while (it.hasNext()) {
            int site = it.next();

            if (getSites().contains(site)) {
                double p = Random.nextUniform(0, 1);
                if (p < lambda) {
                    changeFitnessAt(site - 1);
                    changed = true;
                }
            }
        }
        
        return changed;
    }

    private void changeFitnessAt(int i) {
        List<Double> fs = new ArrayList<Double>();
        
        for (int j = 0; j < getAlphabet().getStateCount(); ++j)
            fs.add((Double) getLogFitness(i, (byte) j));

        Collections.shuffle(fs);
        
        for (int j = 0; j < getAlphabet().getStateCount(); ++j)
            setLogFitness(i, (byte) j, fs.get(j));
    }

    @Override
    public double updateLogFitness(Genome genome, double logFitness) {
        if (changed) {
            return computeLogFitness(genome);
        } else
            return logFitness;
    }
}
