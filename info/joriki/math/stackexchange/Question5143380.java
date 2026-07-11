package info.joriki.math.stackexchange;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import info.joriki.math.algebra.BigRational;

public class Question5143380 {
    final static boolean performChecks = false;

    final static BigRational half = new BigRational (1,2);

    final static int nrows = 1000000;

    static int next = 0;

    static class Glass {
        int n;
        int k;

        BigRational left = BigRational.ONE;
        BigRational rate;

        BigRational lastUpdateTime = BigRational.ZERO;

        Glass () {}

        Glass (int n,int k,BigRational rate,BigRational time) {
            if (n > next)
                throw new Error ();
            if (n == next) {
                System.out.println ("row " + n + " reached at " + time);
                next++;
            }
            this.n = n;
            this.k = k;
            this.rate = rate;
            this.lastUpdateTime = time;
        }

        BigRational fillTime () {
            return BigRational.sum (lastUpdateTime,BigRational.product (left,rate.reciprocal ()));
        }

        void update (BigRational time) {
            left = left(time);
            lastUpdateTime = time;
        }

        BigRational left (BigRational time) {
            return BigRational.sum (left,BigRational.product (rate,BigRational.sum (time.negate (),lastUpdateTime)));
        }

        long index () {
            return Question5143380.index (n,k);
        }

        @Override
        public String toString () {
            return "Glass [n=" + n + ", k=" + k + ", left=" + left + ", rate=" + rate + ", lastUpdateTime="
                    + lastUpdateTime + "]";
        }
    }

    static enum Type {
        FILLING, FULL
    }

    static Glass full = new Glass ();

    static Map<Long,Type> types = new HashMap<> ();

    static Map<Long,Glass> glasses = new HashMap<> ();

    static long index (int n,int k) {
        return ((long) n << 32) | k;
    }

    static Glass getGlass (int n, int k) {
        Type type = types.get (index (n,k));
        return type == null ? null : type == Type.FULL ? full : glasses.get (index (n,k));
    }

    public static void main (String [] args) {
        PriorityQueue<Glass> queue = new PriorityQueue<> ((g1,g2) -> g1.fillTime ().compareTo (g2.fillTime ()));

        BigRational time = BigRational.ZERO;

        Glass top = new Glass (0,0,BigRational.ONE,time);
        glasses.put (top.index (),top);
        top.lastUpdateTime = time;

        queue.add (top);

        BigRational [] [] updates = new BigRational [2] [nrows];
        int toggle = 0;

        for (;;) {
            Glass glass = queue.poll ();
            time = glass.fillTime ();
            glass.left = BigRational.ZERO;
            types.put (glass.index (),Type.FULL);

            int n = glass.n;
            int lo = glass.k;
            int hi = glass.k;

            updates [toggle] [glass.k] = glass.rate;

            do {
                n++;
                hi++;
                for (int i = lo;i <= hi;i++)
                    updates [1 - toggle] [i] = BigRational.ZERO;
                for (int i = lo;i < hi;i++)
                    for (int j = 0;j < 2;j++)
                        updates [1 - toggle] [i + j] = BigRational.sum (updates [1 - toggle] [i + j],BigRational.product (half,updates [toggle] [i]));
                while (lo <= hi) {
                    Glass g = getGlass (n,lo);
                    if (g == full)
                        break;
                    if (g == null) {
                        g = new Glass (n,lo,BigRational.ZERO,time);
                        glasses.put (index (n,lo),g);
                        types.put (g.index (),Type.FILLING);
                    }
                    else {
                        queue.remove (g);
                        g.update (time);
                    }
                    g.rate = BigRational.sum (g.rate,updates [1 - toggle] [lo]);
                    queue.add (g);
                    lo++;
                }
                while (lo <= hi) {
                    Glass g = getGlass (n,hi);
                    if (g == full)
                        break;
                    if (g == null) {
                        g = new Glass (n,hi,BigRational.ZERO,time);
                        glasses.put (index (n,hi),g);
                        types.put (g.index (),Type.FILLING);
                    }
                    else {
                        queue.remove (g);
                        g.update (time);
                    }
                    g.rate = BigRational.sum (g.rate,updates [1 - toggle] [hi]);
                    queue.add (g);
                    hi--;
                }
                toggle = 1 - toggle;
            } while (lo <= hi);


            if (performChecks) {
                BigRational rateCheck = BigRational.ZERO;
                for (Glass g : queue)
                    rateCheck = BigRational.sum (rateCheck,g.rate);

                if (rateCheck.compareTo (BigRational.ONE) != 0)
                    throw new RuntimeException ("rate check failed");

                BigRational volumeCheck = BigRational.ZERO;
                for (Glass g : queue)
                    volumeCheck = BigRational.sum (volumeCheck,BigRational.sum (BigRational.ONE,g.left (time).negate ()));
                for (Map.Entry<Long,Type> entry : types.entrySet ())
                    if (entry.getValue () == Type.FULL)
                        volumeCheck = BigRational.sum (volumeCheck,BigRational.ONE);

                if (volumeCheck.compareTo (time) != 0)
                    throw new RuntimeException ("volume check failed");
            }
        }
    }
}
