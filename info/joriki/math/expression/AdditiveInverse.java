package info.joriki.math.expression;

import info.joriki.math.algebra.Field;

public class AdditiveInverse<T> implements Expression<T> {
    Field<T> field;
    Expression<T> e;
    
    private AdditiveInverse(Field<T> field,Expression<T> e) {
        this.e = e;
        this.field = field;
    }
    
    public String toString() {
        return "(-" + e.toString() + ')';
    }

    public T evaluate(Valuation<T> v) {
        return field.addition().inverse(e.evaluate(v));
    }

    public Expression<T> differentiate(Variable<T> x) {
        return new AdditiveInverse<>(field, e.differentiate(x));
    }
    
    public static <T> Expression<T> build(Field<T> field,Expression<T> e) {
        return new AdditiveInverse<>(field, e);
    }
}
