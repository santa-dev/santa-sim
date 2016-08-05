package santa.simulator.fitness;

import java.util.ArrayList;
import java.util.Collections;

import santa.simulator.Random;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.distribution.BetaDistribution;

/**
 * A distribution of fitness effects motivated by
 *
 * Carrasco et al (2007) J Virology 81:12979-12984
 *
 *
 * @author Alexei Drummond
 */
public class BetaDistributedPurifyingFitnessModel implements PurifyingFitnessModel {

    BetaDistribution beta;
    double pLethal;

    double[][] fitnesses;
    int alphabetSize;

    public BetaDistributedPurifyingFitnessModel(double a, double b, double pLethal, int alphabetSize, int sites) {

        this.alphabetSize = alphabetSize;
        this.pLethal = pLethal;

        fitnesses = new double[sites][alphabetSize];

        beta = new BetaDistribution(Random.randomData.getRandomGenerator(), a, b);
        

        for (int i = 0; i < sites; i++) {

            ArrayList<Double> f = new ArrayList<Double>(alphabetSize);
            f.add(1.0);
            for (int j = 1; j < alphabetSize; j++) {

                if (Math.random() < pLethal) {
                    f.add(0.0);
                } else {
                    try {
						f.add(beta.inverseCumulativeProbability(Math.random()));
					} catch (OutOfRangeException e) {
						e.printStackTrace();
					}                    
                }
            }
            Collections.sort(f);
            //System.out.println(f.get(alphabetSize-1));
            for (int j = 0; j < alphabetSize; j++) {
                fitnesses[i][j] = f.get(alphabetSize-j-1);
            }
        }
    }

    public double[] getFitnesses(int site, PurifyingFitnessRank rank) {
        return fitnesses[site];
    }
}
