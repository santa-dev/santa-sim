package santa.simulator.genomes;

import santa.simulator.fitness.FitnessFunction;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: BaseGenome.java,v 1.5 2006/07/19 12:53:05 kdforc0 Exp $
 */
public abstract class BaseGenome implements Genome {

    public BaseGenome() {
        this.fitnessCache = null;
    }

    public int getTotalMutationCount() {
        return totalMutationCount;
    }

    public void setTotalMutationCount(int totalMutationCount) {
        this.totalMutationCount = totalMutationCount;
    }

    protected void incrementTotalMutationCount() {
        totalMutationCount++;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void incrementFrequency() {
        frequency++;
    }

    public double getLogFitness() {
        return logFitness;
    }

    public void setLogFitness(double logFitness) {
        if (logFitness != this.logFitness) {
            this.logFitness = logFitness;
            fitnessKnown = false;
        }
    }

    public double getFitness() {
        if (!fitnessKnown) {
            fitness = Math.exp(logFitness);
            fitnessKnown = true;
        }
        return fitness;
    }

    public FitnessFunction.FitnessGenomeCache getFitnessCache() {
        return fitnessCache;
    }

    public void setFitnessCache(FitnessFunction.FitnessGenomeCache fitnessCache) {
        this.fitnessCache = fitnessCache;
    }

	public byte[] getNucleotides(Feature coords) {
		byte[] nucleotides = new byte[coords.getLength()];
		int k = 0;
		for (int i = 0; i < coords.getFragmentCount(); i++) {
			int start = coords.getFragmentStart(i);
			int finish = coords.getFragmentFinish(i);
			if (start < finish) {
				for (int j = start; j <= finish; j++) {
					nucleotides[k] = getNucleotide(j);
					k++;
				}
			} else {
				for (int j = finish; j >= start; j--) {
					nucleotides[k] = getNucleotide(j);
					k++;
				}
			}
		}

		return nucleotides;
	}

	public byte[] getStates(SequenceAlphabet alphabet, Feature coords) {
		Sequence seq = new SimpleSequence(getNucleotides(coords));
		return seq.getStates(alphabet);
	}

    // private members

    private double logFitness = 0.0;
    private double fitness = 1.0;
    private boolean fitnessKnown = false;

    private int frequency = 0;
    private int totalMutationCount = 0;

    FitnessFunction.FitnessGenomeCache fitnessCache;
}
