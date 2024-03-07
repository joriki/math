package info.joriki.math.stackexchange;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import info.joriki.math.algebra.BigRational;
import info.joriki.math.numbers.Divisors;
import info.joriki.math.numbers.Primes;

public class Question4876625 {
    final static int n = 10;

    static int [] euss;
    static int lcm;
    static BitSet used = new BitSet (n);
    static List<List<Integer>> divisors = new ArrayList<>();

    public static void main (String [] args) {
        Primes.initialize (10000000);
        fractionRecursion (BigRational.ZERO,4,0);
    }

    static Stack<Integer> fractions = new Stack<>();
    static List<List<Integer>> fractionSets = new ArrayList<>();

    static boolean fractionRecursion (BigRational sum,int k,int index) {
        int diff = sum.compareTo (BigRational.ONE);

        if (diff == 0) {
            if (index == n) {
                System.out.println("    " + fractions.stream ().map (String::valueOf).collect (Collectors.joining (", ")));
                euss = fractions.stream ().mapToInt (i -> i).toArray ();
                used.clear ();
                divisors.clear ();
                rectangles.clear ();
                for (int fraction : euss)
                    divisors.add (Divisors.divisors (fraction).stream ().map (d -> (int) (long) d).filter (d -> d != 1 && d != fraction).toList ());
                lcm = Divisors.lcm (euss);
                rectangleRecursion (0);
            }
            return true;
        }

        if (diff > 0)
            return false;

        if (index == n)
            return true;

        index++;
        boolean first = true;
        for (;;k++)
            if (!Primes.isPrime (k)) {
                fractions.push (k);
                boolean tooSmall = fractionRecursion (BigRational.sum (sum,new BigRational (1,k)),k + 1,index);
                fractions.pop ();
                if (tooSmall)
                    return first;
                first = false;
            }
    }

    static Stack<Rectangle> rectangles = new Stack<>(); 

    static boolean rectangleRecursion (int index) {
        if (index == n) {
            final int dim = 1000;
            BufferedImage outputImage = new BufferedImage (dim,dim,BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = (Graphics2D) outputImage.getGraphics ();
            double scale = dim / (lcm + 1.);
            g.scale (scale,scale);
            g.setStroke (new BasicStroke ((float) (1 / scale)));
            g.fillRect (0,0,lcm + 1,lcm + 1);
            g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor (Color.black);
            for (Rectangle r : rectangles) {
                g.drawRect (r.x,r.y,r.width,r.height);
                int x = r.x + (r.width / 2);
                int y = r.y + (r.height / 2) - 3;
                int w = lcm / r.width;
                int h = lcm / r.height;
                g.setFont (new Font ("GentiumPlus",0,(int) Math.round (33 / scale)));
                FontMetrics fontMetrics = g.getFontMetrics ();
                for (String s : new String [] {"" + w * h,w + " · " + h}) {
                    g.drawString (s,x - fontMetrics.stringWidth (s) / 2,y);
                    y += 39 / scale;
                }
            }
            try {
                ImageIO.write (outputImage,"PNG",new File ("/Users/joriki/tmp/rectangles.png"));
            } catch (IOException e) {
                e.printStackTrace ();
            }
            System.out.println("solution found");
            return true;
        }
        else {
            Point p;
            if (rectangles.isEmpty ())
                p = new Point (0,0);
            else {
                p = new Point (lcm,lcm);
                for (Rectangle r : rectangles) {
                    int x = r.x + r.width;
                    int y = r.y + r.height;
                    if (x < lcm)
                        min (p,x,r.y);
                    if (y < lcm)
                        min (p,r.x,y);
                }
            }
            for (int i = 0;i < n;i++)
                if (!used.get (i)) {
                    used.set (i);
                    for (int divisor : divisors.get (i)) {
                        int w = lcm / divisor;
                        int h = lcm / (euss [i] / divisor);
                        if (p.x + w <= lcm && p.y + h <= lcm) {
                            Rectangle r = new Rectangle (p.x,p.y,w,h);
                            if (rectangles.stream ().noneMatch (s -> r.intersects (s))) {
                                rectangles.push (r);
                                if (rectangleRecursion (index + 1))
                                    return true;
                                rectangles.pop ();
                            }
                        }
                    }
                    used.clear (i);
                }
        }
        return false;
    }

    static void min (Point p,int x,int y) {
        if ((y < p.y || (y == p.y && x < p.x)) && rectangles.stream ().noneMatch (r -> r.contains (x,y))) {
            p.x = x;
            p.y = y;
        }
    }
}
