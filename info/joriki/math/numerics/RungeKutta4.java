package info.joriki.math.numerics;

public class RungeKutta4 {
	final static double [] fractions = {0,1/2.,1/2.,1};
	final static double [] weights = {1/6.,2/6.,2/6.,1/6.};
	
	public static double [] [] perform (double a,double b,int n,double [] initialValues,DifferentialEquationSpecification specification) {
		double h = (b - a) / n;
		double [] [] intermediate = new double [5] [];
		intermediate [0] = new double [initialValues.length];
		double [] y = new double [initialValues.length];
		double [] [] values = new double [n + 1] [initialValues.length];
		values [0] = initialValues.clone ();
		double t = a;
		for (int i = 0;i < n;i++) {
			for (int j = 0;j < 4;j++) {
				double fraction = fractions [j];
				for (int k = 0;k < initialValues.length;k++)
					y [k] = values [i] [k] + fraction * h * intermediate [j] [k];
				intermediate [j + 1] = specification.f (t + fraction * h,y);
			}
			values [i + 1] = values [i].clone ();
			for (int j = 0;j < 4;j++)
				for (int k = 0;k < initialValues.length;k++)
					values [i + 1] [k] += h * weights [j] * intermediate [j + 1] [k];
			t += h;
		}
		return values;
	}
}
