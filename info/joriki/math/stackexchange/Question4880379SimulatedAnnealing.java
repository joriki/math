package info.joriki.math.stackexchange;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import info.joriki.math.combinatorics.Binomials;
import info.joriki.util.IntegerArray;

public class Question4880379SimulatedAnnealing {
    final static int n = 8;
    final static int m = 17;

    final static double betastep = 0.0001;
    final static double betamax = 0.002;
    final static double discount = 0.01;

    final static int target = Binomials.factorial (n).intValueExact ();

    final static Random random = new Random (3);

    static int [] [] swaps = new int [m] [2];

    public static void main (String [] args) {

        for (int i = 0;i < m;i++) {
            swaps [i] [0] = i % n;
            swaps [i] [1] = (i + 1) % n;
        }

        int last = 0;
        int max = 0;
        int count = 0;

        double E = 0;
        double E2 = 0;

        for (double beta = 0;beta < betamax;) {
            int s = 0;
            int w = 0;
            int old = 0;
            boolean swap = random.nextBoolean ();
            if (swap) {
                s = random.nextInt (m - 1);
                swap (s);
            }
            else {
                s = random.nextInt (m);
                w = random.nextInt (2);
                old = swaps [s] [w];
                int c = random.nextInt (n - 1);
                change (s,w,c);
            }

            int size = size ();
            if (size > last || random.nextDouble () < Math.exp (beta * (size - last))) {
                last = size;
                if (size == target) {
                    System.out.println ("found solution");
                    break;
                }
            }
            else {
                if (swap)
                    swap (s);
                else
                    swaps [s] [w] = old;
            }

            max = Math.max (max,last);

            E *= 1 - discount;
            E += discount * last;

            E2 *= 1 - discount;
            E2 += discount * last * last;

            if (++count > 100)
                beta += betastep / Math.sqrt (E2 - E * E);

            if ((count & 0xf) == 0)
                System.out.println(beta + " : " + last + " (" + max + ")");
        }

        System.out.println("optimizing");

        outer:
        for (;;) {
            for (int s = 0;s < m - 1;s++) {
                swap (s);
                int size = size ();
                if (size > last) {
                    System.out.println("improved from " + last + " to " + size);
                    last = size;
                    continue outer;
                }
                else
                    swap (s);
            }

            for (int s = 0;s < m;s++)
                for (int w = 0;w < 2;w++) {
                    int old = swaps [s] [w];
                    for (int c = 0;c < n - 1;c++) {
                        change (s,w,c);
                        int size = size ();
                        if (size > last) {
                            System.out.println("improved from " + last + " to " + size);
                            last = size;
                            continue outer;
                        }
                        else
                            swaps [s] [w] = old;
                    }
                }
            break;
        }

        for (int [] swap : swaps)
            System.out.print ("(" + (swap [0] + 1) + "" + (swap [1] + 1) + ")");
        System.out.println ();
        System.out.println ();

        for (int [] swap : swaps) {
            for (int i = 0;i < n;i++)
                System.out.print (i == swap [0] || i == swap [1] ? '*' : ' ');
            System.out.println ();
        }
    }

    static void swap (int s) {
        int [] t = swaps [s];
        swaps [s] = swaps [s + 1];
        swaps [s + 1] = t;
    }

    static void change (int s,int w,int c) {
        if (c >= swaps [s] [1 - w])
            c++;
        swaps [s] [w] = c;
    }

    static int size () {
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
        return found.size ();
    }
}
