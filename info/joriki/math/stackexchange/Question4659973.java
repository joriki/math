package info.joriki.math.stackexchange;

import info.joriki.math.algebra.BigRational;

public class Question4659973 {
    final static int nsides = 6;
    final static BigRational fraction = new BigRational (1,nsides);

    public static void main (String [] args) {
        BigRational max = BigRational.ZERO;

        for (int i = 1;i <= nsides;i++)
            for (int j = i + 1;j <= i + nsides;j++)
                for (int k = j + 1;k <= j + nsides;k++) {
                    BigRational [] p = new BigRational [k + nsides];
                    p [i] = p [j] = p [k] = BigRational.ONE;
                    for (int l = 1;l < nsides;l++)
                        p [k + l] = BigRational.ZERO;
                    for (int m = k - 1;m >= 0;m--)
                        if (p [m] == null) {
                            BigRational sum = BigRational.ZERO;
                            for (int l = 1;l <= nsides;l++)
                                sum = BigRational.sum(sum,p [m + l]);
                            p [m] = BigRational.product(sum,fraction);
                        }
                    if (p [0].compareTo(max) >= 0) {
                        System.out.println(i + ", " + j + ", " + k + " : " + p [0]);
                        max = p [0];
                    }                            
                }
    }
}
