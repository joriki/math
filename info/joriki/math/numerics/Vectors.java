package info.joriki.math.numerics;

public class Vectors {
    public static double dot (double [] v1,double [] v2) {
        if (v1.length != v2.length)
            throw new IllegalArgumentException("dot product of vectors of different lengths");
        double dot = 0;
        for (int i = 0;i < v1.length;i++)
            dot += v1 [i] * v2 [i];
        return dot;
    }
}
