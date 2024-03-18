package info.joriki.math.stackexchange;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Question4882983Simulation {
    final static long ntrials = 100000000000L;

    final static Random random = ThreadLocalRandom.current();

    public static void main (String [] args) {
        long [] counts = new long [4];
        for (long n = 0;n < ntrials;n++) {
            int start = 0;
            int length = 0;
            int last = 6;

            for (;;) {
                int roll = random.nextInt (6);

                if (roll > last) {
                    if (++length == 3) {
                        counts [start]++;
                        break;
                    }
                }
                else {
                    length = 1;
                    start = roll;
                }

                last = roll;
            }
        }

        for (long c : counts)
            System.out.println (c / (double) ntrials);
    }
}
