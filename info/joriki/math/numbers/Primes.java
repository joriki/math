package info.joriki.math.numbers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Primes {
    private static boolean [] prime; // prime [i] : is 2i + 1 prime?

    public static void initialize (long max) {
        int length = (int) (max >> 1);
        if (prime != null && prime.length >= length)
            return;
        
        prime = new boolean [length];
        if (length == 0)
            return;
        
        Arrays.fill (prime,true);
        prime [0] = false;

        // fins primes using simple sieve of Eratosthenes
        int limit = (int) Math.sqrt (max); // highest factor to test
        for (int p = 3;p <= limit;p += 2)  // loop over odd integers
            if (prime [p >> 1])            // only test primes p
                for (int k = (3 * p) >> 1;k < prime.length;k += p) // loop over odd multiples of p
                    prime [k] = false;      // sieve them out
    }
    
    public static Map<Integer,Integer> primeFactorization (long n) {
        if (n == 0)
            throw new IllegalArgumentException();
        
        initialize(n);
        
        Map<Integer,Integer> factorization = new HashMap<>();
        int count;
        
        count = 0;
        while ((n & 1) == 0) {
            count++;
            n >>= 1;
        }

        if (count > 0)
            factorization.put(2,count);
        
        for (int k = 3;k * (long) k <= n;k += 2) {
            if (k < 0)
                throw new Error("prime overflow");
            if (prime [k >> 1]) {
                count = 0;
                while (n % k == 0) {
                    count++;
                    n /= k;
                }
                if (count > 0)
                    factorization.put(k, count);
            }
        }

        if (n > Integer.MAX_VALUE)
            throw new Error();
        
        if (n > 1) 
            factorization.put((int) n,1);
        
        return factorization;
    }
    
    public static List<Integer> primeFactors (long n) {

        initialize(n);
        
        List<Integer> factors = new ArrayList<>();

        while ((n & 1) == 0) {
            factors.add(2);
            n >>= 1;
        }
        
        for (int k = 3;k * (long) k <= n;k += 2) {
            if (k < 0)
                throw new Error("prime overflow");
            if (prime [k >> 1])
                while (n % k == 0) {
                    factors.add(k);
                    n /= k;
                }
        }

        if (n > Integer.MAX_VALUE)
            throw new Error();
        
        if (n > 1) 
            factors.add((int) n);
        
        return factors;
    }
    
    public static Stream<Integer> stream() {
        return Stream.generate(new Supplier<Integer>() {
            boolean first = true;
            int p = 1;
            public Integer get() {
                if (first) {
                    first = false;
                    return 2;
                }
                
                do
                    p += 2;
                while (!prime [p >> 1]);
                
                return p;
            }
        });
    }
}
