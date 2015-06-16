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

/**
 * The exposure dependent fitness function assigns a fitness to an
 * individual based on how much the allele has been exposed in the
 * population (since it last appeared), and assigns a lower
 * fitness to alleles that have been present for a longer time in
 * a higher prevalence in the population for a longer time, using
 * the formula: f = e^-(penalty*E), where E is the integrated
 * prevalence of the allele over time since its last appearance,
 * and penalty a parameter that controls how severe past exposure
 * is punished in terms of fitness.
 **/
public class ExposureDependentFitnessFactor extends AbstractSignatureFitnessFactor {
    private Map<Signature, Double> exposure;
    double penalty;
    int currentGeneration;

    public ExposureDependentFitnessFactor(double penalty, Feature feature, Set<Integer> sites) {
        super(feature, sites);
        this.penalty = penalty;
        this.exposure = new HashMap<Signature, Double>();
    }

    public double computeLogFitness(byte[] states) {
        Signature s = createSignature(states);
        Double e = exposure.get(s);

        if (e != null) {
            return Math.max(-10, -e * penalty);
        } else
            return 0;
    }


    /**
	 * Invoked once per generation to update prevelance integral for each allele.
	 * @param generation
	 * @param population
	 *
	 * @return  TRUE if fitness cache is invalidated, FALSE otherwise.
	 **/
	public boolean updateGeneration(int generation, Population population) {
        this.currentGeneration = generation + 1;

        List<Genome> genomes = population.getGenePool().getGenomes();

        /*
         * We will flag 'living' entries as negative, so that we will toss
         * positive entries in a second run.
         */

        for (Genome genome : genomes) {
	        byte[] sequence = genome.getStates(getFeature());
            Signature s = createSignature(sequence);
            Double e = exposure.get(s);
            double d = (double)genome.getFrequency() / population.getPopulationSize();

            if (e == null) {
                exposure.put(s, -d);
            } else {
                if (e > 0) {
                    exposure.put(s, -e - d);
                } else {
                    exposure.put(s, e - d);
				}
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

    public double getPenalty() {
        return penalty;
    }
}
