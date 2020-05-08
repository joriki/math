package info.joriki.math.random;

import java.util.Random;

public class AugmentedRandom extends Random {
    public AugmentedRandom () {}
    
    public AugmentedRandom (long seed) {
        super (seed);
    }
    
    public double nextExponential(double lambda) {
        return -Math.log(nextDouble()) / lambda;
    }

    public int nextPoisson(double lambda) {
        int result = 0;
        lambda = Math.exp(lambda);
        for (;;) {
            lambda *= nextDouble();
            if (lambda < 1)
                return result;
            result++;
        }
    }

    public double [] nextUnitVector(int n) {
        double [] unitVector = new double [n];
        double norm = 0;
        for (int i = 0;i < n;i++) {
            unitVector [i] = nextGaussian();
            norm += unitVector [i] * unitVector [i];
        }
        norm = 1 / Math.sqrt(norm);
        for (int i = 0;i < n;i++)
            unitVector [i] *= norm;
        return unitVector;
    }
}
