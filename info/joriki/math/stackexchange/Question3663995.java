package info.joriki.math.stackexchange;
import info.joriki.math.numerics.Vectors;
import info.joriki.math.random.AugmentedRandom;

public class Question3663995 {
    public static long ntrials = 10000000;
    public static AugmentedRandom random = new AugmentedRandom();
    public static int k = 3;
    public static int d = 3;
    public static double theta = Math.PI / 9;
    public static double dot = Math.cos(theta);
    
    public static void main(String [] args) {
        long count = 0; 
        outer:
        for (long trial = 0;trial < ntrials;trial++) {
            double [] [] x = new double [k] [];
            for (int i = 0;i < k;i++)
                x [i] = random.nextUnitVector(d);
            for (int i = 1;i < k;i++)
                for (int j = 0;j < i;j++)
                    if (Vectors.dot(x [i], x [j]) < dot)
                        continue outer;
            count++;
        }
        System.out.println(count / (double) ntrials);
    }
}
