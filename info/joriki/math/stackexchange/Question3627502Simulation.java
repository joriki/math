package info.joriki.math.stackexchange;

import info.joriki.math.random.AugmentedRandom;

public class Question3627502Simulation {
    final static long ntrials = 100000000;
    final static AugmentedRandom random = new AugmentedRandom();
    
    final static double [] t = {
            3.678862957878492, 
            2.695824358127301,
            1.7324119453416291,
            0.8048789131191986
    };
    
    public static void main(String [] args) {
        long count = 0;
        for (long trial = 0;trial < ntrials;trial++) {
            int c = 0;
            for (;;) {
                count++;
                c += random.nextPoisson(t [c]);
                if (c == t.length)
                    break;
                if (c > t.length)
                    c = 0;
            }
        }
        System.out.println(count / (double) ntrials);
    }
}
