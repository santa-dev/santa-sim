package santa.simulator.fitness;

import santa.simulator.genomes.SequenceAlphabet;

public class PurifyingFitnessPiecewiseLinearModel implements PurifyingFitnessModel {


    private SequenceAlphabet alphabet;
    private double minFitness;
    private double lowFitness;

    public PurifyingFitnessPiecewiseLinearModel(SequenceAlphabet alphabet,
            double minFitness, double lowFitness) {
        this.alphabet = alphabet;
        this.minFitness = minFitness;
        this.lowFitness = lowFitness;
    }

    public double[] getFitnesses(int site, PurifyingFitnessRank rank) {
        int probable = rank.getProbableSetSize(site);

        double[] fitnesses = new double[alphabet.getStateCount()];

        fitnesses[0] = 1.;
        for (int i = 1; i < alphabet.getStateCount(); ++i) {
            if (i < probable) {
                fitnesses[i] = 1. - (1. - lowFitness)*(i/(probable-1.0));
            } else 
                fitnesses[i] = minFitness;
        }

        return fitnesses;
    }
}
