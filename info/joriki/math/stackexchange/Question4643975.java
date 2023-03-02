package info.joriki.math.stackexchange;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import info.joriki.math.combinatorics.Binomials;
import info.joriki.math.numbers.Primes;

public class Question4643975 {
    static BigInteger [] binomials; 
    static BigInteger [] residues; 

    static Set<BitSet> even;
    static Set<BitSet> odd;
    
    static Map<BitSet,BigInteger> evenMap;
    static Map<BitSet,BigInteger> oddMap;
    
    static BigInteger modulo;
    
    public static void main(String [] args) {
        Primes.initialize(10000);

        for (int n = 5;;n += 2) {
            if (Primes.isPrime(n) || n % 2 == 0 || n % 3 == 0)
                continue;
            int half = (n + 1) >> 1;
            binomials = new BigInteger [half];
            residues = new BigInteger [half];
            for (int i = 0;i < half;i++)
                binomials [i] = Binomials.binomial(n - 1, i);
            modulo = BigInteger.ONE;
            even = Set.of(new BitSet());
            odd = Set.of(new BitSet());
            int max = 0;
            int p;
            for (p = n - 2;p + p > n;p -= 2)
                if (Primes.isPrime(p)) {
                    modulo = modulo.multiply(BigInteger.valueOf(p));
                    for (int i = 0;i < half;i++)
                        residues [i] = binomials [i].remainder(modulo);
                    recurse (max,n - p,false);
                    max = n - p;
                }
            recurse (max,n >> 1,true);
            for (;p > 1;p -= 2)
                if (Primes.isPrime(p)) {
                    modulo = modulo.multiply(BigInteger.valueOf(p));
                    for (int i = 0;i < half;i++)
                        residues [i] = binomials [i].remainder(modulo);
                    recurse (max,max,false);
                }
            residues = binomials;
            modulo = null;
            Set<BigInteger> lastIntersection = recurse (max,max,false);
            System.out.println(n + " : " + lastIntersection.size() + " : " + lastIntersection);
            System.out.println(even);
            System.out.println(odd);
            System.out.println();
        }
    }
    
    static Set<BigInteger> recurse (int max,int newMax,boolean last) {
        evenMap = new HashMap<>();
        oddMap = new HashMap<>();
        for (BitSet bits : even)
            recurse (max,newMax,bits,last,0);
        for (BitSet bits : odd)
            recurse (max,newMax,bits,last,1);
        Set<BigInteger> intersection = new HashSet<>(evenMap.values());
        intersection.retainAll(new HashSet<>(oddMap.values()));
        even = candidates (evenMap,intersection);
        odd = candidates (oddMap,intersection);
        return intersection;
    }
    
    static void recurse (int max,int newMax,BitSet bits,boolean last,int parity) {
        recurse (max,newMax,(BitSet) bits.clone(),getSum(bits,parity),last && (newMax & 1) == parity,parity);
    }
    
    static BigInteger getSum (BitSet bits,int parity) {
        BigInteger sum = BigInteger.ZERO;
        for (int k = 0;k < bits.length();k++)
            if (bits.get(k))
                sum = sum.add(residues [(k & ~1) + parity]);
        return sum;
    }
    
    static void recurse (int max,int newMax,BitSet bits,BigInteger sum,boolean last,int parity) {
        if (modulo != null)
            while (sum.compareTo(modulo) >= 0)
                sum = sum.subtract(modulo);
        
        boolean single = last && max + parity == newMax;
        if (max + parity >= newMax && !single)
          (parity == 0 ? evenMap : oddMap).put((BitSet) bits.clone(),sum);
        else {
            int lim = single ? 1 : 2;
            for (int k = 0;k <= lim;k++) {
                recurse (max + 2,newMax,bits,sum,last,parity);
                bits.set(max + k);
                sum = sum.add(residues [max + parity]);
            }
            for (int k = 0;k <= lim;k++)
                bits.clear(max + k);
        }
    }

    static Set<BitSet> candidates (Map<BitSet,BigInteger> map,Set<BigInteger> intersection) {
        Set<BitSet> candidates = new HashSet<>();
        for (BitSet candidate : map.keySet())
            if (intersection.contains(map.get(candidate)))
                candidates.add(candidate);
        return candidates;
    }
}
