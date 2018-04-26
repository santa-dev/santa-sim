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
    private double splitProbability;
    private double deathProbability;
    private double persistProbability;
    
    public ClonalExpansionSelector(double splitProbability, double deathProbability) {
        this.splitProbability = splitProbability;
        this.deathProbability = deathProbability;
        this.persistProbability = 1 - deathProbability;
    }

    public void selectParents(List<Virus> currentGeneration, List<Integer> selectedParents, int sampleSize) {
        for(int i = 0; i < currentGeneration.size(); ++i) {
            double fitness = currentGeneration.get(i).getFitness();
            int nbChildren = 0;
            
            double draw = Random.nextUniform(0, 1);
            if (draw < persistProbability * fitness) {
                if (draw < splitProbability * fitness) {
                    nbChildren = 2;
                } else {
                    nbChildren = 1;
                }
            }
            
            for(long n = 0; n < nbChildren * sampleSize; ++n) {
                selectedParents.add(i);
            }
        }
        Collections.shuffle(selectedParents);
    }
}
