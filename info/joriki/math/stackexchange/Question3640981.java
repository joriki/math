package info.joriki.math.stackexchange;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import info.joriki.math.numbers.ArithmeticFunctions;
import info.joriki.math.numbers.Primes;

public class Question3640981 {
    static Long pk;
    
    static Map<BigInteger,BigInteger> f = new HashMap<>();
    static Map<BigInteger,BigInteger> inverse = new HashMap<>();
    
    static void put(BigInteger n,BigInteger fn) {
        f.put(n, fn);
        inverse.put(fn, n);
        System.out.println("    " + n + " -> " + fn + " -> " + f.get(fn));
    }
    
    public static void main(String [] args) {
        Primes.initialize(0x800000);
        
        put(BigInteger.ONE,BigInteger.ONE);
        put(BigInteger.TWO,BigInteger.TWO);

        for (long n = 3;;n++) {
            BigInteger bn = BigInteger.valueOf(n);
            if (f.containsKey(bn))
                continue;
            
            long d = ArithmeticFunctions.d(n);
            BigInteger bd = BigInteger.valueOf(d);
            BigInteger result = inverse.get(bd);
            
            if (result != null)
                put(bn,result);
            else
                Primes.stream().filter(new Predicate<Integer>() {
                    public boolean test(Integer p) {
                        BigInteger pk = BigInteger.valueOf(p).pow(f.get(bd).intValueExact() - 1);
                        boolean free = !f.containsKey(pk);
                        if (free) {
                            put (pk,bd);
                            put (bn,pk);
                        }
                        return free;
                    }
                }).findFirst();
        }
    }
}
