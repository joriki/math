package info.joriki.math.stackexchange;

import java.util.stream.Stream;

import info.joriki.math.algebra.BigRational;
import info.joriki.math.algebra.Matrix;
import info.joriki.math.algebra.RationalField;

public class Question4882983 {
    final static String transitions
            = "2&1&1&1&1&0&0&0&0&0&0&0&0&0&0\n"
            + "1&1&0&0&0&1&1&1&1&0&0&0&0&0&0\n"
            + "1&1&1&0&0&0&0&0&0&1&1&1&0&0&0\n"
            + "1&1&1&1&0&0&0&0&0&0&0&0&1&1&0\n"
            + "1&1&1&1&1&0&0&0&0&0&0&0&0&0&1\n"
            + "0&1&1&0&0&0&0&0&0&0&0&0&0&0&0\n"
            + "0&1&1&1&0&0&0&0&0&0&0&0&0&0&0\n"
            + "0&1&1&1&1&0&0&0&0&0&0&0&0&0&0\n"
            + "1&1&1&1&1&0&0&0&0&0&0&0&0&0&0\n"
            + "0&1&1&1&0&0&0&0&0&0&0&0&0&0&0\n"
            + "0&1&1&1&1&0&0&0&0&0&0&0&0&0&0\n"
            + "1&1&1&1&1&0&0&0&0&0&0&0&0&0&0\n"
            + "0&1&1&1&1&0&0&0&0&0&0&0&0&0&0\n"
            + "1&1&1&1&1&0&0&0&0&0&0&0&0&0&0\n"
            + "1&1&1&1&1&0&0&0&0&0&0&0&0&0&0";

    final static String absorptions
            = "0&0&0&0\n"
            + "0&0&0&0\n"
            + "0&0&0&0\n"
            + "0&0&0&0\n"
            + "0&0&0&0\n"
            + "4&0&0&0\n"
            + "3&0&0&0\n"
            + "2&0&0&0\n"
            + "1&0&0&0\n"
            + "0&3&0&0\n"
            + "0&2&0&0\n"
            + "0&1&0&0\n"
            + "0&0&2&0\n"
            + "0&0&1&0\n"
            + "0&0&0&1";

    final static String initial = "1&0&0&0&0&0&0&0&0&0&0&0&0&0&0";

    static int [] [] parse (String s) {
        return Stream.of (s.split ("\n")).map (row -> Stream.of (row.split ("&")).mapToInt (Integer::valueOf).toArray ()).toArray (int [] []::new);
    }

    static Matrix<BigRational> parseMatrix (String s,int denominator) {
        int [] [] a = parse (s);
        Matrix<BigRational> m = new Matrix<> (RationalField.Q,a.length,a [0].length);
        for (int i = 0;i < a.length;i++)
            for (int j = 0;j < a [i].length;j++)
                m.set (i,j,new BigRational (a [i] [j],denominator));
        return m;
    }

    public static void main (String [] args) {
        Matrix<BigRational> t = parseMatrix (transitions,6);
        Matrix<BigRational> identity = Matrix.getIdentity (RationalField.Q,t.m);
        identity.add (BigRational.ONE.negate (),t);
        Matrix<BigRational> a = parseMatrix (absorptions,6);
        identity.solveFor (a);
        System.out.println (Matrix.product (parseMatrix (initial,1),a));
    }
}
