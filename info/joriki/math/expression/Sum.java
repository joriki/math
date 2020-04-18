package info.joriki.math.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import info.joriki.math.algebra.Field;

public class Sum<T> implements Expression<T> {
    Field<T> field;
    List<Expression<T>> terms;

    public Sum(Field<T> field,List<Expression<T>> terms) {
        this.terms = terms;
        this.field = field;
    }

    public String toString() {
        return '(' + terms.stream().map(Expression::toString).collect(Collectors.joining(" + ")) + ')';
    }
    
    public T evaluate(Valuation<T> v) {
        T sum = field.addition().identity();
        for (Expression<T> term : terms)
            sum = field.addition().op(sum, term.evaluate(v));
        return sum;
    }

    public Expression<T> differentiate(Variable<T> x) {
        List<Expression<T>> differentiatedTerms = new ArrayList<>();
        for (Expression<T> term : terms) {
            Expression<T> termDerivative = term.differentiate(x);
            if (!termDerivative.equals(Constant.build(field,field.addition().identity())))
                differentiatedTerms.add(termDerivative);
        }
        return build(field,differentiatedTerms);
    }
    
    static public <T> Expression<T> build(Field<T> field,List<Expression<T>> terms) {
        Expression<T> zero = Constant.build(field,field.addition().identity());
        List<Expression<T>> nonTrivialTerms = new ArrayList<>();
        for (Expression<T> term : terms)
            if (!term.equals(zero))
                nonTrivialTerms.add(term);
        switch(nonTrivialTerms.size()) {
        case 0: return zero;
        case 1: return nonTrivialTerms.get(0);
        default: return new Sum<>(field,nonTrivialTerms);
        }
    }

    static public <T> Expression<T> build(Field<T> field,Expression<T> term1,Expression<T> term2) {
        List<Expression<T>> terms = new ArrayList<>();
        terms.add(term1);
        terms.add(term2);
        return build(field,terms);
    }
}
