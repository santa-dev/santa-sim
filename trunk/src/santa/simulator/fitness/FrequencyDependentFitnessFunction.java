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

public class FrequencyDependentFitnessFunction extends AbstractSignatureFitnessFunction {
    private double shape;
    private Map<Signature, Integer> frequencies;
    private double populationSize;

    public FrequencyDependentFitnessFunction(double shape, Set<Integer> sites, SequenceAlphabet alphabet) {
        super(sites, alphabet);
        this.shape = shape;
        this.frequencies = new HashMap<Signature, Integer>();
    }

    public double computeLogFitness(Genome genome) {
        Signature s = createSignature(genome);
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
            Signature s = createSignature(genome);
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

    public double updateLogFitness(Genome genome, double logFitness, Mutation m) {
        return logFitness;
    }

    public double updateLogFitness(Genome genome, double logFitness) {
        return computeLogFitness(genome);
    }

    public double getShape() {
        return shape;
    }
}
