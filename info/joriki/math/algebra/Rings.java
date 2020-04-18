package info.joriki.math.algebra;

public class Rings {
	private Rings () {}
	public static <T> T generate (Ring<T> r,int n) {
		return
				n < 0 ? r.addition ().inverse (generate (r,-n)) : 
				n == 0 ? r.addition ().identity () :
			    n == 1 ? r.multiplication ().identity () :
			    (n & 1) == 0 ? twice (r,generate (r,n / 2)) :
			    r.addition ().op (r.multiplication ().identity (),generate (r,n - 1));
	}
	
	public static <T> T twice (Ring<T> r,T t) {
		return r.addition ().op (t,t);
	}
}
