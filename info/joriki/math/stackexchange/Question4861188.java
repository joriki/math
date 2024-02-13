package info.joriki.math.stackexchange;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import info.joriki.math.algebra.BigRational;

/*
 * https://math.stackexchange.com/questions/4861188
 * 
 * Constructs Hamiltonian cycles on rectangular lattices such that no two edges are parallel.
 * Builds a partial cycle consisting of linear segments, trying all potential edges for all
 * directions, starting with the directions with the fewest remaining options, backtracking
 * when some direction can no longer be realized.
 */

public class Question4861188 {
    // graphics parameters
    final static int d = 36;
    final static int margin = 2;
    
    static int m,n;                    // lattice dimensions
    static long nsolutions;            // solution count
    static int ndirections;            // direction count
    static boolean [] directionsUsed;  // [direction] has the direction been used for an edge? 
    static int directions [] [];       // [vertex] [vertex] directions of all potential edges 
    static int [] [] [] vertices;      // [direction] [index] [2] : vertices incident at edges
    static int [] directionCounts;     // [direction] : count of active edges in that direction
    static int [] [] neighbors;        // [vertex] [2] : up to two neighbors in partial cycle
    static int [] neighborCounts;      // [vertex] : count of neighbors in partial cycle
    static int [] ends;                // [vertex] : other end of segment that starts at vertex
    
    public static void main(String [] args) throws IOException {
        for (int size = 4;;size++) // m * n
            for (m = 3;m * m <= size;m++) {
                n = size / m;
                if (m * n == size) {
                    ndirections = 0;
                    Map<BigRational,Integer> directionMap = new HashMap<>(); // maps slopes to direction indices
                    for (int dy = -(m - 1);dy < m;dy++)
                        for (int dx = 1;dx < n;dx++)
                            directionMap.computeIfAbsent(new BigRational (dy,dx),r -> ndirections++);
                    ndirections++; // add one more index for vertical edges (infinite slope)

                    directions = new int [m * n] [m * n];
                    directionCounts = new int [ndirections];

                    // precompute directions for all potential edges
                    for (int y = 0,i = 0;y < m;y++)
                        for (int x = 0;x < n;x++,i++)
                            for (int v = 0,j = 0;v < m;v++)
                                for (int u = 0;u < n;u++,j++) {
                                    int direction = u == x ? ndirections - 1 : directionMap.get (new BigRational(v - y,u - x));
                                    directions [i] [j] = direction;
                                    if (i < j)
                                        directionCounts [direction]++;
                                }

                    vertices = new int [ndirections] [] [];

                    for (int d = 0;d < ndirections;d++) {
                        vertices [d] = new int [directionCounts [d]] [2];
                        directionCounts [d] = 0;
                    }

                    // precompute vertices for all potential edges per direction
                    for (int j = 1;j < m * n;j++)
                        for (int i = 0;i < j;i++) {
                            int d = directions [i] [j];
                            int [] edge = vertices [d] [directionCounts [d]++];
                            edge [0] = i;
                            edge [1] = j;
                        }

                    int directionSurplus = ndirections - m * n;

                    System.out.println("The " + m + " x " + n + " lattice has " + ndirections + " directions for " + m * n + " vertices (" + (directionSurplus > 0 ? "more than" : directionSurplus < 0 ? "not" : "just") + " enough)");
                    
                    if (directionSurplus >= 0) {
                        directionsUsed = new boolean [ndirections];
                        neighbors = new int [m * n] [2];
                        neighborCounts = new int [m * n];
                        ends = new int [m * n];
                        for (int i = 0;i < m * n;i++)
                            ends [i] = i; // initially each vertex is a segment of its own
                        nsolutions = 0;
                        recurse (m * n,directionSurplus,0,1);
                        System.out.println(m + " x " + n + " : " + nsolutions + " solutions");
                    }
                }
            }
    }

    /**
     * @left  : number of edges yet to be added
     * @extra : excess of active directions over @left
     * @where : numerator of the progress estimate
     * @total : denominator of the progress estimate  
     */
    
    static void recurse (int left,int extra,long where,long total) throws IOException {
        if (left == 0) {
            if ((nsolutions++ & 0xffff) == 0) { // every once in a while
                // print progress estimate and solution count
                System.out.print((where / (double) total) + " / " + nsolutions + " : ");

                // print and illustrate solution
                int w = (n - 1) * d + 2 * margin;
                int h = (m - 1) * d + 2 * margin;
                BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = (Graphics2D) image.getGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(Color.white);
                graphics.fillRect(0,0,w,h);
                graphics.setColor(Color.lightGray);
                for (int y = 0;y < m;y++)
                    graphics.drawLine(margin,margin + y * d,margin + (n - 1) * d,margin + y * d);
                for (int x = 0;x < n;x++)
                    graphics.drawLine(margin + x * d,margin,margin + x * d,margin + (m - 1) * d);
                graphics.setColor(Color.red);

                int v0 = 0;
                int v1 = -1;
                int v2 = -1;
                for (int i = 0;i < m * n;i++) {
                    System.out.print (" " + v0);
                    v2 = v1;
                    v1 = v0;
                    v0 = neighbors [v0] [neighbors [v0] [0] != v2 ? 0 : 1];
                    graphics.drawLine(
                            margin + (v1 % n) * d,
                            margin + (v1 / n) * d,
                            margin + (v0 % n) * d,
                            margin + (v0 / n) * d);
                }
                System.out.println();
                ImageIO.write(image,"PNG",new File ("/Users/joriki/tmp/hamiltonian_cycle_" + m + "x" + n + "_" + (nsolutions >> 0x10) + ".png"));
            }
        }
        else {
            left--;

            // find one of the directions with the fewest options left
            int min = Integer.MAX_VALUE;
            int direction = 0;
            
            for (int d = 0;d < ndirections;d++)
                if (!directionsUsed [d] && directionCounts [d] < min) {
                    min = directionCounts [d];
                    direction = d;
                }

            // count edge options in advance (only needed for progress estimate)
            int count = 0;
            for (int [] edge : vertices [direction])
                if (neighborCounts [edge [0]] < 2 && neighborCounts [edge [1]] < 2 && !(left != 0 && ends [edge [0]] == edge [1]))
                    count++;

            // if an excess of directions remains, add one more option for skipping this direction
            if (extra > 0)
                count++;
            
            where *= count;
            total *= count;

            for (int [] edge : vertices [direction])
                // check whether the edge is still available and wouldn't create a cycle prematurely
                if (neighborCounts [edge [0]] < 2 && neighborCounts [edge [1]] < 2 && !(left != 0 && ends [edge [0]] == edge [1])) {
                    directionCounts [directions [edge [0]] [edge [1]]]--;

                    for (int i = 0;i < 2;i++) {
                        int v = edge [i];
                        int w = edge [1 - i];
                        neighbors [v] [neighborCounts [v]] = w;
                        // if the vertex now has two neighbors, deactivate its other edges
                        if (++neighborCounts [v] == 2) {
                            int [] d = directions [v];
                            for (int j = 0;j < m * n;j++)
                                // deactivate the edge unless it was already deactivated
                                if (j != v && j != w && neighborCounts [j] != 2)
                                    directionCounts [d [j]]--;
                        }
                    }

                    // connect the ends
                    int e0 = ends [edge [0]];
                    int e1 = ends [edge [1]];
                    int ee0 = ends [e0];
                    int ee1 = ends [e1];

                    ends [e0] = e1;
                    ends [e1] = e0;

                    directionsUsed [direction] = true;

                    recurse (left,extra,where++,total); // recurse, then undo everything

                    directionsUsed [direction] = false;

                    ends [e1] = ee1;
                    ends [e0] = ee0;

                    for (int i = 1;i >= 0;i--) {
                        int v = edge [i];
                        int w = edge [1 - i];
                        int [] d = directions [v];
                        if (neighborCounts [v]-- == 2)
                            for (int j = m * n - 1;j >= 0;j--)
                                if (j != v && j != w && neighborCounts [j] != 2)
                                    directionCounts [d [j]]++;
                        neighbors [v] [neighborCounts [v]] = -1;
                    }

                    directionCounts [directions [edge [0]] [edge [1]]]++;
                }
            // if an excess of directions remains, try skipping this one
            if (extra > 0) {
                directionsUsed [direction] = true;
                recurse (left + 1,extra - 1,where,total);
                directionsUsed [direction] = false;
            }
        }
    }
}
