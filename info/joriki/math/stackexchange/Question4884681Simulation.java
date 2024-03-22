package info.joriki.math.stackexchange;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class Question4884681Simulation {
    final static int nevents = 54;

    final static int [] mins = new int [nevents]; // lower bound (inclusive) of the outcome
    final static int [] lims = new int [nevents]; // upper bound (exclusive) of the outcome

    static {
        for (int i = 0;i < nevents;i++) { // events are indexed starting at 0
            mins [i] = i < 22 ? 0 : 1; // outcome must be at least 0 for i < 22, at least 1 otherwise
            lims [i] = i < 32 ? 6 : 8; // outcome must be less than 6 for i < 32, less than 8 otherwise
        }
    }

    static RandomGenerator random = RandomGeneratorFactory.of("L64X1024MixRandom").create ();

    public static void main (String [] args) {

        long count = 0;

        int [] events = new int [nevents];

        outer:
        for (long n = 0;;n++) {
            if ((n % 1000000) == 0)
                System.out.println (n + " : " + count / (double) n);
            for (int i = 0;i < nevents;i++)
                events [i] = random.nextInt (mins [i],lims [i]);
            for (int period = 2;3 * period <= nevents;period++) {
                inner:
                for (int start = 0;start + 3 * period <= nevents;start++) {
                    for (int i = 0,first = start;i < period;i++,first++)
                        if (events [first + period] != events [first] || events [first + 2 * period] != events [first])
                            continue inner;
                    count++;
                    continue outer;
                }
            }
        }
    }
}
