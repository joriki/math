package info.joriki.math.algebra;

public class FiniteField implements Field<Integer> {
	int p;
	
	public FiniteField (int p) {
		this.p = p;

		for (int i = 3;;i += 2) {
			if (i * i > p) {
				if ((p % 2) == 0 && p != 2)
					break;
				return;
			}
			if ((p % i) == 0 && p != i)
				break;
		}
		throw new IllegalArgumentException ("only finite fields of prime order implemented");
	}

	public InvertibleBinaryOperation<Integer> multiplication () {
		return new InvertibleBinaryOperation<Integer> () {
			public Integer op (Integer t1,Integer t2) {
				return (t1 * t2) % p;
			}
			
			public Integer identity () {
				return 1;
			}
			
			public Integer inverse (Integer t) {
				return pow (t,p - 2);
			}
		};
	}

	public InvertibleBinaryOperation<Integer> addition () {
		return new InvertibleBinaryOperation<Integer> () {
			public Integer op (Integer t1,Integer t2) {
				return (t1 + t2) % p;
			}
			
			public Integer identity () {
				return 0;
			}
			
			public Integer inverse (Integer t) {
				return t == 0 ? 0 : p - t; 
			}
		};
	}

	public int pow (int x,int n) {
		int product = 1;
		for (int bit = 1;bit <= n;bit <<= 1) {
			if ((n & bit) != 0) {
				product *= x;
				product %= p;
			}
			x *= x;
			x %= p;
		}
		return product;
	}
}
