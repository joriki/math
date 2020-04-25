package info.joriki.math.numbers;

import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;

public class ArithmeticFunctions {
    public static int mu(long n) {
        Map<Integer,Integer> primeFactorization = Primes.primeFactorization(n);
        for (int exponent : primeFactorization.values())
            if (exponent > 1)
                return 0;

        return 1 - 2 * (primeFactorization.size() & 1);
    }

    public static long d (long n) {
        long result = 1;
        for (int exponent : Primes.primeFactorization(n).values())
            result *= exponent + 1;
        return result;
    }
    
    public static BigInteger sigma(int i,long n) {
        if (i == 0)
            return BigInteger.valueOf(d(n));
        
        BigInteger result = BigInteger.ONE;
        for (Entry<Integer,Integer> entry : Primes.primeFactorization(n).entrySet()) {
            BigInteger p = BigInteger.valueOf(entry.getKey());
            BigInteger pi = p.pow(i);
            result = result.multiply(pi.pow(entry.getValue() + 1).subtract(BigInteger.ONE).divide(pi.subtract(BigInteger.ONE)));
        }
        return result;
    }
}
