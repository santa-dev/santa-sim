package santa.simulator;

import java.util.Collection;
import java.util.List;
import java.util.Collections;
import java.util.ListIterator;
import java.util.RandomAccess;

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
    
    public static int nextBinomial(int n, double p) {
        return randomData.nextBinomial(n, p);
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
    
    // From java.util.Collections
    // Added so that all random calls go through this class
    public static void shuffle(List<?> list) {
        int size = list.size();
        if (size < 5000 || list instanceof RandomAccess) {
            for (int i=size; i>1; i--)
                Collections.swap(list, i-1, nextInt(0, i - 1));
        } else {
            Object arr[] = list.toArray();

            // Shuffle array
            for (int i=size; i>1; i--)
                swap(arr, i-1, nextInt(0, i - 1));

            // Dump array back into list
            // instead of using a raw type here, it's possible to capture
            // the wildcard but it will require a call to a supplementary
            // private method
            ListIterator it = list.listIterator();
            for (int i=0; i<arr.length; i++) {
                it.next();
                it.set(arr[i]);
            }
        }
    }
    
    /**
     * From java.util.Collections
     * Swaps the two specified elements in the specified array.
     */
    private static void swap(Object[] arr, int i, int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
    
    public static void setSeed(long seed) {
    	((RandomDataGenerator) randomData).reSeed(seed);
    }
    
	public static RandomDataGenerator randomData = new RandomDataGenerator();
}
