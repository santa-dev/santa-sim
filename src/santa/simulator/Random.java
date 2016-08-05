package santa.simulator;

import java.util.Collection;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Random.java,v 1.2 2006/02/16 14:50:37 rambaut Exp $
 */
public class Random {

    public static double nextExponential(double v) {
        return randomData.nextExponential(v);
    }

    public static double nextGaussian(double v, double v1) {
        return randomData.nextGaussian(v, v1);
    }

    public static String nextHexString(int i) {
        return randomData.nextHexString(i);
    }

    public static int nextInt(int i, int i1) {
		return randomData.nextInt(i, i1);
    }

    public static long nextLong(long l, long l1) {
        return randomData.nextLong(l, l1);
    }

    public static int[] nextPermutation(int i, int i1) {
        return randomData.nextPermutation(i, i1);
    }

    public static long nextPoisson(double v) {
        return randomData.nextPoisson(v);
    }

    public static Object[] nextSample(Collection<?> collection, int i) {
        return randomData.nextSample(collection, i);
    }

    public static String nextSecureHexString(int i) {
        return randomData.nextSecureHexString(i);
    }

    public static int nextSecureInt(int i, int i1) {
        return randomData.nextSecureInt(i, i1);
    }

    public static long nextSecureLong(long l, long l1) {
        return randomData.nextSecureLong(l, l1);
    }

    public static double nextUniform(double v, double v1) {
    	return randomData.nextUniform(v, v1);
    }

    public static void shuffle(int[] numbers) {
        int[] permutation = randomData.nextPermutation(numbers.length, numbers.length);
        
        int[] numbersCopy = new int[numbers.length];
        System.arraycopy(numbers, 0, numbersCopy, 0, numbers.length);
        
        for (int i = 0; i < numbers.length; ++i) {
            numbers[i] = numbersCopy[permutation[i]];
        }
    }
    
    public static void setSeed(long seed) {
    	((RandomDataGenerator) randomData).reSeed(seed);
    }
    
	public static RandomDataGenerator randomData = new RandomDataGenerator();
}
