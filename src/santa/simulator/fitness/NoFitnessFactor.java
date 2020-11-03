/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package santa.simulator.fitness;

import java.util.Set;
import santa.simulator.genomes.Feature;
import santa.simulator.genomes.StateChange;

/**
 *
 * @author Bradley R. Jones
 */
public class NoFitnessFactor extends AbstractFitnessFactor {

    public NoFitnessFactor(Feature feature, Set<Integer> sites) {
        super(feature, sites);
    }

    @Override
    public double computeLogFitness(byte[] states) {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double getLogFitnessChange(StateChange change) {
        return 0;
    }
    
}
