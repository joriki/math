package info.joriki.math.combinatorics;

import java.util.function.LongConsumer;

public class Combinations {
    private Combinations () {}

    public static void forEachCombination (int n,int k,LongConsumer consumer) {
        forEachCombination (n,k,0,0,consumer);
    }

    private static void forEachCombination (int n,int k,int j,long combination,LongConsumer consumer) {
        if (k == 0)
            consumer.accept (combination);
        else {
            k--;
            for (int i = j;i < n - k;i++)
                forEachCombination (n,k,i + 1,combination | (1L << i),consumer);
        }
    }

    public static long [] combinations (int n,int k) {
        final long [] combinations = new long [Binomials.binomial (n,k).intValueExact ()];
        forEachCombination (n,k,new LongConsumer () {
            int i;

            public void accept (long combination) {
                combinations [i++] = combination;
            }
        });
        return combinations;
    }
}
