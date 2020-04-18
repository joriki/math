package info.joriki.math.expression;

import info.joriki.math.algebra.RealField;

public class Exponential implements Expression<Double> {
    Expression<Double> exponent;
    
    private Exponential(Expression<Double> exponent) {
        this.exponent = exponent;
    }
    
    public String toString() {
        return "exp(" + exponent + ')';
    }

    public Double evaluate(Valuation<Double> v) {
        return Math.exp(exponent.evaluate(v));
    }
    
    public Expression<Double> differentiate(Variable<Double> x) {
        return Product.build(RealField.R,exponent.differentiate(x),this);
    }
    
    public static Expression<Double> build(Expression<Double> exponent) {
        return exponent instanceof Constant ?
                Constant.build(RealField.R,Math.exp(((Constant<Double>) exponent).value)) :
                new Exponential(exponent);    
    }
}
