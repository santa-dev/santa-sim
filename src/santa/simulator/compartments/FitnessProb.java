package santa.simulator.compartments;

import santa.simulator.Virus;
import santa.simulator.fitness.FitnessFunction;
import santa.simulator.genomes.Genome;

/**
 *
 * @author Bradley R. Jones
 */
public class FitnessProb implements TransferProb {
    public FitnessFunction fitness;
    
    public FitnessProb(FitnessFunction fitness) {
        this.fitness = fitness;
    }
    
    public double getProb(Virus virus, int generation) {
        Genome genome = virus.getGenome().copy();
        fitness.computeLogFitness(genome);
        
        return genome.getFitness();
    }
}
