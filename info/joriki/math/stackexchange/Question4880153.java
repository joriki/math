package info.joriki.math.stackexchange;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import info.joriki.math.numerics.DifferentialEquationSpecification;
import info.joriki.math.numerics.RungeKutta4;

public class Question4880153 {
    final static int nsteps = 11;
    final static double amin = -2;
    final static double amax = 2;

    final static int n = 500;

    final static int width = n;
    final static int height = 500;

    public static void main (String [] args) {
        double [] [] [] values = new double [nsteps] [] [];

        for (int i = 0;i < nsteps;i++) {
            double a = amin + i * (amax - amin) / (nsteps - 1);
            values [i] = RungeKutta4.perform (1,0,n,new double [] {1,1},new DifferentialEquationSpecification() {
                public double [] f (double t,double [] y) {
                    double y0 = y [0];
                    double y1 = y [1];
                    double y2 = t == 1 ? a : (1 - y1) * (4 / t + (1 + y1) * y0 / (1 - y0 * y0)); 
                    return new double [] {y1,y2};
                }
            });
        }

        BufferedImage outputImage = new BufferedImage (width,height,BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D) outputImage.getGraphics ();
        g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRect (0,0,width,height);
        g.setColor (Color.black);

        for (double [] []  v : values)
            for (int i = 0;i <= n;i++) {
                double y = v [i] [0];
                if (y < 0)
                    break;
                g.fillRect (width - i,height - (int) (height * v [i] [0]),1,1);
            }

        try {
            ImageIO.write (outputImage,"PNG",new File ("/Users/joriki/tmp/runge-kutta.png"));
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}
