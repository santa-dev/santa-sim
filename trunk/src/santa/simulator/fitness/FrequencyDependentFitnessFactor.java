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

public class FrequencyDependentFitnessFactor extends AbstractSignatureFitnessFactor {
    private double shape;
    private Map<Signature, Integer> frequencies;
    private double populationSize;

    public FrequencyDependentFitnessFactor(double shape, Feature feature, Set<Integer> sites) {
        super(feature, sites);
        this.shape = shape;
        this.frequencies = new HashMap<Signature, Integer>();
    }

    public double computeLogFitness(byte[] states) {
        Signature s = createSignature(states);
        Integer count = frequencies.get(s);

        if (count != null) {
            double f = count / populationSize;

            return Math.max(-10, Math.log(1 - Math.pow(f, shape)));
        } else
            return 0;
    }

    public boolean updateGeneration(int generation, Population population) {
        Set<Entry<Signature, Integer>> entries = frequencies.entrySet();
        for (Iterator<Entry<Signature, Integer>> i = entries.iterator(); i.hasNext();) {
            Entry<Signature, Integer> e = i.next();
            e.setValue(0);
        }

        List<Genome> genomes = population.getGenePool().getGenomes();

        for (Genome genome : genomes) {
	        byte[] sequence = genome.getStates(getFeature());
            Signature s = createSignature(sequence);
            Integer count = frequencies.get(s);
            frequencies.put(s, count == null ? genome.getFrequency() : count + genome.getFrequency());
        }

        entries = frequencies.entrySet();
        //System.err.println("States:");
        for (Iterator<Entry<Signature, Integer>> i = entries.iterator(); i.hasNext();) {
            Entry<Signature, Integer> e = i.next();
            if (e.getValue() == 0) {
                i.remove();
            } else {
                //System.err.println("entry: " + e.getKey() + ": " + e.getValue());
            }
        }

        this.populationSize = population.getPopulationSize();

        return true;
    }

    public double getShape() {
        return shape;
    }
}
