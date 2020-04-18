package info.joriki.math.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Matrix<T> {
	public int m;
	public int n;
	public Ring<T> ring;
	
	Object [] [] matrix;
	
	T zero;
	T one;
	
	public Matrix (Ring<T> ring,int m,int n) {
		this.ring = ring;
		this.m = m;
		this.n = n;

		zero = ring.addition ().identity ();
		one = ring.multiplication ().identity ();
		
		matrix = new Object [m] [n];
	}

	public void zero () {
		for (int i = 0;i < m;i++)
			for (int j = 0;j < n;j++)
				matrix [i] [j] = zero;
	}

	public void identity () {
	    if (m != n)
		throw new Error ();
	    zero ();
	    for (int i = 0;i < n;i++)
		matrix [i] [i] = one;
	}
	
	public T get (int i,int j) {
		return (T) matrix [i] [j];
	}
	
	public void set (int i,int j,T t) {
		matrix [i] [j] = t;
	}

	public void add (int i,int j,T t) {
	    set (i,j,ring.addition ().op (get (i,j),t));
	}
	
	public Matrix<T> getRowEchelonForm () {
		Matrix<T> rowEchelonForm = copy ();
		rowEchelonForm.toRowEchelonForm ();
		return rowEchelonForm;
	}
	
	public void toRowEchelonForm () {
		InvertibleBinaryOperation<T> plus = ring.addition ();
		BinaryOperationWithIdentity<T> times = ring.multiplication ();
		
		for (int row = 0,column = 0;column < n;column++) {
			int pivot;
			for (pivot = row;pivot < m;pivot++)
				if (!get (pivot,column).equals (zero))
					break;
			if (pivot != m) { // is this column already done?
				if (pivot != row)
					// swap rows row and pivot
					for (int j = column;j < n;j++) {
						T tmp = get (pivot,j);
						set (pivot,j,get (row,j));
						set (row,j,tmp);
					}
				for (int i = row + 1;i < m;i++)
					if (!(get (i,column).equals (zero))) {
						for (int j = column + 1;j < n;j++)
							// a_i,j = a_row,column * a_i,j - a_row,j * a_i,column 
							set (i,j,plus.op (times.op (get (row,column),get (i,j)),plus.inverse (times.op (get (row,j),get (i,column)))));
						set (i,column,zero);
					}
				row++;
			}
		}
	}
	
	public Matrix<T> getReducedRowEchelonForm () {
		Matrix<T> reducedRowEchelonForm = copy ();
		reducedRowEchelonForm.toReducedRowEchelonForm ();
		return reducedRowEchelonForm;
	}
	
	public void toReducedRowEchelonForm () {
		InvertibleBinaryOperation<T> addition = ring.addition ();
		InvertibleBinaryOperation<T> multiplication = (InvertibleBinaryOperation<T>) ring.multiplication ();
		toRowEchelonForm ();
		int j = 0;
		for (int i = 0;i < m;i++) {
			while (j < n) {
				T aij = get (i,j);
				if (!aij.equals (zero)) {
					aij = multiplication.inverse (aij);
					for (int k = j + 1;k < n;k++) {
						T aik = multiplication.op (get (i,k),aij);
						set (i,k,aik);
						for (int l = 0;l < i;l++)
							set (l,k,addition.op (get (l,k),addition.inverse (multiplication.op (aik,get (l,j)))));
					}
					set (i,j,one);
					for (int l = 0;l < i;l++)
						set (l,j,zero);
					j++;
					break;
				}
				j++;
			}
		}
	}

	public Matrix<T> getKernelBasis () {
		Matrix<T> reducedRowEchelonForm = getReducedRowEchelonForm ();
		boolean [] hasLeadingOne = new boolean [n];
		List<Integer> indices = new ArrayList<Integer> ();
		int j = 0;
		for (int i = 0;i < m;i++)
			while (j < n) {
				T aij = reducedRowEchelonForm.get (i,j);
				if (!aij.equals (zero)) {
					if (!aij.equals (one))
						throw new Error ();
					indices.add (j);
					hasLeadingOne [j] = true;
					j++;
					break;
				}
				j++;
			}
		InvertibleBinaryOperation<T> addition = ring.addition ();
		Matrix<T> kernelBasis = new Matrix<T> (ring,n,n - indices.size ());
		kernelBasis.zero ();
		int k = 0;
		for (int i = 0;i < n;i++)
			if (!hasLeadingOne [i]) {
				kernelBasis.set (i,k,one);
				for (int l = 0;l < indices.size ();l++)
					kernelBasis.set (indices.get (l),k,addition.inverse (reducedRowEchelonForm.get (l,i)));
				k++;
			}
		if (k != kernelBasis.n)
			throw new Error (k + " / " + indices.size ());
		return kernelBasis;
	}

	public int rank () {
		return getRowEchelonForm ().getEchelonRank ();
	}
	
	private int getEchelonRank () {
		for (int i = m - 1;i >= 0;i--)
			for (int j = 0;j < n;j++)
				if (!get (i,j).equals (zero))
					return i + 1;
		return 0;
	}

	public Matrix<T> getTranspose () {
		Matrix<T> transpose = new Matrix<T> (ring,n,m);
		for (int i = 0;i < m;i++)
			for (int j = 0;j < n;j++)
				transpose.set (j,i,get (i,j));
		return transpose;
	}
	
	public static <T> Matrix<T> sum (Matrix<T> A,Matrix<T> B) {
		if (A.ring != B.ring || A.m != B.m || A.n != B.n)
			throw new IllegalArgumentException (A.m + " x " + A.n + ", " + B.m + " x " + B.n);
		
		InvertibleBinaryOperation<T> addition = A.ring.addition ();
		Matrix<T> sum = new Matrix<T> (A.ring,A.m,A.n);
		
		for (int i = 0;i < A.m;i++)
			for (int j = 0;j < A.n;j++)
				sum.set (i,j,addition.op (A.get (i,j),B.get (i,j)));
		
		return sum;
	}
	
	public static <T> Matrix<T> product (Matrix<T> A,Matrix<T> B) {
		if (A.ring != B.ring || A.n != B.m)
			throw new IllegalArgumentException ();
		BinaryOperation<T> addition = A.ring.addition ();
		BinaryOperation<T> times = A.ring.multiplication ();

		Matrix<T> product = new Matrix<T> (A.ring,A.m,B.n);
		for (int i = 0;i < A.m;i++)
			for (int k = 0;k < B.n;k++) {
				T t = A.zero;
				for (int j = 0;j < A.n;j++)
					t = addition.op (t,times.op (A.get (i,j),B.get (j,k)));
				product.set (i,k,t);
			}
		return product;
	}
	
	public Matrix<T> copy () {
		Matrix<T> copy = new Matrix<T> (ring,m,n);
		for (int i = 0;i < m;i++)
			for (int j = 0;j < n;j++)
				copy.matrix [i] [j] = matrix [i] [j]; // not cloning individual objects
		return copy;
	}
	
	public void solveFor (Matrix<T> b) {
		if (m != n || m != b.m)
			throw new IllegalArgumentException ();

		Field<T> field = (Field<T>) ring;
		InvertibleBinaryOperation<T> times = field.multiplication ();
		InvertibleBinaryOperation<T> plus = field.addition ();

		for (int i = 0;i < m;i++) {
//			System.out.println ("****** " + i);
//			System.out.println (this);
	      int pivotIndex = i;

	      for (;pivotIndex < m;pivotIndex++)
	    	  if (!get (pivotIndex,i).equals (zero))
	    		  break;

	      if (pivotIndex == m)
	    	  throw new ArithmeticException ("singular system");

	      // swap two equations to bring the pivot to the diagonal
	      Object [] swapRow = matrix [pivotIndex];
	      matrix [pivotIndex] = matrix [i];
	      matrix [i] = swapRow;
	      for (int j = 0;j < b.n;j++) {
	    	  T swap = b.get (pivotIndex,j);
	    	  b.set (pivotIndex,j,b.get (i,j));
	    	  b.set (i,j,swap);
	      }

	      // normalize current row.
	      T pivot = times.inverse (get (i,i));
	      for (int j = i + 1;j < n;j++)
	    	  set (i,j,times.op (get (i,j),pivot));
	      for (int j = 0;j < b.n;j++)
	    	  b.set (i,j,times.op (b.get (i,j),pivot));
	    	  
	      // subtract multiples of it from the ones below.
	      for (int k = i + 1;k < m;k++) {
	    	  T first = get (k,i);

	    	  if (!first.equals (zero)) {
	    		  for (int j = i + 1;j < n;j++)
	    			  // buf [k] [j] -= first * buf [i] [j];
	    			  set (k,j,plus.op (get (k,j),plus.inverse (times.op (first,get (i,j)))));
	    		  for (int j = 0;j < b.n;j++)
	    			  // x [k] -= first * x [i];
	    			  b.set (k,j,plus.op (b.get (k,j),plus.inverse (times.op (first,b.get (i,j)))));
	    	  }
	      }
		}

		// now back-substitute
	    for (int k = m - 1;k > 0;k--)
	      for (int i = 0;i < k;i++)
	    	  for (int j = 0;j < b.n;j++)
	    		  b.set (i,j,plus.op (b.get (i,j),plus.inverse (times.op (get (i,k),b.get (k,j)))));
	}
	
	public static <T> Matrix<T> subspaceIntersection (Matrix<T> m1,Matrix<T> m2) {
		if (m1.m != m2.m)
			throw new IllegalArgumentException ();
		if (m1.ring != m2.ring)
			throw new IllegalArgumentException ();

		Matrix<T> m = new Matrix<T> (m1.ring,m1.m,m1.n + m2.n);
		
		for (int i = 0;i < m1.m;i++) {
			for (int j = 0;j < m1.n;j++)
				m.set (i,j,m1.get (i,j));
			for (int j = 0;j < m2.n;j++)
				m.set (i,m1.n + j,m2.get (i,j));
		}
		
		Matrix<T> kernelBasis = m.getKernelBasis ();
		return product (m1,kernelBasis.getSubmatrix (m1.n,kernelBasis.n));
	}
	
	public Matrix<T> getSubmatrix (int m,int n) {
		return getSubmatrix (0,m,0,n);
	}
	
	public Matrix<T> getSubmatrix (int m1,int m2,int n1,int n2) {
		if (m1 > m2 || n1 > n2 || m1 < 0 || n2 < 0 || m2 > m || n2 > n)
			throw new IllegalArgumentException ("[" + m1 + "," + m2 + "] x [" + n1 + "," + n2 + "] from " + m + " x " + n);
		
		Matrix<T> submatrix = new Matrix<T> (ring,m2 - m1,n2 - n1);
		
		for (int i = m1;i < m2;i++)
			for (int j = n1;j < n2;j++)
				submatrix.set (i - m1,j - n1,get (i,j));
		
		return submatrix;
	}
	
	public Matrix<T> changeRing (Ring<T> ring) {
		Matrix<T> result = new Matrix<T> (ring,m,n);
		for (int i = 0;i < m;i++)
			for (int j = 0;j < n;j++)
				result.set (i,j,get (i,j));
		return result;
	}
	
	public String toString () {
		StringBuilder matrixBuilder = new StringBuilder ();
		
		for (int i = 0;i < m;i++) {
			for (int j = 0;j < n;j++) {
				if (j != 0)
					matrixBuilder.append (" ");
				matrixBuilder.append (matrix [i] [j]);
			}
			matrixBuilder.append ('\n');
		}
		
		return matrixBuilder.toString ();
	}

	class ClowList {
		int d = n + 1;
		Object [] clows = new Object [2 * d * d * d];
		
		public ClowList () {
			Arrays.fill (clows,zero);
		}
		
		private int index (int l,int c,int c0,int s) {
			return s + 2 * (c0 + d * (c + d * l));
		}
		
		public T get (int l,int c,int c0,int s) {
			return (T) clows [index (l,c,c0,s)];
		}
		
		public void set (int l,int c,int c0,int s,T t) {
			clows [index (l,c,c0,s)] = t;
		}

		public void plus (int l,int c,int c0,int s,T t) {
			T old = get (l,c,c0,s);
			set (l,c,c0,s,old == null ? t : ring.addition ().op (old,t));
		}
	}; 
	
	public T determinant () {
		return determinantByClows ();
	}
	
	public T determinantByClows () {
		if (m != n)
			throw new IllegalArgumentException ();
		
		ClowList clows = new ClowList ();

		for (int c0 = 0;c0 < n;c0++)
			clows.set (0,c0,c0,1,one);

		for (int l = 0;l < n;l++) {
			for (int c = 0;c < n;c++) {
				for (int c0 = 0;c0 <= c;c0++)
					for (int s = 0;s <= 1;s++) {
						T t = clows.get (l,c,c0,s);
						for (int cp = n - 1;cp > c0;cp--)
							clows.plus (l + 1,cp,c0,s,ring.multiplication ().op (t,(T) matrix [c] [cp]));
						for (int c0p = n;c0p > c0;c0p--)
							clows.plus (l + 1,c0p,c0p,1 - s,ring.multiplication ().op (t,(T) matrix [c] [c0]));
					}
			}
		}

		return ring.addition ().op (clows.get (n,n,n,1),ring.addition ().inverse (clows.get (n,n,n,0)));
	}
	
	//     A B
	// det     = (d - 1) det A + det (A - BC)
	//     C d
	public T determinantByDimensionalReduction () {
		if (m != n)
			throw new IllegalArgumentException ();
		return determinantByDimensionalReduction (n - 1);
	}
	
	private T determinantByDimensionalReduction (int k) {
		if (k == 0)
			return get (0,0);
		
		InvertibleBinaryOperation<T> plus = ring.addition ();
		BinaryOperationWithIdentity<T> times = ring.multiplication ();
		T result = times.op (plus.op (get (k,k),plus.inverse (times.identity ())),determinantByDimensionalReduction (k - 1));
		for (int i = 0;i < k;i++)
			for (int j = 0;j < k;j++)
				set (i,j,plus.op (get (i,j),plus.inverse (times.op (get (i,k),get (k,j)))));
		result = plus.op (result,determinantByDimensionalReduction (k - 1));
		for (int i = 0;i < k;i++)
			for (int j = 0;j < k;j++)
				set (i,j,plus.op (get (i,j),times.op (get (i,k),get (k,j))));
		return result;
	}
	
	public T determinantByLaplaceExpansion () {
		return determinantByLaplaceExpansion (-1);
	}
	
	public T determinantByLaplaceExpansion (int sign) {
		if (m != n)
			throw new IllegalArgumentException ();
		return determinantByLaplaceExpansion (Rings.generate (ring,sign),0,new boolean [n]);
	}
	
	private T determinantByLaplaceExpansion (T sign,int k,boolean [] done) {
		if (k == n)
			return ring.multiplication ().identity ();
		T result = ring.addition ().identity ();
		T power = ring.multiplication ().identity ();
		for (int i = 0;i < n;i++)
			if (!done [i]) {
				T aki = get (k,i);
				if (!aki.equals (ring.addition ().identity ())) {
					done [i] = true;
					result = ring.addition ().op (result,ring.multiplication ().op (power,ring.multiplication ().op (aki,determinantByLaplaceExpansion (sign,k + 1,done))));
					done [i] = false;
				}
				power = ring.multiplication ().op (power,sign);
			}
		return result;
	}
	
	public T trace () {
		if (m != n)
			throw new IllegalArgumentException ();
		T trace = ring.addition ().identity ();
		for (int i = 0;i < m;i++)
			trace = ring.addition ().op (trace,get (i,i));
		return trace;
	}
	
	public void add (T factor,Matrix<T> matrix) {
		if (matrix.m != m || matrix.n != n)
			throw new IllegalArgumentException ();
		for (int i = 0;i < n;i++)
			for (int j = 0;j < m;j++)
				add (i,j,ring.multiplication ().op (factor,matrix.get (i,j)));
	}
}
