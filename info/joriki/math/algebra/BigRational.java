package info.joriki.math.algebra;

import java.math.BigInteger;

public class BigRational implements Comparable<BigRational> {
	public static BigRational ZERO = new BigRational (0);
	public static BigRational ONE  = new BigRational (1);
	
	BigInteger num;
	BigInteger den;
	
	public BigRational(long n) {
	    this (BigInteger.valueOf(n));
	}
	
	public BigRational (long num,long den) {
		this (BigInteger.valueOf (num),BigInteger.valueOf (den));
	}
	
    public BigRational (BigInteger n) {
        this (n,BigInteger.ONE);
    }

    public BigRational (BigInteger num,BigInteger den) {
		if (den.equals (BigInteger.ZERO))
			throw new IllegalArgumentException ("zero denominator");
		BigInteger gcd = num.gcd (den);
		this.num = num.divide (gcd);
		this.den = den.divide (gcd);
		
		if (this.den.signum () < 0) {
			this.den = this.den.negate ();
			this.num = this.num.negate ();
		}
	}
	
	public String toString () {
		return num + "/" + den;
	}
	
	public boolean equals (Object o) {
		BigRational r = (BigRational) o;
		return r.num.equals (num) && r.den.equals (den);
	}
	
	public static BigRational sum (BigRational r1,BigRational r2) {
		return new BigRational (r1.num.multiply (r2.den).add (r1.den.multiply (r2.num)),r1.den.multiply (r2.den));
	}
	
	public static BigRational product (BigRational r1,BigRational r2) {
		return new BigRational (r1.num.multiply (r2.num),r1.den.multiply (r2.den));
	}
	
	public BigRational negate () {
		return new BigRational (num.negate (),den);
	}

	public BigRational reciprocal () {
		if (num.equals (BigInteger.ZERO))
			throw new IllegalArgumentException ("reciprocal of zero");
		return new BigRational (den,num);
	}
	
	public BigRational pow (int exponent) {
	    return new BigRational(num.pow(exponent), den.pow(exponent));
	}
	
	public double doubleValue() {
	    boolean negative = num.signum() < 0;
	    BigInteger [] d = (negative ? num.negate() : num).divideAndRemainder(den);
	    double result = d [0].doubleValue();
	    BigInteger r = d [1];
	    if (!r.equals(BigInteger.ZERO)) {
	        double bit = 1;
	        for (;;) {
	            bit /= 2;
	            r = r.shiftLeft(1);
	            if (r.compareTo(den) >= 0) {
	                double was = result;
	                result += bit;
	                if (result == was)
	                    break;
	                r = r.subtract(den);
	                if (r.equals(BigInteger.ZERO))
	                    break;
	            }
	        }
	    }
	    return negative ? -result : result;
	}

    public int compareTo(BigRational r) {
        return num.multiply(r.den).compareTo(r.num.multiply(den));
    }
}
