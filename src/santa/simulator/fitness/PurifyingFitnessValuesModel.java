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
        Arrays.sort(values);
        this.values = new double[values.length];
        for (int i = 0; i < values.length; ++i)
            this.values[i] = values[values.length - i - 1];
    }
    
    public double[] getFitnesses(int site, PurifyingFitnessRank rank) {
        return values;
    }

}
