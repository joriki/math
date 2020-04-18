package info.joriki.math.algebra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PolynomialRing<T> implements Ring<Polynomial<T>> {
	int nvars;
	Ring<T> ring;
	
	public PolynomialRing (int nvars,Ring<T> ring) {
		this.nvars = nvars;
		this.ring = ring;
	}

	public InvertibleBinaryOperation<Polynomial<T>> addition () {
		return new InvertibleBinaryOperation<Polynomial<T>> () {
			Polynomial<T> zero = new Polynomial<T> (ring);
			
			public Polynomial<T> identity () {
				return zero;
			}

			public Polynomial<T> op (Polynomial<T> t1,Polynomial<T> t2) {
				Polynomial<T> sum = new Polynomial<T> (t1);
				sum.add (t2);
				return sum;
			}

			public Polynomial<T> inverse (Polynomial<T> p) {
				Polynomial<T> inverse = new Polynomial<T> (ring);
				for (Map.Entry<Polynomial.Term,T> entry : p.coefficients.entrySet ())
					inverse.coefficients.put (entry.getKey (),ring.addition ().inverse (entry.getValue ()));
				return inverse;
			}
		};
	}


	public BinaryOperationWithIdentity<Polynomial<T>> multiplication () {
		return new BinaryOperationWithIdentity<Polynomial<T>> () {
			Polynomial<T> one = new Polynomial<T> (ring);
			{
				one.coefficients.put (new Polynomial.Term (nvars),ring.multiplication ().identity ());
			}
			
			public Polynomial<T> identity () {
				return one;
			}

			public Polynomial<T> op (Polynomial<T> p1,Polynomial<T> p2) {
				Polynomial<T> product = new Polynomial<T> (ring);
				for (Map.Entry<Polynomial.Term,T> entry1 : p1.coefficients.entrySet ())
					for (Map.Entry<Polynomial.Term,T> entry2 : p2.coefficients.entrySet ())
						product.add (new Polynomial.Term (entry1.getKey (),entry2.getKey ()),ring.multiplication ().op (entry1.getValue (),entry2.getValue ()));
				return product;
			}
		};
	}
	
	public List<Polynomial<T>> generators () {
		List<Polynomial<T>> generators = new ArrayList<Polynomial<T>> ();
		for (int i = 0;i < nvars;i++) {
			Polynomial.Term t = new Polynomial.Term (nvars);
			t.counts [i] = 1;
			Polynomial<T> p = new Polynomial<T> (ring);
			p.add (t,ring.multiplication ().identity ());
			generators.add (p);
		}
		return generators;
	}
}
