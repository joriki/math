package info.joriki.math.stackexchange;

import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public class Question3652964 {
    final static int ntrials = 100000000;
    final static RandomGenerator random = ThreadLocalRandom.current ();

    public static void main(String [] args) {
        long last = 0;
        for (int n = 0;n < ntrials;n++) {
            int [] counts = new int [5];
            int count = 0;
            for (;;) {
                int bin = random.nextInt(6);
                if (bin == 5)
                    break;
                if (++counts [bin] == 3 && ++count == 5) {
                    last++;
                    break;
                }
            }
        }
        System.out.println(last / (double) ntrials);
    }
}
