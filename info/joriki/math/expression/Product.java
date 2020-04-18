package info.joriki.math.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import info.joriki.math.algebra.Field;

public class Product<T> implements Expression<T> {
    Field<T> field;
    List<Expression<T>> factors;
    
    private Product(Field<T> field,List<Expression<T>> factors) {
        this.field = field;
        this.factors = factors;
    }

    public String toString() {
        return '(' + factors.stream().map(Expression::toString).collect(Collectors.joining(" ⋅ ")) + ')';
    }
    
    public T evaluate(Valuation<T> v) {
        T sum = field.multiplication().identity();
        for (Expression<T> factor : factors)
            sum = field.multiplication().op(sum, factor.evaluate(v));
        return sum;
    }

    public Expression<T> differentiate(Variable<T> x) {
        switch(factors.size()) {
        case 0 : return Constant.build(field, field.addition().identity());
        case 1 : return factors.get(0).differentiate(x);
        case 2 :
            Expression<T> f = factors.get(0);
            Expression<T> g = factors.get(1);
            Expression<T> df = f.differentiate(x);
            Expression<T> dg = g.differentiate(x);
            return Sum.build(field,build(field,df,g),build(field,f,dg));
        }

        List<Expression<T>> terms = new ArrayList<>();
        for (Expression<T> factor : factors) {
            Expression<T> differentiatedFactor = factor.differentiate(x);
            terms.add(quotient(field,differentiatedFactor,factor));
        }
        return build(field,this,Sum.build(field,terms));
    }
    
    static public <T> Expression<T> build(Field<T> field,List<Expression<T>> factors) {
        Expression<T> zero = Constant.build(field,field.addition().identity());
        Expression<T> one = Constant.build(field,field.multiplication().identity());
        List<Expression<T>> nonTrivialFactors = new ArrayList<>();
        for (Expression<T> factor : factors)
            if (factor.equals(zero))
                return zero;
            else if (!factor.equals(one))
                nonTrivialFactors.add(factor);
        switch(nonTrivialFactors.size()) {
        case 0: return one;
        case 1: return nonTrivialFactors.get(0);
        default: return new Product<>(field,nonTrivialFactors);
        }
    }
    
    static public <T> Expression<T> build(Field<T> field,Expression<T> factor1,Expression<T> factor2) {
        List<Expression<T>> factors = new ArrayList<>();
        factors.add(factor1);
        factors.add(factor2);
        return build(field,factors);
    }
    
    static public <T> Expression<T> reciprocal(Field<T> field,Expression<T> e) {
        return Power.build(field,e,-1);
    }
    
    static public <T> Expression<T> quotient(Field<T> field,Expression<T> num,Expression<T> den) {
        return build(field,num,reciprocal(field,den));
    }
}
