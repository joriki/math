package info.joriki.math.stackexchange;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import info.joriki.math.combinatorics.Combinations;

public class Question4867338 {
    final static int dim = 1225;
    final static int w = 2;

    final static int npoints = 7;
    final static int nsides = 3;

    final static Random random = new Random (243);

    static Graphics2D g;

    public static void main (String [] args) throws IOException {
      int count = 0;

      for (;;) {
          double [] [] polygon = generateRandomConvexPolygon (npoints);
          round (polygon);

          double [] [] cover = generateRandomConvexPolygon (nsides);
          for (int i = 0;i < nsides;i++)
              for (int j = 0;j < 2;j++)
                  cover [i] [j] *= 2;
          round (cover);

          double [] [] transformed = new double [nsides] [2];

          double [] [] a = new double [2] [2];
          double [] b = new double [2];
          double [] x = new double [2];

          count++;

          BitSet shattered = new BitSet ();

          class Shattering {
              BitSet shattered = new BitSet ();
              double [] [] cover;
              double [] [] match;
          }

          List<Shattering> shatterings = new ArrayList<> ();

          double [] [] match = new double [3] []; // the three points that the cover is matched onto
          for (long combination : Combinations.combinations (npoints,3)) {
              int m = 0;
              for (int i = 0;i < npoints;i++)
                  if ((combination & (1L << i)) != 0)
                      match [m++] = polygon [i];

              for (int mirror = 0;mirror < 2;mirror++) {
                  for (int two = 0;two < nsides;two++)     // side with two points
                      for (int one = 0;one < nsides;one++) // side with one point
                          if (one != two)
                              for (int s = 0;s < 3;s++) { // single point on side one
                                  int d1 = (s + 1) % 3;   // first point on side two
                                  int d2 = (s + 2) % 3;   // second point on side two

                                  // rotate the cover to align the side with two points to those points
                                  double alpha = angle (match [d1],match [d2]);
                                  double beta  = angle (cover [(two + 1) % nsides],cover [(two + 2) % nsides]);
                                  double delta = alpha - beta;

                                  for (int i = 0;i < nsides;i++)
                                      rotate (cover [i],transformed [i],delta);

                                  // set up and solve a system of linear equations for the required translation

                                  a [0] [0] = transformed [(two + 2) % nsides] [1] - transformed [(two + 1) % nsides] [1];
                                  a [0] [1] = transformed [(two + 1) % nsides] [0] - transformed [(two + 2) % nsides] [0];
                                  a [1] [0] = transformed [(one + 2) % nsides] [1] - transformed [(one + 1) % nsides] [1];
                                  a [1] [1] = transformed [(one + 1) % nsides] [0] - transformed [(one + 2) % nsides] [0];

                                  b [0] = cross (transformed [(two + 1) % nsides],transformed [(two + 2) % nsides],match [d1]);
                                  b [1] = cross (transformed [(one + 1) % nsides],transformed [(one + 2) % nsides],match [s]);

                                  double det = a [0] [0] * a [1] [1] - a [0] [1] * a [1] [0];

                                  x [0] = (a [1] [1] * b [0] - a [0] [1] * b [1]) / det;
                                  x [1] = (a [0] [0] * b [1] - a [1] [0] * b [0]) / det;

                                  for (int i = 0;i < nsides;i++)
                                      for (int j = 0;j < 2;j++)
                                          transformed [i] [j] += x [j];

                                  // are all three points on the sides (and not just on the lines containing the sides)?
                                  if (isOn (transformed [(two + 1) % nsides],transformed [(two + 2) % nsides],match [d1]) &&
                                      isOn (transformed [(two + 1) % nsides],transformed [(two + 2) % nsides],match [d2]) &&
                                      isOn (transformed [(one + 1) % nsides],transformed [(one + 2) % nsides],match [s])) {

                                      // generate the 8 subsets covered by this configuration

                                      int inside = 0;

                                      for (int i = 0;i < npoints;i++)
                                          if ((combination & (1L << i)) == 0)
                                              if (isIn (transformed,polygon [i]))
                                                  inside |= 1 << i;

                                      Shattering shattering = new Shattering ();
                                      for (int bits = 0;bits < 8;bits++) {
                                          inside &= ~combination;
                                          for (int i = 0,mask = 1;i < npoints;i++)
                                              if ((combination & (1L << i)) != 0) {
                                                  if ((bits & mask) != 0)
                                                      inside |= 1 << i;
                                                  mask <<= 1;
                                              }
                                          shattering.shattered.set (inside);
                                      }
                                      shattered.or (shattering.shattered);
                                      shattering.cover = copy (transformed);
                                      shattering.match = copy (match);
                                      shatterings.add (shattering);
                                  }
                              }
                  // flip the cover
                  for (int i = 0;i < nsides;i++)
                      cover [i] [1] *= -1;
              }
          }

          int nbits = shattered.cardinality ();
          if (nbits == 1 << npoints) {
              System.out.println ("count: " + count);
              System.out.println ("shatterings: " + shatterings.size ());

              int [] counts = new int [1 << npoints];
              int nshown = 0;

              BitSet selected = new BitSet ();
              for (int i = 0;i < 18;i++) {
                  selected.set (i);
                  BitSet s = shatterings.get (i).shattered;
                  for (int j = 0;j < s.length ();j++)
                      if (s.get (j))
                          if (counts [j]++ == 0)
                              nshown++;
              }

              // use simulated annealing to find a minimal subset of configurations that cover all subsets

              double beta = 0;
              
              for (;;) {
                  int add;
                  do
                      add = random.nextInt (shatterings.size ());
                  while (selected.get (add));

                  int sub;
                  do
                      sub = random.nextInt (shatterings.size ());
                  while (!selected.get (sub));

                  int delta = 0;

                  BitSet s = shatterings.get (add).shattered;
                  for (int j = 0;j < s.length ();j++)
                      if (s.get (j))
                          if (counts [j]++ == 0)
                              delta++;

                  s = shatterings.get (sub).shattered;
                  for (int j = 0;j < s.length ();j++)
                      if (s.get (j))
                          if (--counts [j] == 0)
                              delta--;

                  if (random.nextDouble () < Math.exp (beta * delta)) {
                      selected.set (add);
                      selected.clear (sub);

                      nshown += delta;

                      if (nshown == 1 << npoints)
                          break;
                  }
                  else {
                      s = shatterings.get (sub).shattered;
                      for (int j = 0;j < s.length ();j++)
                          if (s.get (j))
                              counts [j]++;

                      s = shatterings.get (add).shattered;
                      for (int j = 0;j < s.length ();j++)
                          if (s.get (j))
                              counts [j]--;
                  }

                  beta += 0.000001;
              }

              selected.stream ().forEach (index -> {
                  BufferedImage outputImage = new BufferedImage (dim,dim,BufferedImage.TYPE_3BYTE_BGR);
                  g = (Graphics2D) outputImage.getGraphics ();
                  g.fillRect (0,0,dim,dim);
                  g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                  g.setColor (Color.black);
                  drawPolygon (shatterings.get (index).cover);

                  g.setColor (Color.red);
                  for (double [] p : polygon)
                      drawPoint (p);

                  g.setColor (Color.green.darker ());
                  for (double [] p : shatterings.get (index).match)
                      drawPoint (p);

                  try {
                      ImageIO.write (outputImage,"PNG",new File ("/Users/joriki/tmp/points" + index + ".png"));
                  } catch (IOException e) {
                      e.printStackTrace ();
                  }
              });

              System.out.println("points: " + Arrays.stream (polygon).map (p -> "$(" + p [0] + "," + (1 - p [1]) + ")$").collect(Collectors.joining(", ")));
              System.out.println("cover : " + Arrays.stream (cover).map (p -> "$(" + p [0] + "," + (1 - p [1]) + ")$").collect(Collectors.joining(", ")));

              return;
          }
      }
    }

    static double square (double [] x1,double [] x2) {
        double sum = 0;
        for (int i = 0;i < 2;i++) {
            double d = x2 [i] - x1 [i];
            sum += d * d;
        }
        return sum;
    }

    static double [] [] copy (double [] [] x) {
        return Arrays.stream (x).map (t -> t.clone ()).toArray (double [] []::new);
    }

    final static double round = 100;

    static void round (double [] [] x) {
        for (double [] v : x)
            for (int i = 0;i < v.length;i++)
                v [i] = Math.round (round * v [i]) / round;
    }

    static boolean isIn (double [] [] cover,double [] p) {
        double sign = 0;
        for (int i = 0;i < nsides;i++) {
            double thisSign = sign (cover [i],cover [(i + 1) % nsides],p);
            if (i == 0)
                sign = thisSign;
            else if (thisSign != sign)
                return false;
        }
        return true;
    }

    static boolean isOn (double [] x1,double [] x2,double [] p) {
        return (p [0] - x1 [0]) * (p [0] - x2 [0]) + (p [1] - x1 [1]) * (p [1] - x2 [1]) < 0;
    }

    static double cross (double [] x1,double [] x2,double [] p) {
        return (x1 [0] - x2 [0]) * (p [1] - x2 [1]) - (x1 [1] - x2 [1]) * (p [0] - x2 [0]);
    }

    static double angle (double [] x1,double [] x2) {
        return Math.atan2 (x2 [1] - x1 [1],x2 [0] - x1 [0]);
    }

    static void rotate (double [] from,double [] to,double angle) {
        double cos = Math.cos (angle);
        double sin = Math.sin (angle);
        
        to [0] = cos * from [0] - sin * from [1];
        to [1] = cos * from [1] + sin * from [0];
    }

    static void drawPolygon (double [] [] p) {
        for (int i = 0;i < p.length;i++)
            drawLine (p [i],p [(i + 1) % p.length]);
    }

    static void drawLine (double [] x1,double [] x2) {
        g.drawLine (xCoordinate (x1 [0]),yCoordinate (x1 [1]),xCoordinate (x2 [0]),yCoordinate (x2 [1]));
    }

    static void drawPoint (double [] x1) {
        g.fillOval (xCoordinate (x1 [0]) - w,yCoordinate (x1 [1]) - w,2 * w + 1,2 * w + 1);
    }

    final static double scale = 0.34;
    final static double xoff = -0.039;
    final static double yoff = 0;

    static int xCoordinate (double x) {
        return (int) (dim * (scale * x + (1 - scale) / 2 + xoff));
    }

    static int yCoordinate (double x) {
        return (int) (dim * (scale * x + (1 - scale) / 2 + yoff));
    }

    static double [] [] generateRandomConvexPolygon (int n) throws IOException {
        List<double []> x = new ArrayList<> ();

        outer:
        for (int i = 0;i < n;i++)
            for (;;) {
                double [] p = randomPoint ();
                
                if (i < 3) {
                    x.add (p);
                    break;
                }

                for (int j = 0;j < i;j++) {
                    double sign  = sign (x.get ((j + 1) % i),x.get ((j + 2) % i),x.get ((j + 3) % i));
                    double sign0 = sign (x.get ((j + 0) % i),x.get ((j + 1) % i),p);
                    double sign1 = sign (x.get ((j + 1) % i),x.get ((j + 2) % i),p);
                    double sign2 = sign (x.get ((j + 2) % i),x.get ((j + 3) % i),p);
                    if (sign0 == sign && sign1 != sign && sign2 == sign) {
                        x.add ((j + 2) % i,p);
                        continue outer;
                    }
                }
            }
        return x.toArray (double [] []::new);
    }

    static double sign (double [] x1,double [] x2,double [] p) {
        return Math.signum (cross (x1,x2,p));
    }

    static double [] randomPoint () {
        double [] p = new double [2];
        for (int i = 0;i < p.length;i++)
            p [i] = random.nextDouble ();
        return p;
    }
}
