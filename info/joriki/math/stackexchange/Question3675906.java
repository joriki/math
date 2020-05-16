package info.joriki.math.stackexchange;
import java.math.BigInteger;

import info.joriki.math.combinatorics.Binomials;

public class Question3675906 {
    final static int N = 2;
    final static int m = 20;
    final static int n = 3;
    final static int k = 40;

    public static void main(String [] args) {
        int kn = Math.floorDiv(k, n);
        BigInteger sum = BigInteger.ZERO;
        for (int j = 0;j <= kn;j++) {
            BigInteger innerSum = BigInteger.ZERO;
            BigInteger sign = BigInteger.ONE;
            for (int s = 0;s <= kn - j;s++,sign = sign.negate())
                innerSum = innerSum.add (Binomials.binomial(N * m - j, s).multiply(Binomials.binomial(N * m * n - n * j - n * s, k - n * j - n * s)).multiply(sign));
            sum = sum.add(innerSum.multiply(Binomials.binomial(N, j)).multiply(BigInteger.valueOf(m).pow(j)));
        }
        System.out.println(1 - sum.doubleValue() / Binomials.binomial(N * m * n, k).doubleValue());
    }
}
