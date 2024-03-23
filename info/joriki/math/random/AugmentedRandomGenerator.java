package info.joriki.math.random;

import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public class AugmentedRandomGenerator implements RandomGenerator {
    RandomGenerator random;

    public AugmentedRandomGenerator () {
        this (ThreadLocalRandom.current ());
    }

    public AugmentedRandomGenerator (RandomGenerator random) {
        this.random = random;
    }

    public double nextExponential (double lambda) {
        return -Math.log(random.nextDouble()) / lambda;
    }

    public int nextPoisson(double lambda) {
        int result = 0;
        lambda = Math.exp(lambda);
        for (;;) {
            lambda *= random.nextDouble();
            if (lambda < 1)
                return result;
            result++;
        }
    }

    public double [] nextUnitVector (int n) {
        double [] unitVector = new double [n];
        double norm = 0;
        for (int i = 0;i < n;i++) {
            unitVector [i] = random.nextGaussian();
            norm += unitVector [i] * unitVector [i];
        }
        norm = 1 / Math.sqrt(norm);
        for (int i = 0;i < n;i++)
            unitVector [i] *= norm;
        return unitVector;
    }

    public double [] [] nextRotationMatrix () {
        double [] [] r = new double [3] [];
        for (int i = 0;i < 3;i++)
            r [i] = nextUnitVector (3);
        for (int j = 1;j < 3;j++) {
            for (int i = 0;i < j;i++) {
                double sum = 0;
                for (int k = 0;k < 3;k++)
                    sum += r [i] [k] * r [j] [k];
                for (int k = 0;k < 3;k++)
                    r [j] [k] -= sum * r [i] [k];
            }
            double sum = 0;
            for (int k = 0;k < 3;k++)
                sum += r [j] [k] * r [j] [k];
            sum = Math.sqrt (sum);
            for (int k = 0;k < 3;k++)
                r [j] [k] /= sum;
        }
        return r;
    }

    public long nextLong () {
        return random.nextLong ();
    }
}
