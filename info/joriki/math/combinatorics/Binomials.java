package info.joriki.math.combinatorics;

import java.math.BigInteger;

public class Binomials {
	static BigInteger [] [] binomials = new BigInteger [0] [0];
	
	public static BigInteger binomial (int n,int k) {
		if (k < 0 || n < 0)
			throw new UnsupportedOperationException ();
		if (k > n)
			return BigInteger.ZERO;

		if (n >= binomials.length) {
			BigInteger [] [] old = binomials;
			binomials = new BigInteger [n + 1] [];
			for (int i = 0;i <= n;i++)
				binomials [i] = i < old.length ? old [i] : new BigInteger [i + 1];
		}
		
		if (binomials [n] [k] == null) {
			binomials [n] [k] = BigInteger.ONE;
			for (int i = 0;i < k;i++)
				binomials [n] [k] = binomials [n] [k].multiply (BigInteger.valueOf (n - i)).divide (BigInteger.valueOf (i + 1));
		}
		return binomials [n] [k];
	}
	
	public static BigInteger multinomial (int n,int ... ks) {
	    BigInteger result = factorial (n);
	    int krest = n;
	    for (int k : ks) {
	        result = result.divide(factorial (k));
	        krest -= k;
	    }
	    return result.divide(factorial (krest));
	}
	
	static BigInteger [] factorials = new BigInteger [0];
	
	public static BigInteger factorial (int n) {
	    if (n >= factorials.length) {
	        BigInteger [] old = factorials;
	        factorials = new BigInteger [n + 1];
	        for (int i = 0;i <= n;i++)
	            factorials [i] = i < old.length ? old [i] : i == 0 ? BigInteger.ONE : factorials [i - 1].multiply(BigInteger.valueOf (i));
	    }
	    
	    return factorials [n];
	}
}
