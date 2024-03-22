package info.joriki.math.stackexchange;

import java.io.IOException;

import info.joriki.math.numerics.quadrature.LebedevQuadrature;
import info.joriki.math.random.AugmentedRandomGenerator;

public class Question4880954Integration {
    final static AugmentedRandomGenerator random = new AugmentedRandomGenerator ();

    public static void main (String [] args) throws IOException {
        LebedevQuadrature q = new LebedevQuadrature (131);
        int n = q.weights.length;

        for (;;) {
            double [] [] thetas = new double [3] [n];
            double [] [] phis = new double [3] [n];

            double [] [] [] rotations = new double [3] [] [];

            for (int j = 0;j < 3;j++)
                rotations [j] = random.nextRotationMatrix ();

            // rotate the three Lebedev grids into random orientations
            for (int i = 0;i < n;i++) {
                double theta = q.theta [i];
                double phi = q.phi [i];
                double sin = Math.sin (theta);
                double [] t = {
                        sin * Math.cos (phi),
                        sin * Math.sin (phi),
                        Math.cos (theta)
                };
                for (int l = 0;l < 3;l++) {
                    double [] y = new double [3];
                    for (int j = 0;j < 3;j++)
                        for (int k = 0;k < 3;k++)
                            y [j] += rotations [l] [j] [k] * t [k];
                    thetas [l] [i] = Math.acos (y [2]);
                    phis [l] [i] = Math.atan2 (y [1],y [0]);
                }
            }

            // precompute the area contributions for triangles with one vertex at the origin
            double [] [] [] p = new double [3] [n] [n];

            for (int l = 0;l < 3;l++)
                for (int i = 0;i < n;i++) {
                    for (int j = 0;j < n;j++) {
                        double t1 = thetas [(l + 0) % 3] [i];
                        double t2 = thetas [(l + 1) % 3] [j];
                        double p1 = phis [(l + 0) % 3] [i];
                        double p2 = phis [(l + 1) % 3] [j];
                        if (t1 != 0 && t2 != 0 && (t1 != t2 || p1 != p2)) {
                            double r1 = r (t1);
                            double r2 = r (t2);
                            double x1 = r1 * Math.cos (p1);
                            double y1 = r1 * Math.sin (p1);
                            double x2 = r2 * Math.cos (p2);
                            double y2 = r2 * Math.sin (p2);
                            double dx = x2 - x1;
                            double dy = y2 - y1;
                            double s = (dx * x1 + dy * y1) / (dx * dx + dy * dy);
                            double x0 = x1 - s * dx;
                            double y0 = y1 - s * dy;
                            double r0 = Math.sqrt (x0 * x0 + y0 * y0);
                            double p0 = Math.atan2 (y0,x0);
                            p [l] [i] [j] = (antiderivative (r0,p2 - p0) - antiderivative (r0,p1 - p0)) / (2 * Math.PI);
                        }
                    }
                }

            // now sum them over all triangles with the quadrature weights
            double [] isum = new double [2];
            for (int i = 0;i < n;i++) {
                double [] jsum = new double [2];
                for (int j = 0;j < n;j++) {
                    double [] ksum = new double [2];
                    for (int k = 0;k < n;k++) {
                        // each triangle consists of three triangles with one vertex at the origin
                        // if the triangle doesn’t contain the origin, the contributions have different signs
                        double v = p [0] [i] [j] + p [1] [j] [k] + p [2] [k] [i];
                        ksum [0] += q.weights [k] * v * v;
                        ksum [1] += q.weights [k] * Math.abs (v);
                    }
                    for (int l = 0;l < 2;l++)
                        jsum [l] += q.weights [j] * ksum [l];
                }
                for (int l = 0;l < 2;l++)
                    isum [l] += q.weights [i] * jsum [l];
            }
            System.out.println(isum [1] + " " + isum [0]);
        }
    }

    // stereographic projection from the north pole
    static double r (double theta) {
        return 1 / Math.tan (theta / 2);
    }

    // contribution of the triangle that extends to angle phi from the closest approach to the origin at r0
    static double antiderivative (double r0,double phi) {
        double r = Math.sqrt (1 / (1 + 1 / (r0 * r0)));
        return r * Math.atan (r * Math.tan (phi));
    }
}
