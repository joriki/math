package info.joriki.math.stackexchange;

import java.math.BigInteger;

import info.joriki.math.combinatorics.Binomials;

public class Question406716 {
    public static void main(String [] args) {
        for (int n = 1;;n++) {
            BigInteger sum = Binomials.factorial(n);
            for (int k = 1;k < n;k++) {
                BigInteger part = BigInteger.ZERO;
                for (int r = 1;r <= k;r++)
                    part = part.add(Binomials.binomial(n - k,r).multiply(Binomials.binomial(k - 1,r - 1)).multiply(Binomials.factorial(n - k)).multiply(BigInteger.TWO.pow(r)));
                if ((k & 1) == 1)
                    part = part.negate();
                sum = sum.add(part);
            }
            System.out.println(n + " : " + sum);
        }
    }
}
