package info.joriki.math.algebra;

public class IntegerRing implements Ring<Integer> {
	public final static IntegerRing Z = new IntegerRing ();
	
	public InvertibleBinaryOperation<Integer> addition () {
		return new InvertibleBinaryOperation<Integer> () {
			public Integer op (Integer i1,Integer i2) {
				return i1 + i2;
			}
			
			public Integer identity () {
				return 0;
			}
			
			public Integer inverse (Integer i) {
				return -i;
			}
		};
	}

	public BinaryOperationWithIdentity<Integer> multiplication () {
		return new BinaryOperationWithIdentity<Integer> () {
			public Integer op (Integer i1,Integer i2) {
				return i1 * i2;
			}
			
			public Integer identity () {
				return 1;
			}
		};
	}

}
