package info.joriki.math.expression;

import info.joriki.math.algebra.Field;
import info.joriki.math.algebra.InvertibleBinaryOperation;

public class Power<T> implements Expression<T> {
    Field<T> field;
    Expression<T> base;
    int exponent;

    private Power(Field<T> field,Expression<T> base,int exponent) {
        this.field = field;
        this.base = base;
        this.exponent = exponent;
    }
    
    public String toString() {
        return base.toString() + " ^ " + exponent; 
    }

    public T evaluate(Valuation<T> v) {
        InvertibleBinaryOperation<T> multiplication = field.multiplication();
        T power = multiplication.identity();
        T value = base.evaluate(v);
        for (int i = Math.abs(exponent);i > 0;i--)
            power = multiplication.op(power, value);
        return exponent < 0 ? multiplication.inverse(power) : power;
    }

    public Expression<T> differentiate(Variable<T> x) {
        T one = field.multiplication().identity();
        T fieldExponent = field.addition().identity();
        for (int i = Math.abs(exponent);i > 0;i--)
            fieldExponent = field.addition().op(fieldExponent, one);
        if (exponent < 0)
            fieldExponent = field.addition().inverse(fieldExponent);
        return Product.build(field,Constant.build(field,fieldExponent),Product.build(field,new Power<> (field,base,exponent - 1),base.differentiate(x)));
    }
    
    public static <T> Expression<T> build(Field<T> field,Expression<T> base,int exponent) {
        switch(exponent) {
        case 0 : return Constant.build(field,field.multiplication().identity());
        case 1 : return base;
        default: return new Power<>(field,base,exponent);
        }
    }
}
