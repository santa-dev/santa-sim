/*
 * Created on Apr 14, 2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import santa.simulator.Population;
import santa.simulator.Random;
import santa.simulator.EventLogger;
import santa.simulator.genomes.*;

import java.util.*;

/**
 * A Purifying fitness function performs puryfing selection. It is configured by
 * giving it a rank and model for its fitness values.
 */
public class PurifyingFitnessFactor extends AbstractSiteFitnessFactor {
    private PurifyingFitnessRank rank;
    private PurifyingFitnessModel valueModel;
    private double fluctuateRate;
    private double fluctuateLogFitnessLimit;

    public PurifyingFitnessFactor(PurifyingFitnessRank rank,
                                    PurifyingFitnessModel valueModel,
                                    double fluctuateRate,
                                    double fluctuateFitnessLimit,
                                    Feature feature,
                                    Set<Integer> sites) {
        super(feature, sites);

        this.rank = rank;
        this.valueModel = valueModel;
        this.fluctuateRate = fluctuateRate;
        this.fluctuateLogFitnessLimit = Math.log(fluctuateFitnessLimit);

        setFitnesses();
    }

    protected void setFitnesses() {
        int siteCount = getFeature().getLength();
        int stateSize = getAlphabet().getStateCount();
        Set<Integer> sites = getSites();

        double[][] logFitness = new double[siteCount][stateSize];

        for (int i = 0; i < logFitness.length; ++i) {
            if (sites.contains(i)) {
                double[] fitnesses = valueModel.getFitnesses(i, rank);
                byte[] states = rank.getStatesOrder(i);
                for (int j = 0; j < stateSize; ++j) {
                    logFitness[i][states[j]] = Math.log(fitnesses[j]);
                }
            } else {
                for (int j = 0; j < stateSize; ++j) {
                    logFitness[i][j] = 0.0;
                }
            }
        }

        initialize(logFitness);
    }

    @Override
    public boolean updateGeneration(int generation, Population population) {
        boolean changed = false;

        if (fluctuateRate != 0) {
            Iterator<Integer> it = getSites().iterator();

            while (it.hasNext()) {
                int site = it.next();

                double p = Random.nextUniform(0, 1);
                if (p < fluctuateRate) {
                    changeFitnessAt(site - 1);
                    changed = true;
                }
            }
        }

        return changed;
    }

    private void changeFitnessAt(int i) {
        List<Integer> fittest = new ArrayList<Integer>();
        List<Integer> lessFit = new ArrayList<Integer>();

        for (int j = 0; j < getAlphabet().getStateCount(); ++j) {
            double logfitness = getLogFitness(i, (byte) j);
            if (logfitness == 0.0) {
                fittest.add(j);
            } else if (logfitness + 1E-8 >= fluctuateLogFitnessLimit) {
                lessFit.add(j);
            }
        }

        if (fittest.size() == 0) {
            throw new RuntimeException("No fittest states to fluctuate");
        }

        if (lessFit.size() == 0) {
            throw new RuntimeException("No less fit states to fluctuate");
        }

        Integer newFittest = lessFit.get(0);
        if (lessFit.size() > 1) {
            newFittest = lessFit.get(Random.nextInt(0, lessFit.size() - 1));
        }

        Integer newLessFit = fittest.get(0);
        if (fittest.size() > 1) {
            fittest.get(Random.nextInt(0, fittest.size() - 1));
        }

        EventLogger.log("Fluctuated fitness: " + newFittest + " swapping with " + newLessFit);

        setLogFitness(i, newLessFit.byteValue(), getLogFitness(i, newFittest.byteValue()));
        setLogFitness(i, newFittest.byteValue(), 0.0);
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

    public static PurifyingFitnessFactor createEmpiricalFitnessFunction(double[] fitnesses, Feature feature, Set<Integer> sites) {
	    SequenceAlphabet alphabet = SequenceAlphabet.NUCLEOTIDES;
	    if (feature != null) {
		    if (feature.getFeatureType() == Feature.Type.AMINO_ACID) {
			    alphabet = SequenceAlphabet.AMINO_ACIDS;
		    }
	    }

        List<Byte> states = new ArrayList<Byte>();
        for (byte b = 0; b < alphabet.getStateCount(); b++) {
            states.add(b);
        }
        PurifyingFitnessRank rank = new PurifyingFitnessRank(feature, sites, states, alphabet.getStateCount(), false);
        PurifyingFitnessValuesModel model = new PurifyingFitnessValuesModel(fitnesses);

        return new PurifyingFitnessFactor(rank, model, 0, 0, feature, sites);
    }
}
