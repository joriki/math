package info.joriki.math.stackexchange;
import java.util.ArrayList;
import java.util.List;

import info.joriki.math.algebra.Field;
import info.joriki.math.algebra.Matrix;
import info.joriki.math.algebra.RealField;
import info.joriki.math.expression.AdditiveInverse;
import info.joriki.math.expression.Constant;
import info.joriki.math.expression.Exponential;
import info.joriki.math.expression.Expression;
import info.joriki.math.expression.ExpressionField;
import info.joriki.math.expression.Power;
import info.joriki.math.expression.Product;
import info.joriki.math.expression.Sum;
import info.joriki.math.expression.Valuation;
import info.joriki.math.expression.Variable;
import info.joriki.math.numerics.NewtonMethod;

public class Question3627502 {
    public static void main(String [] args) {
        Field<Double> R = RealField.R;
        Field<Expression<Double>> Rx = new ExpressionField<Double>(R);
        
        for (int n = 1;;n++) {
            List<Variable<Double>> times = new ArrayList<>();
            for (int i = 0;i < n;i++)
                times.add(Variable.build(R, "t" + i));
            Valuation<Double> values = new Valuation<>();
            for (int i = 0;i < n;i++)
                values.put(times.get(i),(double)(n - i));
            Matrix<Expression<Double>> a = new Matrix<>(Rx,n,n);
            a.zero();
            Expression<Double> one = Constant.build(R,1.);
            Expression<Double> minusOne = Constant.build(R,-1.);
            Matrix<Expression<Double>> b = new Matrix<>(Rx,n,1);
            for (int i = 0;i < n;i++) {
                b.set(i, 0, minusOne);
                Variable<Double> t = times.get(i);
                Expression<Double> exp = Exponential.build(AdditiveInverse.build(R, t));
                List<Expression<Double>> subtracted = new ArrayList<>();
                double factorial = 1;
                for (int j = 0,k = i;k <= n;j++,k++,factorial /= j) {
                    List<Expression<Double>> factors = new ArrayList<>();
                    factors.add(exp);
                    factors.add(Power.build(R, t, j));
                    factors.add(Constant.build(R, factorial));
                    Expression<Double> probability = Product.build(R, factors);
                    if (k > 0) {
                        if (k < n)
                            a.set(i, k,probability);
                        subtracted.add(probability);
                    }
                }
                Expression<Double> complement = AdditiveInverse.build(R,Sum.build(R, subtracted));
                a.set(i, 0, i == 0 ? complement : Sum.build(R,one,complement));
                if (i != 0)
                    a.set(i, i, Sum.build(R,a.get(i, i),minusOne));
            }
            
            a.solveFor(b);
            Expression<Double> f = b.get(0,0);
            NewtonMethod.optimize(f, values);
        }
    }
}
