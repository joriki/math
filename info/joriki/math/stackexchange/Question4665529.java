package info.joriki.math.stackexchange;

import java.math.BigInteger;

import info.joriki.math.combinatorics.Binomials;

public class Question4665529 {
    public static void main(String [] args) {
        for (int n = 1;;n++) {
            long mask = (1L << (n + 1)) - 1;
            long count = 0;
            outer:
            for (long bits = 0;bits < 1L << (2 * n);bits++) {
                long b = bits * (1L + (1L << (2 * n)));
                for (int i = 0;i < 2 * n;i++) {
                    long s = b >> i;
                    long t = s & 7;
                    if (t == 2 || t == 5)
                        continue outer;
                    t = s & mask;
                    if (t == 0 || t == mask)
                        continue outer;
                }
                count++;
            }
            BigInteger sum = BigInteger.ZERO;

            for (int k = 0;4 * k <= n - 3;k++)
                sum = sum.add(Binomials.binomial(n - 2 * k - 2,2 * k + 1));

            sum = sum.multiply(BigInteger.valueOf(2 * n)).negate();

            for (int k = 0;2 * k <= n - 2;k++)
                for (int j = 1;j <= 2;j++)
                    sum = sum.add(Binomials.binomial(2 * n - 2 * k - 4 + j,2 * k + j));

            sum = sum.multiply(BigInteger.TWO);

            if (sum.longValueExact() != count)
                throw new Error();

            System.out.println(n + "&" + count + "\\\\");
        }
    }
}
