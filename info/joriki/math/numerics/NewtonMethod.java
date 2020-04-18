package info.joriki.math.numerics;

import java.util.ArrayList;
import java.util.List;

import info.joriki.math.algebra.Matrix;
import info.joriki.math.algebra.RealField;
import info.joriki.math.expression.Expression;
import info.joriki.math.expression.Valuation;
import info.joriki.math.expression.Variable;

public class NewtonMethod {
    public static void optimize(Expression<Double> f,Valuation<Double> values) {
        List<Variable<Double>> variables = new ArrayList<>(values.keySet());
        int n = variables.size();
        
        Expression<Double> [] firstDerivatives = new Expression [n];
        for (int i = 0;i < n;i++)
            firstDerivatives [i] = f.differentiate(variables.get(i));

        Expression<Double> [] [] secondDerivatives = new Expression [n] [n];
        for (int i = 0;i < n;i++)
            for (int j = 0;j <= i;j++)
                secondDerivatives [i] [j] = f.differentiate(variables.get(i)).differentiate(variables.get(j));

        double last = Double.POSITIVE_INFINITY;
        
        for (;;) {
            double value = f.evaluate(values);
            if (value >= last)
                break;
            last = value;
            
            System.out.println(values + " : " + value);
            
            Matrix<Double> gradient = new Matrix<>(RealField.R,n,1);
            Matrix<Double> hessian = new Matrix<>(RealField.R,n,n);
            
            for (int i = 0;i < n;i++)
                gradient.set(i,0,firstDerivatives [i].evaluate(values));

            for (int i = 0;i < n;i++)
                for (int j = 0;j <= i;j++) {
                    Double d = secondDerivatives [i] [j].evaluate(values);
                    hessian.set(i,j,d);
                    hessian.set(j,i,d);
                }
            
            hessian.solveFor(gradient);
            Valuation<Double> newValues = new Valuation<>();
 
            for (int i = 0;i < n;i++) {
                Variable<Double> v = variables.get(i);
                newValues.put(v,values.get(v) - gradient.get(i,0));
            }
            values = newValues;
        }
    }
}
