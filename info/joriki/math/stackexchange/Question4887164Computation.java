package info.joriki.math.stackexchange;

import info.joriki.math.algebra.BigRational;

public class Question4887164Computation {
    public static void main (String [] args) {
        BigRational [] a = new BigRational [14];

        for (int s = 10;s <= 13;s++)
            a [s] = new BigRational (s - 5,2);

        for (int s = 9;s >= 0;s--) {
            BigRational sum = BigRational.ZERO;
            for (int k = 1;k <= 4;k++)
                sum = BigRational.sum (sum,a [s + k]);
            a [s] = BigRational.product (sum,new BigRational (1,6));
            System.out.println (s + "&" + a [s].toTeXFractionString () + "\\\\");
        }
    }
}
