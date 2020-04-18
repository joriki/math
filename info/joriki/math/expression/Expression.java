package info.joriki.math.expression;

public interface Expression<T> {
    T evaluate(Valuation<T> v);
    Expression<T> differentiate(Variable<T> x);
}
