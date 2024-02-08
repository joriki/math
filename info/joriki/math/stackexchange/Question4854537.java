package info.joriki.math.stackexchange;

import java.math.BigInteger;

import info.joriki.math.algebra.BigRational;
import info.joriki.math.combinatorics.Binomials;

public class Question4854537 {
    final static int n = 10;
    final static int k = 20;
    
    public static void main(String [] args) {
        for (int i = 0;i <= n && i <= k;i++) {
            BigInteger sum = BigInteger.ZERO;
            for (int j = 0;j <= n - i && j <= k - i;j++) {
                BigInteger term = Binomials.factorial(j).multiply(multinomial (n,i,j)).multiply(multinomial (k,i,j)).multiply(BigInteger.valueOf(k - i - j).pow(n - i - j));
                if ((j & 1) == 1)
                    term = term.negate();
                sum = sum.add(term);
            }
            BigRational p = new BigRational (sum.multiply(Binomials.factorial(i)),BigInteger.valueOf(k).pow(n));
            System.out.println(p);
        }
                
    }
    
    static BigInteger multinomial (int m,int i,int j) {
        return Binomials.binomial(m,i).multiply(Binomials.binomial(m - i,j));
    }
}
