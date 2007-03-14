/*
 * Created on Apr 14, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import java.util.*;

import santa.simulator.Population;
import santa.simulator.Random;
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
    private double fluctuateRate;
    private double fluctuateLogFitnessLimit;
    
    boolean changed;

    public PurifyingFitnessFunction(PurifyingFitnessRank rank,
                                    PurifyingFitnessModel valueModel,
                                    double fluctuateRate,
                                    double fluctuateFitnessLimit,
                                    Set<Integer> sites, SequenceAlphabet alphabet) {
        super(sites, alphabet);

        this.rank = rank;
        this.valueModel = valueModel;
        this.fluctuateRate = fluctuateRate;
        this.fluctuateLogFitnessLimit = Math.log(fluctuateFitnessLimit);

        setFitnesses();
    }

    protected void setFitnesses() {
        int stateSize = getAlphabet().getStateCount();
        Set<Integer> sites = getSites();

        double[][] logFitness = new double[GenomeDescription.getGenomeLength(getAlphabet())][stateSize];

        for (int i = 0; i < logFitness.length; ++i) {
            if (sites.contains(i + 1)) {
                double[] fitnesses = valueModel.getFitnesses(i, rank);
                byte[] states = rank.getStatesOrder(i);
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

        if (fluctuateRate != 0) {
            Iterator<Integer> it = getSites().iterator();

            while (it.hasNext()) {
                int site = it.next();

                if (getSites().contains(site)) {
                    double p = Random.nextUniform(0, 1);
                    if (p < fluctuateRate) {
                        changeFitnessAt(site - 1);
                        changed = true;
                    }
                }
            }
        }
        
        return changed;
    }

    private void changeFitnessAt(int i) {
        List<Double> fs = new ArrayList<Double>();
        List<Integer> indexes = new ArrayList<Integer>();

        for (int j = 0; j < getAlphabet().getStateCount(); ++j) {
            double logfitness = getLogFitness(i, (byte) j);
            if (logfitness >= fluctuateLogFitnessLimit) {
                fs.add(logfitness);
                indexes.add(j);
            }
        }

        Collections.shuffle(fs);

        for (int j = 0; j < indexes.size(); ++j)
            setLogFitness(i, indexes.get(j).byteValue(), fs.get(j));
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

    public double getFluctuateFitnessLimit() {
        return Math.exp(fluctuateLogFitnessLimit);
    }

    public double getFluctuateRate() {
        return fluctuateRate;
    }

    public static PurifyingFitnessFunction createEmpiricalFitnessFunction(double[] fitnesses, Set<Integer> sites, SequenceAlphabet alphabet) {
        List<Byte> states = new ArrayList<Byte>();
        for (byte b = 0; b < alphabet.getStateCount(); b++) {
            states.add(b);
        }
        PurifyingFitnessRank rank = new PurifyingFitnessRank(alphabet, states, alphabet.getStateCount());
        PurifyingFitnessValuesModel model = new PurifyingFitnessValuesModel(fitnesses);
        
        return new PurifyingFitnessFunction(rank, model, 0, 0, sites, alphabet);
    }
}
