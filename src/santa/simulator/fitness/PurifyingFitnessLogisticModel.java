/*
 * Created on Mar 13, 2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import santa.simulator.genomes.SequenceAlphabet;

public class PurifyingFitnessLogisticModel implements PurifyingFitnessModel {

    private SequenceAlphabet alphabet;
    private double minimumFitness;
    private double mostFitCount;
    private double leastFitCount;

    public PurifyingFitnessLogisticModel(SequenceAlphabet alphabet, double minimumFitness, double mostFitCount, double leastFitCount) {
        this.alphabet = alphabet;
        this.minimumFitness = minimumFitness;
        this.mostFitCount = mostFitCount;
        this.leastFitCount = leastFitCount;
    }

    public double[] getFitnesses() {
        // TODO Auto-generated method stub
        return null;
    }

}
