/*
 * Created on Jul 18, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import santa.simulator.Population;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.Mutation;
import santa.simulator.genomes.SequenceAlphabet;

public class AgeDependentFitnessFunction extends AbstractSignatureFitnessFunction {
    private Map<Signature, Integer> birthGenerations;
    double declineRate;
    int currentGeneration;

    public AgeDependentFitnessFunction(double declineRate, Set<Integer> sites, SequenceAlphabet alphabet) {
        super(sites, alphabet);
        this.declineRate = declineRate;
        this.birthGenerations = new HashMap<Signature, Integer>();
    }

    public double computeLogFitness(Genome genome) {
        Signature s = createSignature(genome);
        Integer birthGeneration = birthGenerations.get(s);

        if (birthGeneration != null) {
            double age = currentGeneration - birthGeneration;
            return Math.max(-10, -age * declineRate);
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

        double sumAges = 0;
        for (Genome genome : genomes) {
            Signature s = createSignature(genome);
            Integer birthGeneration = birthGenerations.get(s);

            if (birthGeneration == null) {
                birthGenerations.put(s, -currentGeneration);
            } else {
                if (birthGeneration > 0) {
                    birthGenerations.put(s, -birthGeneration);
                    sumAges += (currentGeneration - birthGeneration) * genome.getFrequency();
                }
            }
        }

        System.err.println("Average viral genome age: " + sumAges / population.getPopulationSize());

        Set<Entry<Signature, Integer>> entries = birthGenerations.entrySet();
        for (Iterator<Entry<Signature, Integer>> i = entries.iterator(); i.hasNext();) {
            Entry<Signature, Integer> e = i.next();
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
