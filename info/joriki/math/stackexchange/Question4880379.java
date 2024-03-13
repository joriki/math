package info.joriki.math.stackexchange;

import java.util.HashSet;
import java.util.Set;

import info.joriki.util.IntegerArray;

public class Question4880379 {

    public static void main (String [] args) {
        int factorial = 1;
        outer:
        for (int n = 2;;n++) {
            factorial *= n;
            int nswaps = (n * (n - 1)) / 2;
            for (int k = 1;;k++) {
                System.out.println ("n = " + n + ", k = " + k);
                int [] s1 = new int [k]; 
                int [] s2 = new int [k]; 
                long lim = (long) Math.pow (nswaps,k);
                boolean done = false;
                for (long swaps = 0;swaps < lim;swaps++) {
                    long left = swaps;
                    for (int i = 0;i < k;i++) {
                        int swap = (int) (left % nswaps);
                        left /= nswaps;
                        s1 [i] = (int) Math.floor (Math.sqrt (2 * swap + 0.5) + 0.5);
                        s2 [i] = swap - (s1 [i] * (s1 [i] - 1)) / 2;
                    }

                    Set<IntegerArray> found = new HashSet<>();
                    for (int bits = 0;bits < 1 << k;bits++) {
                        int [] p = new int [n];
                        for (int i = 0;i < n;i++)
                            p [i] = i;
                        for (int i = 0;i < k;i++)
                            if ((bits & (1 << i)) != 0) {
                                int h = p [s1 [i]];
                                p [s1 [i]] = p [s2 [i]];
                                p [s2 [i]] = h;
                            }
                        found.add (new IntegerArray (p));
                    }
                    if (found.size () == factorial) {
                        System.out.println("worked: n = " + n + " k = " + k + " : " + swaps);
                        continue outer;
                    }
                }
            }
        }
    }
}
