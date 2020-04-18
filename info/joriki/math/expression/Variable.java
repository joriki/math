package info.joriki.math.expression;

import info.joriki.math.algebra.Field;

public class Variable<T> implements Expression<T> {
    String name;
    Field<T> field;
    
    private Variable(Field<T> field,String name) {
        this.name = name;
        this.field = field;
    }
    
    public String toString() {
        return name;
    }

    public T evaluate(Valuation<T> valuation) {
        return valuation.get(this);
    }

    public Expression<T> differentiate(Variable<T> x) {
        return Constant.build(field,x == this ? field.multiplication().identity() : field.addition().identity());
    }
    
    public static <T> Variable<T> build(Field<T> field,String name) {
        return new Variable<>(field,name);
    }
}
