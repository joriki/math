package info.joriki.math.combinatorics;

import java.util.function.Consumer;

import info.joriki.math.combinatorics.Binomials;

public class Permutations {
    // generate permutations with Heap's algorithm
    public static int [] [] getPermutations(int n) {
        int [] [] permutations = new int [Binomials.factorial(n).intValue()] [];
        int index = 0;
        int [] p = new int [n];
        int [] c = new int [n];
        for (int i = 1;i < n;i++)
            p [i] = i;
        permutations [index++] = p.clone();
        for (int i = 0;i < n;) {
            if (c [i] < i) {
                int swapIndex = (i & 1) == 0 ? 0 : c [i];
                int h = p [swapIndex];
                p [swapIndex] = p [i];
                p [i] = h;
                permutations [index++] = p.clone();
                c [i]++;
                i = 0;
            }
            else {
                c [i] = 0;
                i++;
            }
        }
        return permutations;
    }

    public static void forEachPermutation(int n,Consumer<int []> consumer) {
        int [] p = new int [n];
        int [] c = new int [n];
        for (int i = 1;i < n;i++)
            p [i] = i;
        consumer.accept(p.clone());
        for (int i = 0;i < n;) {
            if (c [i] < i) {
                int swapIndex = (i & 1) == 0 ? 0 : c [i];
                int h = p [swapIndex];
                p [swapIndex] = p [i];
                p [i] = h;
                consumer.accept(p.clone());
                c [i]++;
                i = 0;
            }
            else {
                c [i] = 0;
                i++;
            }
        }
    }

}
