package info.joriki.math.expression;

import info.joriki.math.algebra.Field;
import info.joriki.math.algebra.InvertibleBinaryOperation;

public class ExpressionField<T> implements Field<Expression<T>>{
    Field<T> field;
    
    public ExpressionField(Field<T> field) {
        this.field = field;
    }

    public InvertibleBinaryOperation<Expression<T>> multiplication() {
        return new InvertibleBinaryOperation<Expression<T>>() {
            public Expression<T> identity() {
                return Constant.build(field, field.multiplication().identity());
            }

            public Expression<T> op(Expression<T> t1,Expression<T> t2) {
                return Product.build(field,t1,t2);
            }

            public Expression<T> inverse(Expression<T> t) {
                return Product.reciprocal(field, t);
            }
        };
    }

    public InvertibleBinaryOperation<Expression<T>> addition() {
        return new InvertibleBinaryOperation<Expression<T>>() {
            public Expression<T> identity() {
                return Constant.build(field, field.addition().identity());
            }

            public Expression<T> op(Expression<T> t1,Expression<T> t2) {
                return Sum.build(field,t1,t2);
            }

            public Expression<T> inverse(Expression<T> t) {
                return AdditiveInverse.build(field, t);
            }
        };
    }

}
