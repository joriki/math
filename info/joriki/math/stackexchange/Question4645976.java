package info.joriki.math.stackexchange;

import info.joriki.math.algebra.BigRational;

public class Question4645976 {
    public static void main(String [] args) {
        System.out.println (t (0));
    }
    
    static BigRational [] cache = new BigRational [1000]; 
    
    static BigRational t (int x) {
        if (x >= 1000)
            return BigRational.ZERO;
        if (cache [x] == null)
            cache [x] = computeT (x);
        return cache [x];
    }
    
    static BigRational computeT (int x) {
        BigRational p;
        if (x < 300)
            p = BigRational.ZERO;
        else if (x < 500)
            p = new BigRational ((x - 300) * 15 + 2000,10000);
        else
            p = new BigRational (x,1000);
        
        p = BigRational.sum(BigRational.ONE,p.negate());
        
        int g = Math.max(1, (int) Math.round((1000 - x) / 8.6));
        
        BigRational attempt = BigRational.product(p,BigRational.sum(t(x + g),new BigRational(2 * g)));
        BigRational wait = BigRational.sum(BigRational.ONE,t (x + 1));
        
        boolean shouldWait = attempt.compareTo(wait) > 0;
        
        BigRational t = shouldWait ? wait : attempt;

        System.out.println(x + " : " + (shouldWait ? "wait" : "attempt (next attempt at " + (x + g) + ")") + " --> " + t);
        
        return t;
    }
}
