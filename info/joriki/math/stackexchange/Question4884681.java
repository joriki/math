package info.joriki.math.stackexchange;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import info.joriki.math.algebra.BigRational;

public class Question4884681 {
    final static int nevents = 54;

    final static int [] mins = new int [nevents]; // lower bound (inclusive) of the outcome
    final static int [] lims = new int [nevents]; // upper bound (exclusive) of the outcome

    final static BigInteger [] noutcomes = new BigInteger [nevents]; // number of possible outcomes
    final static BigInteger [] toBig = new BigInteger [8];

    static {
        for (int i = 0;i < nevents;i++) { // events are indexed starting at 0
            mins [i] = i < 22 ? 0 : 1; // outcome must be at least 0 for i < 22, at least 1 otherwise
            lims [i] = i < 32 ? 6 : 8; // outcome must be less than 6 for i < 32, less than 8 otherwise
            noutcomes [i] = BigInteger.valueOf (lims [i] - mins [i]);
        }
    }

    static class ConstraintSet {
        Set<Long> constraints = new HashSet<>();

        public ConstraintSet (ConstraintSet ... sets) {
            for (ConstraintSet set : sets)
                add (set);
        }

        public ConstraintSet (Collection<ConstraintSet> sets) {
            for (ConstraintSet set : sets)
                add (set);
        }

        public void add (ConstraintSet set) {
            for (long constraint : set.constraints)
                    add (constraint);
        }

        void add (long constraint) {
            outer:
            for (;;) {
                Iterator<Long> iterator = constraints.iterator ();
                while (iterator.hasNext ()) {
                    long c = iterator.next ();
                    if ((c & constraint) != 0) {
                        // if the new constraint overlaps with an existing one, remove the existing one and add their OR
                        iterator.remove ();
                        constraint |= c;
                        continue outer;
                    }
                }
                constraints.add (constraint);
                return;
            }
        }

        BigInteger count () {
            long constrained = 0;
            for (long constraint : constraints)
                constrained |= constraint;
            BigInteger count = BigInteger.ONE;
            // first count the unconstrained outcomes
            for (int i = 0;i < nevents;i++)
                if ((constrained & (1L << i)) == 0)
                    count = count.multiply (noutcomes [i]);
            // for each equality constraint, find the intersection of the outcome ranges involved
            for (long constraint : constraints) {
                int min = 0;
                int lim = Integer.MAX_VALUE;
                for (int i = 0;i < nevents;i++)
                    if ((constraint & (1L << i)) != 0) {
                        min = Math.max (min,mins [i]);
                        lim = Math.min (lim,lims [i]);
                    }
                count = count.multiply (toBig [lim - min]);
            }
            return count;
        }
    }

    public static void main (String [] args) {
        for (int i = 0;i < toBig.length;i++)
            toBig [i] = BigInteger.valueOf (i);

        List<ConstraintSet> conditions = new ArrayList<> ();

        // generate conditions one for each possible length and starting position
        for (int period = 2;3 * period <= nevents;period++)
            for (int start = 0;start + 3 * period <= nevents;start++) {
                ConstraintSet condition = new ConstraintSet ();
                long bits = (1 + (1L << period) + (1L << (period << 1))) << start;
                for (int i = 0;i < period;i++,bits <<= 1)
                    condition.add (bits);
                conditions.add (condition);
            }

        BigInteger total = recurse (conditions,0);

        // perform inclusion–exclusion
        BigInteger sum = BigInteger.ZERO;
        for (int i = 1;;i++) {
            BigInteger count = recurse (conditions,i);
            boolean negative = (i & 1) == 0;
            if (negative)
                count = count.negate ();
            sum = sum.add (count);
            BigRational probability = new BigRational (sum,total);
            System.out.println ((negative ? "≥ " : "≤ ") + sum + " / " + total + " = " + probability);
            if (count.signum () == 0)
                break;
        }
    }

    static BigInteger recurse (List<ConstraintSet> conditions,int depth) {
        return recurse (conditions,new Stack<> (),0,depth);
    }

    static BigInteger recurse (List<ConstraintSet> conditions,Stack<ConstraintSet> selection,int min,int depth) {
        if (depth-- == 0)
            return new ConstraintSet (selection).count ();
        BigInteger sum = BigInteger.ZERO;
        for (int i = min;i + depth < conditions.size ();i++) {
            if (depth >= 3)
                System.out.println (i);
            selection.push (conditions.get (i));
            sum = sum.add (recurse (conditions,selection,i + 1,depth));
            selection.pop ();
        }
        return sum;
    }
}
