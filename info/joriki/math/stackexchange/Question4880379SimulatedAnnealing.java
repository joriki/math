package info.joriki.math.stackexchange;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import info.joriki.math.combinatorics.Binomials;
import info.joriki.util.IntegerArray;

public class Question4880379SimulatedAnnealing {
    final static double discount = 0.01;
    final static int n = 8;
    final static int m = 16;
    final static int target = Binomials.factorial (n).intValueExact ();
    
    final static Random random = new Random (243);
    
    public static void main (String [] args) {
        int [] [] swaps = new int [m] [2];
        
        for (int i = 0;i < m;i++) {
            swaps [i] [0] = i % n;
            swaps [i] [1] = (i + 1) % n;
        }

        int last = 0;
        int max = 0;
        int count = 0;
        
        double E = 0;
        double E2 = 0;

        for (double beta = 0;;) {
            int s = 0;
            int w = 0;
            int old = 0;
            boolean swap = random.nextBoolean ();
            if (swap) {
                s = random.nextInt (m - 1);
                int [] t = swaps [s];
                swaps [s] = swaps [s + 1];
                swaps [s + 1] = t;
            }
            else {
                s = random.nextInt (m);
                w = random.nextInt (2);
                old = swaps [s] [w];

                int candidate = random.nextInt (n - 1);
                if (candidate >= swaps [s] [1 - w])
                    candidate++;
                swaps [s] [w] = candidate;
            }

            Set<IntegerArray> found = new HashSet<>();
            for (int bits = 0;bits < 1 << m;bits++) {
                int [] p = new int [n];
                for (int i = 0;i < n;i++)
                    p [i] = i;
                for (int i = 0;i < m;i++)
                    if ((bits & (1 << i)) != 0) {
                        int h = p [swaps [i] [0]];
                        p [swaps [i] [0]] = p [swaps [i] [1]];
                        p [swaps [i] [1]] = h;
                    }
                found.add (new IntegerArray (p));
            }
            int size = found.size ();
            if (size == target) {
                System.out.println ("found solution");
                return;
            }
            if (size > last || random.nextDouble () < Math.exp (beta * (size - last)))
                last = size;
            else {
                if (swap) {
                    int [] t = swaps [s];
                    swaps [s] = swaps [s + 1];
                    swaps [s + 1] = t;
                }
                else
                    swaps [s] [w] = old;
            }

            max = Math.max (max,last);

            E *= 1 - discount;
            E += discount * last;

            E2 *= 1 - discount;
            E2 += discount * last * last;

            if (++count > 100)
                beta += 0.0001 / Math.sqrt (E2 - E * E);

            if ((count & 0xf) == 0)
                System.out.println(beta + " : " + last + " (" + max + ")");
        }
    }
}
