package info.joriki.math.expression;

import info.joriki.math.algebra.Field;

public class Constant<T> implements Expression<T> {
    Field<T> field;
    T value;

    private Constant (Field<T> field,T value) {
        this.value = value;
        this.field = field;
    }
    
    public String toString() {
        return value.toString();
    }
    
    public boolean equals (Object o) {
        if (!(o instanceof Constant))
            return false;
        return ((Constant<T>) o).value.equals(value);
    }
    
    public T evaluate(Valuation<T> v) {
        return value;
    }

    public Expression<T> differentiate(Variable<T> x) {
        return new Constant<T> (field,field.addition().identity());
    }
    
    public static <T> Expression<T> build(Field<T> field,T value) {
        return new Constant<>(field,value);
    }
}
