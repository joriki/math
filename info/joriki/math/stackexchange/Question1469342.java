package info.joriki.math.stackexchange;

import java.util.ArrayList;
import java.util.List;

import info.joriki.math.algebra.BigRational;
import info.joriki.math.algebra.Matrix;
import info.joriki.math.algebra.RationalField;

public class Question1469342 {
    final static int d2 = 4;
    final static int dim = d2 << 1;
    final static int n = dim * dim * (d2 * (d2 + 1)) >> 2;
    
    public static void main(String [] args) {
        List<List<Integer>> neighbours = new ArrayList<>();
        
        for (int y = 0;y < dim;y++)
            for (int x = 0;x < dim;x++) {
                List<Integer> n = new ArrayList<>();
                for (int dx = -1;dx <= 1;dx++)
                    for (int dy = -1;dy <= 1;dy++)
                        if (((dx ^ dy) & 1) == 1) {
                            int xx = x + dx;
                            int yy = y + dy;
                            if (0 <= xx && xx < dim && 0 <= yy && yy < dim)
                                n.add(yy * dim + xx);
                        }
                neighbours.add(n);
            }
        
        Matrix<BigRational> m = new Matrix<>(RationalField.Q,n,n);
        Matrix<BigRational> b = new Matrix<>(RationalField.Q,n,1);

        m.zero();
        b.zero();

        for (int x1 = 0;x1 < d2;x1++)
            for (int y1 = 0;y1 <= x1;y1++) {
                int s1 = y1 * dim + x1;
                List<Integer> l1 = neighbours.get(s1);
                for (int x2 = 0;x2 < dim;x2++)
                    for (int y2 = 0;y2 < dim;y2++)
                        if (((x1 ^ y1 ^ x2 ^ y2) & 1) == 0) {
                            int s2 = y2 * dim + x2;
                            List<Integer> l2 = neighbours.get(s2);
                            int i = (s2 >> 1) + dim * d2 * (((x1 * (x1 + 1)) >> 1) + y1);
                            m.set(i, i, BigRational.ONE);
                            if (s1 != s2) {
                                b.set(i,0,BigRational.ONE);
                                BigRational c = new BigRational(-1,l1.size() * l2.size());
                                for (int n1 : l1) {
                                    for (int n2 : l2) {
                                        int u1 = n1 % dim;
                                        int v1 = n1 / dim;
                                        int u2 = n2 % dim;
                                        int v2 = n2 / dim;
                                        if (u1 == d2) {
                                            u1 = d2 - 1;
                                            u2 = dim - 1 - u2;
                                        }
                                        if (v1 == d2) {
                                            v1 = d2 - 1;
                                            v2 = dim - 1 - v2;
                                        }
                                        if (v1 > u1) {
                                            int t = u1;
                                            u1 = v1;
                                            v1 = t;
                                            t = u2;
                                            u2 = v2;
                                            v2 = t;
                                        }
                                        int index = ((v2 * dim + u2) >> 1) + dim * d2 * (((u1 * (u1 + 1)) >> 1) + v1);
                                        m.set(i, index, BigRational.sum(m.get(i, index),c));
                                    }
                                }
                            }
                        }
            }
        m.solveFor(b);
        System.out.println(b.get((dim * dim - 1) >> 1,0));
    }
}
