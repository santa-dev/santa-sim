/*
 * Created on Apr 14, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import java.util.Set;

import santa.simulator.Population;
import santa.simulator.genomes.Genome;
import santa.simulator.genomes.GenomeDescription;
import santa.simulator.genomes.SequenceAlphabet;

/**
 * A Purifying fitness function performs puryfing selection. It is configured by
 * giving it a rank and model for its fitness values.
 */
public class PurifyingFitnessFunction extends AbstractSiteFitnessFunction {
    private PurifyingFitnessRank rank;
    private PurifyingFitnessModel valueModel;
    
    boolean changed;

    public PurifyingFitnessFunction(PurifyingFitnessRank rank,
                                    PurifyingFitnessModel valueModel,
                                    Set<Integer> sites, SequenceAlphabet alphabet) {
        super(sites, alphabet);

        this.rank = rank;
        this.valueModel = valueModel;
        
        setFitnesses();
    }

    protected void setFitnesses() {
        int stateSize = getAlphabet().getStateCount();
        Set<Integer> sites = getSites();

        double[] fitnesses = valueModel.getFitnesses();
        double[][] logFitness = new double[GenomeDescription.getGenomeLength(getAlphabet())][stateSize];

        for (int i = 0; i < logFitness.length; ++i) {
            if (sites.contains(i + 1)) {
                byte[] states = rank.getStates(i);
                for (int j = 0; j < stateSize; ++j) {                    
                    logFitness[i][states[j]] = Math.log(fitnesses[j]);
                }
            } else {
                for (int j = 0; j < stateSize; ++j) {
                    logFitness[i][j] = 0;
                }
            }
        }

        initialize(logFitness);
    }

    @Override
    public boolean updateGeneration(int generation, Population population) {
        changed = false;

        changed = rank.updateGeneration(generation, population);
        if (changed)
            setFitnesses();
        
        return changed;
    }

    public double updateLogFitness(Genome genome, double logFitness) {
        if (changed) {
            return computeLogFitness(genome);
        } else
            return logFitness;
     }

    public PurifyingFitnessRank getRank() {
        return rank;
    }

    public PurifyingFitnessModel getValueModel() {
        return valueModel;
    }
}
