/*
 * Created on Jul 18, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import santa.simulator.Population;
import santa.simulator.genomes.*;

import java.util.*;
import java.util.Map.Entry;

public class ExposureDependentFitnessFactor extends AbstractSignatureFitnessFactor {
    private Map<Signature, Double> exposure;
    double penalty;
    int currentGeneration;

    public ExposureDependentFitnessFactor(double penalty, Feature feature, Set<Integer> sites) {
        super(feature, sites);
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

    public double getPenalty() {
        return penalty;
    }
}
