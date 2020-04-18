package info.joriki.math.algebra;

public class AlgebraicIntegerRing implements Ring<AlgebraicInteger> {
    public static AlgebraicIntegerRing GAUSSIAN_INTEGERS = new AlgebraicIntegerRing(new int [] {1,0});
    public static AlgebraicIntegerRing EISENSTEIN_INTEGERS = new AlgebraicIntegerRing(new int [] {1,1});
    
    private int [] minimalPolynomial;
    private AlgebraicInteger zero;
    private AlgebraicInteger one;
    
    // The minimal polynomial is monic; the coefficient 1 of the highest power is implicit
    public AlgebraicIntegerRing(int [] minimalPolynomial) {
        this.minimalPolynomial = minimalPolynomial;
        zero = new AlgebraicInteger (new int [minimalPolynomial.length]);
        one = new AlgebraicInteger (new int [minimalPolynomial.length]);
        one.x [0] = 1;
    }

    public InvertibleBinaryOperation<AlgebraicInteger> addition() {
        return new InvertibleBinaryOperation<AlgebraicInteger>() {
            public AlgebraicInteger op(AlgebraicInteger t1,AlgebraicInteger t2) {
                if (t1.x.length != minimalPolynomial.length)
                    throw new IllegalArgumentException();
                if (t2.x.length != minimalPolynomial.length)
                    throw new IllegalArgumentException();
                int [] sum = new int [minimalPolynomial.length];
                for (int i = 0;i < sum.length;i++)
                    sum [i] = t1.x [i] + t2.x [i];
                return new AlgebraicInteger(sum);
            }
            
            public AlgebraicInteger identity() {
                return zero;
            }
            
            public AlgebraicInteger inverse(AlgebraicInteger t) {
                if (t.x.length != minimalPolynomial.length)
                    throw new IllegalArgumentException();
                int [] inverse = new int [minimalPolynomial.length];
                for (int i = 0;i < inverse.length;i++)
                    inverse [i] = -t.x [i];
                return new AlgebraicInteger(inverse);
            }
        };
    }

    public BinaryOperationWithIdentity<AlgebraicInteger> multiplication() {
        return new BinaryOperationWithIdentity<AlgebraicInteger>() {
            public AlgebraicInteger op(AlgebraicInteger t1,AlgebraicInteger t2) {
                if (t1.x.length != minimalPolynomial.length)
                    throw new IllegalArgumentException();
                if (t2.x.length != minimalPolynomial.length)
                    throw new IllegalArgumentException();
                
                int [] tmp = new int [2 * minimalPolynomial.length - 1];
                for (int i = 0;i < minimalPolynomial.length;i++)
                    for (int j = 0;j < minimalPolynomial.length;j++)
                        tmp [i + j] += t1.x [i] * t2.x [j];
                // reduce the product using the minimal polynomial
                for (int i = tmp.length - 1;i >= minimalPolynomial.length;i--)
                    for (int j = 0;j < minimalPolynomial.length;j++)
                        tmp [i - minimalPolynomial.length + j] -= tmp [i] * minimalPolynomial [j];

                int [] sum = new int [minimalPolynomial.length];
                for (int i = 0;i < sum.length;i++)
                    sum [i] = tmp [i];
                return new AlgebraicInteger(sum);
            }
            
            public AlgebraicInteger identity() {
                return one;
            }
        };
    }
}
