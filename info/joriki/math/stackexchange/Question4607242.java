package info.joriki.math.stackexchange;

import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public class Question4607242 {
    final static int ntrials = 100000000;
    final static RandomGenerator random = ThreadLocalRandom.current ();

    final static int [] [] dice = {
            {2,2,4,4,1,0,7},
            {0,0,0,0,4,5,11},
    };

    final static int [] required = {0xf,0x3f};

    public static void main(String [] args) {
        long count = 0;

        for (int n = 0;n < ntrials;n++) {
            int obtained = 0;
            for (int die = 0;die < 2;die++)
                do {
                    count++;
                    int roll = random.nextInt(20);
                    for (int i = 0;i < 7;i++) {
                        roll -= dice [die] [i];
                        if (roll < 0) {
                            obtained |= 1 << i;
                            break;
                        }
                    }
                } while ((obtained & required [die]) != required [die]);
        }

        System.out.println(count / (double) ntrials);
    }
}
