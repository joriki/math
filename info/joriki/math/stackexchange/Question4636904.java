package info.joriki.math.stackexchange;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import info.joriki.math.algebra.BigRational;
import info.joriki.math.numbers.Divisors;
import info.joriki.math.numbers.Primes;

public class Question4636904 {
    static List<Integer> palindromeList = new ArrayList<>();
    static int [] palindromes = {};
    static int max;
    
    static int length = 2;
    
    final static int limit = 200;
    
    static int powerOfTen (int n) {
        return BigInteger.TEN.pow(n).intValue();
    }
    
    static void extend() {
        int half = (length + 1) >> 1;
        for (int i = powerOfTen(half - 1);i < powerOfTen(half);i++) {
            String s = String.valueOf(i);
            String t = reverse(s);
            if ((length & 1) == 1)
                s = s.substring(0,half - 1);
            String p = s + t;
            if (p.length() != length)
                throw new Error();
            max = Integer.parseInt(p);
            if (palindromeList.size() == limit)
                System.out.println("limit: " + max);
            palindromeList.add(max);
        }
        length++;
        
        palindromes = palindromeList.stream().mapToInt(i -> i).toArray();
    }

    static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }
    
    static boolean isPalindrome(BigInteger n) {
        String s = n.toString();
        return s.equals(reverse (s));
    }
    
    static Stack<Number> solution = new Stack<>();
    static Set<String> solutions = new TreeSet<>();

    public static void main(String [] args) {
        Primes.initialize(1000000);
        
        for (int n = 10;n <= 100;n++) {
            solutions.clear();
            if (isPalindrome(BigInteger.valueOf(n))) {
                solution.push(n);
                addSolution();
                solution.pop();
            }
            else
                for (int l = 2;l <= 6 && solutions.isEmpty();l++)
                    recurse (new BigRational(1,n),l,0);
            System.out.println("\\frac1{" + n + "}");
            for (String solution : solutions)
                System.out.println("&=&" + solution + "\\\\");
            System.out.println("[8pt]");
        }
    }
    
    static void addSolution() {
        Map<BigInteger,Integer> counts = new TreeMap<>();
        for (Number denominator : solution)
            counts.compute(denominator instanceof BigInteger ? (BigInteger) denominator : BigInteger.valueOf(denominator.intValue()),(k,v) -> v == null ? 1 : v + 1);
        solutions.add(counts.keySet().stream().map(
                denominator -> "\\frac" + counts.get(denominator) + "{" + denominator + "}"
                ).collect(Collectors.joining(" + ")));
    }
    
    static boolean isAdmissible (BigInteger [] div) {
        return div [1].signum() == 0 && div [0].signum() != 0 && isPalindrome (div [0]);
    }
    
    static void recurse (BigRational r,int l,int i) {
        if (l == 2) {
            BigInteger p = r.num;
            BigInteger q = r.den;
            BigInteger q2 = q.multiply(q);
            for (BigInteger divisor1 : Divisors.divisors(q2)) {
                BigInteger [] a = divisor1.add(q).divideAndRemainder(p);
                if (isAdmissible(a)) {
                    BigInteger divisor2 = q2.divide(divisor1);
                    if (divisor2.compareTo(divisor1) >= 0) {
                        BigInteger [] b = divisor2.add(q).divideAndRemainder(p);
                        if (isAdmissible(b)) {
                            solution.push(a [0]);
                            solution.push(b [0]);
                            addSolution();
                            solution.pop();
                            solution.pop();
                        }
                    }
                }
            }
        }
        else {
            int next;
            try {
                next = palindromes [i];
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                extend();
                next = palindromes [i];
            }
            if (r.compareTo(new BigRational(l,next)) > 0) // not enough left
                return;
            BigRational left = BigRational.sum(r,new BigRational(-1,next));
            int c = left.compareTo(BigRational.ZERO);
            if (c == 0)
                throw new Error();
            if (c > 0) {
                solution.push(next);
                recurse (left,l - 1,i);
                solution.pop();
            }
            if (i < limit)
                recurse (r,l,i + 1);
        }
    }
}