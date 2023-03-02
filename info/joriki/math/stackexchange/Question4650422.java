package info.joriki.math.stackexchange;

import info.joriki.math.algebra.BigRational;
import info.joriki.math.combinatorics.Binomials;

public class Question4650422 {
    final static int n = 16;
    
    static BigRational [] [] e = new BigRational [n + 1] [n + 1];
    
    public static void main (String [] args) {
        BigRational sum = BigRational.ZERO;
        for (int i = 0;i <= n;i++)
            for (int j = 0;j <= n;j++)
            if (i != 0 || j != 0) {
                BigRational term = BigRational.product(new BigRational(Binomials.binomial(n,i).multiply(Binomials.binomial(n,j))),BigRational.sum(BigRational.ONE,BigRational.product(new BigRational(n - i,n),new BigRational (n - j,n)).negate()).reciprocal());
                if (((i + j) & 1) == 0)
                    term = term.negate();
                sum = BigRational.sum(sum,term);
            }
        
        System.out.println(sum);
        
        System.out.println (e (n,n));
    }
    
    static BigRational e (int i,int j) {
        if (i < 0 || j < 0 || (i == 0 && j == 0))
            return BigRational.ZERO;
        
        
        if (e [i] [j] == null) {
            BigRational pi = new BigRational (i,n);
            BigRational pj = new BigRational (j,n);
            BigRational pic = new BigRational (n - i,n);
            BigRational pjc = new BigRational (n - j,n);
            
            BigRational t = BigRational.ZERO;
            
            t = BigRational.sum(t,BigRational.product(BigRational.product(pi,pj),e(i - 1,j - 1)));
            t = BigRational.sum(t,BigRational.product(BigRational.product(pi,pjc),e(i - 1,j)));
            t = BigRational.sum(t,BigRational.product(BigRational.product(pic,pj),e(i,j - 1)));
            
            e [i] [j] = BigRational.product(BigRational.sum(t,BigRational.ONE),BigRational.sum(BigRational.ONE,BigRational.product(pic,pjc).negate()).reciprocal());
        }
        return e [i] [j];
    }
}
