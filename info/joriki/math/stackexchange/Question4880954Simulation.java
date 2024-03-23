package info.joriki.math.stackexchange;

import info.joriki.math.combinatorics.Combinations;
import info.joriki.math.random.AugmentedRandomGenerator;

public class Question4880954Simulation {
    public static void main (String [] args) {
        AugmentedRandomGenerator random = new AugmentedRandomGenerator ();

        long [] combinations = Combinations.combinations (5,3);

        double [] [] x = new double [5] [];
        double [] [] triple = new double [3] [];
        double [] [] points = new double [2] [];
        double [] [] inverse = new double [3] [3];

        long count = 0;

        outer:
        for (long n = 0;;n++) {
            if (n % 1000000 == 0)
                System.out.println (n + " : " + count / (double) n);

            // vectors from the north pole to five uniformly random points on the sphere
            for (int i = 0;i < 5;i++) {
                x [i] = random.nextUnitVector (3);
                x [i] [2]--;
            }

            inner:
            for (long combination : combinations) { // loop over the ten triples
                int vertex = 0;
                int point = 0;
                // sort the five points into the triple and the two other points
                for (int i = 0;i < 5;i++)
                    if ((combination & (1 << i)) != 0)
                        triple [vertex++] = x [i];
                    else
                        points [point++] = x [i];

                // invert the matrix formed by the triple
                double determinant =
                        triple [0] [0] * triple [1] [1] * triple [2] [2]
                      + triple [1] [0] * triple [2] [1] * triple [0] [2]
                      + triple [2] [0] * triple [0] [1] * triple [1] [2]
                      - triple [0] [0] * triple [2] [1] * triple [1] [2]
                      - triple [1] [0] * triple [0] [1] * triple [2] [2]
                      - triple [2] [0] * triple [1] [1] * triple [0] [2];

                for (int i0 = 0,i1 = 1,i2 = 2;i0 < 3;i0++,i1++,i1 %= 3,i2++,i2 %= 3)
                    for (int j0 = 0,j1 = 1,j2 = 2;j0 < 3;j0++,j1++,j1 %= 3,j2++,j2 %= 3)
                        inverse [j0] [i0] = (triple [i1] [j1] * triple [i2] [j2] - triple [i2] [j1] * triple [i1] [j2]) / determinant;

                // express the two points as linear combinations of the triple
                for (double [] p : points) {
                    for (int i = 0;i < 3;i++) {
                        double sum = 0;
                        for (int j = 0;j < 3;j++)
                            sum += inverse [j] [i] * p [j];
                        // a negative coordinate means this point lies outside the convex hull of the triple
                        if (sum < 0)
                            continue inner;
                    }
                }

                count++;
                continue outer;
            }
        }
     }
}
