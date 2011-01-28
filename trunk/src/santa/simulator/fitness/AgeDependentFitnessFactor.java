/*
 * Created on Jul 18, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import santa.simulator.genomes.*;
import santa.simulator.population.Population;

import java.util.*;
import java.util.Map.Entry;

public class AgeDependentFitnessFactor extends AbstractSignatureFitnessFactor {
    private Map<Signature, Integer> birthGenerations;
    double declineRate;
    int currentGeneration;

    public AgeDependentFitnessFactor(double declineRate, Feature feature, Set<Integer> sites) {
        super(feature, sites);
        this.declineRate = declineRate;
        this.birthGenerations = new HashMap<Signature, Integer>();
    }

	public double computeLogFitness(byte[] states) {
        Signature s = createSignature(states);
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
	        byte[] sequence = genome.getStates(getFeature());
            Signature s = createSignature(sequence);
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

        //System.err.println("Average viral genome age: " + sumAges / population.getPopulationSize());

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

    public double getDeclineRate() {
        return declineRate;
    }
}
