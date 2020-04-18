package info.joriki.math.algebra;

public class RationalField implements Field<BigRational> {
	public final static RationalField Q = new RationalField ();
	
	public InvertibleBinaryOperation<BigRational> addition () {
		return new InvertibleBinaryOperation<BigRational> () {
			public BigRational op (BigRational r1,BigRational r2) {
				return BigRational.sum (r1,r2);
			}
			
			public BigRational identity () {
				return BigRational.ZERO;
			}
			
			public BigRational inverse (BigRational r) {
				return r.negate ();
			}
		};
	}

	public InvertibleBinaryOperation<BigRational> multiplication () {
		return new InvertibleBinaryOperation<BigRational> () {
			public BigRational op (BigRational r1,BigRational r2) {
				return BigRational.product (r1,r2);
			}
			
			public BigRational identity () {
				return BigRational.ONE;
			}

			public BigRational inverse (BigRational r) {
				return r.reciprocal ();
			}
		};
	}

}
