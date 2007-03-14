/*
 * Created on Mar 12, 2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

public interface PurifyingFitnessModel {

    /**
     * @return 20 fitness values, in decreasing order, all between 0 and 1.
     *            may use the rank to obtain an estimate for observed counts to shape the model
     *            properly
     */
    double[] getFitnesses(int site, PurifyingFitnessRank rank);
    
}
