package info.joriki.math.algebra;

public class RealField implements Field<Double> {
	public final static RealField R = new RealField ();
	
	public InvertibleBinaryOperation<Double> addition () {
		return new InvertibleBinaryOperation<Double> () {
			public Double op (Double r1,Double r2) {
				return r1 + r2;
			}
			
			public Double identity () {
				return 0.;
			}
			
			public Double inverse (Double r) {
				return -r;
			}
		};
	}

	public InvertibleBinaryOperation<Double> multiplication () {
		return new InvertibleBinaryOperation<Double> () {
			public Double op (Double r1,Double r2) {
				return r1 * r2;
			}
			
			public Double identity () {
				return 1.;
			}

			public Double inverse (Double r) {
				return 1 / r;
			}
		};
	}
}
