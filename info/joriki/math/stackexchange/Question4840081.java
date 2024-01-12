package info.joriki.math.stackexchange;

import java.util.Comparator;

import info.joriki.math.algebra.BigRational;
import info.joriki.math.combinatorics.Permutations;

public class Question4840081 {
    static Comparator<BigRational> lexical = new Comparator<>() {
        public int compare(BigRational r,BigRational s) {
            int result = r.den.compareTo(s.den);
            return result != 0 ? result : r.num.compareTo(s.num);
        }
    };

    public static void main(String [] args) {
        for (int b = 2;b < 10;b++) {
            long denominator = (long) Math.pow(b, b);
            BigRational min = new BigRational (1,denominator);
            denominator--;

            for (int [] p : Permutations.getPermutations(b)) {
                long n = 0;
                for (int d : p) {
                    n *= b;
                    n += d;
                }
                BigRational r = new BigRational (n,denominator);
                if (lexical.compare(r,min) < 0)
                    min = r;
            }
            String num = Long.toString(min.num.longValueExact(),b);
            String den = Long.toString(min.den.longValueExact(),b);
            System.out.println(den + "_" + b + "x + x^n&=&" + num + "_" + b + "\\\\");
        }
    }
}
