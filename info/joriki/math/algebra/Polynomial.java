package info.joriki.math.algebra;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Polynomial<T> {
	static int maxTerms;
	
	static class Term {
		int [] counts;
		
		public Term (int n) {
			counts = new int [n];
		}
		
		public Term (Term term) {
			this (term.counts);
		}
		
		public Term (int [] counts) {
			this.counts = counts.clone ();
		}

		public Term (Term t1,Term t2) {
			this (t1);
			if (t2.counts.length != counts.length)
				throw new IllegalArgumentException ();
			for (int i = 0;i < counts.length;i++)
				counts [i] += t2.counts [i];
		}

		public boolean equals (Object o) {
			return Arrays.equals (((Term) o).counts,counts);
		}
		
		public int hashCode () {
			return Arrays.hashCode (counts);
		}
		
		public String toString () {
			return Arrays.toString (counts);
		}
	}
	
	Ring<T> ring;
	Map<Term,T> coefficients;
	
	public Polynomial (Ring<T> ring) {
		this.ring = ring;
		coefficients = new HashMap<Term,T> ();
	}
	
	public Polynomial (Polynomial<T> p) {
		ring = p.ring;
		coefficients = new HashMap<Term,T> (p.coefficients);
	}
	
	public boolean equals (Object o) {
		return o instanceof Polynomial && ((Polynomial) o).coefficients.equals (coefficients);
	}
	
	public void add (Polynomial<T> p) {
		for (Map.Entry<Term,T> entry : p.coefficients.entrySet ())
			add (entry.getKey (),entry.getValue ());
	}
	
	public void add (Term term,T t) {
		T old = coefficients.get (term);
		put (term,old == null ? t : ring.addition ().op (old,t));
	}
	
	private void put (Term term,T t) {
		if (t.equals (ring.addition ().identity ()))
			coefficients.remove (term);
		else {
			coefficients.put (term,t);
			maxTerms = Math.max (maxTerms,coefficients.size ());
		}
	}
	
	public String toString () {
		StringBuilder builder = new StringBuilder ();
		for (Map.Entry<Term,T> entry : coefficients.entrySet ()) {
			if (builder.length () != 0)
				builder.append (" + ");
			builder.append (entry.getValue () + " " + entry.getKey ());
		}
		return builder.length () == 0 ? "0" : builder.toString ();
	}
}
