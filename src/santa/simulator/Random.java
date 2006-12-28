package santa.simulator;

import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomData;

import java.util.Collection;

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

    public static Object[] nextSample(Collection collection, int i) {
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

	public static RandomData randomData = new RandomDataImpl();

}
