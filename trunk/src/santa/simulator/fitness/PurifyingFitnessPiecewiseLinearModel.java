package santa.simulator.fitness;

import santa.simulator.genomes.SequenceAlphabet;

public class PurifyingFitnessPiecewiseLinearModel implements PurifyingFitnessModel {

    // BIOCHEMICAL should be somewhere in rank, and OBSERVED should be renamed
    // to ESTIMATED ? which in case of an alignment simply takes those observed,
    // and in case of a BIOCHEMICAL expansion, takes those biochemically compatible
    public static enum ProbableSetEnum {
        OBSERVED, NUMBER;
    }

    private SequenceAlphabet alphabet;
    private double minFitness;
    private double lowFitness;
    private ProbableSetEnum probableSet;
    private int probableNumber;

    public PurifyingFitnessPiecewiseLinearModel(SequenceAlphabet alphabet,
            double minFitness, double lowFitness, ProbableSetEnum probableSet, int probableNumber) {
        this.alphabet = alphabet;
        this.minFitness = minFitness;
        this.lowFitness = lowFitness;
        this.probableSet = probableSet;
        this.probableNumber = probableNumber;
    }

    public double[] getFitnesses(int site, PurifyingFitnessRank rank) {
        int probable = 0;
        
        switch (probableSet) {
        case OBSERVED:
            probable = rank.getProbableSetSize(site);
            break;
        case NUMBER:
            probable = probableNumber;
        }
    
        double[] fitnesses = new double[alphabet.getStateCount()];

        fitnesses[0] = 1.;
        for (int i = 1; i < alphabet.getStateCount(); ++i) {
            if (i < probable) {
                fitnesses[i] = 1. - (1. - lowFitness)*(i/(probable-1));
            } else
                fitnesses[i] = minFitness;
        }

        return fitnesses;
    }
}
