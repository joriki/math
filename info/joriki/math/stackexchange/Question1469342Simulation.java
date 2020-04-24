package info.joriki.math.stackexchange;

import java.awt.Point;
import java.util.Random;

public class Question1469342Simulation {
    final static int dim = 8;
    final static long ntrials = 10000000;
    final static Random random = new Random();
    
    public static void main(String [] args) {
        long total = 0;
        
        for (long trial = 0;trial < ntrials;trial++) {
            Point p1 = new Point(0,0);
            Point p2 = new Point(dim - 1,dim - 1);
            
            do {
                p1 = randomNeighbour (p1);
                p2 = randomNeighbour (p2);
                total++;
            } while (!p1.equals(p2));
        }
        
        System.out.println(total / (double) ntrials);
    }
    
    static Point randomNeighbour(Point p) {
        for (;;) {
            Point q = new Point(p);
            int d = random.nextBoolean() ? 1 : -1;
            if (random.nextBoolean())
                q.x += d;
            else
                q.y += d;
            if (0 <= q.x && q.x < dim && 0 <= q.y && q.y < dim)
                return q;
        }
    }
}
