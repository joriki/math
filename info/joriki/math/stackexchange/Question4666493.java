package info.joriki.math.stackexchange;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Question4666493 {
    final static boolean longGame = false;
    final static int modulus = 4;
    final static int residue = 0;

    final static long ntrials = 10000000;
    final static Random random = ThreadLocalRandom.current ();

    public static void main(String [] args) {
        long count = 0;
        long turns = 0;
        for (long n = 0;n < ntrials;n++) {
            for (int k = 2;;k++) {
                turns++;
                if ((random.nextInt(k) == 0) == longGame) {
                    if (((k - 1) % modulus) == residue)
                        count++;
                    break;
                }
            }
        }
        System.out.println(turns / (double) ntrials);
        System.out.println(count / (double) ntrials);
    }
}
