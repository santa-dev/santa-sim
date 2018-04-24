package santa.simulator.selectors;

import java.util.Collections;
import java.util.List;
import santa.simulator.Random;
import santa.simulator.Virus;

/**
 *
 * @author Bradley R. Jones
 */
public class ClonalExpansionSelector implements Selector {
    private double growthRate;
    private double carryingPopulation;
    
    public ClonalExpansionSelector(double growthRate, double carryingPopulation) {
        this.growthRate = growthRate;
        this.carryingPopulation = carryingPopulation;
    }

    public void selectParents(List<Virus> currentGeneration, List<Integer> selectedParents, int sampleSize) {
        Collections.shuffle(currentGeneration);
        for(int i = 0; i < currentGeneration.size(); ++i) {
            double fitness = currentGeneration.get(i).getFitness();
            
            double splitProb =  Math.max(fitness *  (1 - growthRate) * (1-(selectedParents.size() + currentGeneration.size())/carryingPopulation),Double.MIN_VALUE);
            long nbChildren = fitness == 0 ? 0 : Random.nextBinomial(1, splitProb) + 1;
            for(long n = 0; n < nbChildren * sampleSize; ++n) {
                selectedParents.add(i);
            }
        }
        Collections.shuffle(selectedParents);
    }
}
