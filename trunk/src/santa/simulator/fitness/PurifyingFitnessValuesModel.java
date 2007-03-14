/*
 * Created on Mar 13, 2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package santa.simulator.fitness;

import java.util.Arrays;

public class PurifyingFitnessValuesModel implements PurifyingFitnessModel {
    
    private double[] values;

    public PurifyingFitnessValuesModel(double[] values) {
        this.values = values;
        Arrays.sort(this.values);
    }
    
    public double[] getFitnesses(int site, PurifyingFitnessRank rank) {
        return values;
    }

}
