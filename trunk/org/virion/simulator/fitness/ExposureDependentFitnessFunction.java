/*
 * Created on Jul 18, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.virion.simulator.fitness;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.virion.simulator.Population;
import org.virion.simulator.genomes.Genome;
import org.virion.simulator.genomes.Mutation;
import org.virion.simulator.genomes.SequenceAlphabet;

public class ExposureDependentFitnessFunction extends AbstractSignatureFitnessFunction {
    private Map<Signature, Double> exposure;
    double penalty;
    int currentGeneration;

    public ExposureDependentFitnessFunction(double penalty, Set<Integer> sites, SequenceAlphabet alphabet) {
        super(sites, alphabet);
        this.penalty = penalty;
        this.exposure = new HashMap<Signature, Double>();
    }

    public double computeLogFitness(Genome genome) {
        Signature s = createSignature(genome);
        Double e = exposure.get(s);
        
        if (e != null) {
            return Math.max(-10, -e * penalty);
        } else
            return 0;
    }

    public boolean updateGeneration(int generation, Population population) {
        this.currentGeneration = generation + 1;
        
        List<Genome> genomes = population.getGenePool().getGenomes();

        /*
         * We will flag 'living' entries as negative, so that we will toss
         * positive entries in a second run.
         */

        for (Genome genome : genomes) {
            Signature s = createSignature(genome);
            Double e = exposure.get(s);
            double d = (double)genome.getFrequency() / population.getPopulationSize();
            
            if (e == null) {
                exposure.put(s, -d);
            } else {
                if (e > 0) {
                    exposure.put(s, -e - d);
                } else
                    exposure.put(s, e - d);
            }
        }

        Set<Entry<Signature, Double>> entries = exposure.entrySet();
        for (Iterator<Entry<Signature, Double>> i = entries.iterator(); i.hasNext();) {
            Entry<Signature, Double> e = i.next();
            if (e.getValue() > 0) {
                i.remove();
            } else {
                e.setValue(-e.getValue());
            }
        }
        
        return true;
    }

    public double updateLogFitness(Genome genome, double logFitness, Mutation m) {
        return logFitness;
    }

    public double updateLogFitness(Genome genome, double logFitness) {
        return computeLogFitness(genome);
    }
}
