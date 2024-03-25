package info.joriki.math.stackexchange;

import info.joriki.math.random.AugmentedRandomGenerator;

public class Question4887164Simulation {
    final static AugmentedRandomGenerator random = new AugmentedRandomGenerator ();

    public static void main (String [] args) {
        outer:
        for (long total = 0,sum = 0;;total++) {
            if ((total % 10000000) == 0)
                System.out.println (sum / (double) total);

            int s = 0;
            inner:
            do {
                int roll = random.nextInt (1,7);
                switch (roll) {
                case 5: break inner;
                case 6: continue outer;
                }
                s += roll;
            } while (s < 10);

            sum += s;
        }
    }
}
