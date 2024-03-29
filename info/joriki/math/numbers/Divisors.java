package info.joriki.math.numbers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import info.joriki.util.ListBuilder;

public class Divisors {
    public static void traverseDivisors(long n,Consumer<Long> consumer) {
        Map<Integer,Integer> primeFactorization = Primes.primeFactorization(n);
        List<Integer> primeFactors = new ArrayList<>(primeFactorization.keySet());
        recurse(consumer,primeFactorization,primeFactors,1,0);
    }
    
    public static List<Long> divisors(long n) {
        ListBuilder<Long> builder = new ListBuilder<>();
        traverseDivisors(n, builder);
        return builder.build();
    }
    
    private static void recurse(Consumer<Long> consumer,Map<Integer,Integer> primeFactorization,List<Integer> primeFactors,long divisor,int index) {
        if (index == primeFactorization.size())
            consumer.accept(divisor);
        else {
            int p = primeFactors.get(index++);
            for (int i = 0;i <= primeFactorization.get(p);i++,divisor *= p)
                recurse(consumer,primeFactorization,primeFactors,divisor,index);
        }
    }
    
    public static List<BigInteger> divisors(BigInteger i) {
        List<BigInteger> divisors = new ArrayList<>();
        Map<Integer,Integer> primeFactorization = Primes.primeFactorization(i);
        List<Integer> primeFactors = new ArrayList<>(primeFactorization.keySet());
        recurse (divisors,primeFactorization,primeFactors,BigInteger.ONE,0);
        return divisors;
    }

    private static void recurse(List<BigInteger> divisors,Map<Integer,Integer> primeFactorization,List<Integer> primes,BigInteger divisor,int index) {
        if (index == primes.size())
            divisors.add(divisor);
        else {
            int prime = primes.get(index++);
            int count = primeFactorization.get(prime);
            BigInteger bigPrime = BigInteger.valueOf(prime);
            for (int i = 0;i <= count;i++) {
                recurse (divisors,primeFactorization,primes,divisor,index);
                divisor = divisor.multiply(bigPrime);
            }
        }
    }

    public static long gcd (long a,long b) {
        return BigInteger.valueOf (a).gcd (BigInteger.valueOf (b)).longValueExact ();
    }

    public static long gcd (long ... a) {
        return gcd (LongStream.of (a).mapToObj (l -> BigInteger.valueOf (l)).toArray (BigInteger []::new)).longValueExact ();
    }

    public static BigInteger gcd (BigInteger ... i) {
        return Stream.of (i).reduce ((a,b) -> a.gcd (b)).orElse (BigInteger.ZERO);
    }

    public static long lcm (long a,long b) {
        return a * (b / gcd (a,b));
    }

    public static int lcm (int ... l) {
        return IntStream.of (l).reduce ((a,b) -> (int) lcm (a,b)).orElse (1);
    }

    public static long lcm (long ... l) {
        return LongStream.of (l).reduce ((a,b) -> lcm (a,b)).orElse (1);
    }
}
