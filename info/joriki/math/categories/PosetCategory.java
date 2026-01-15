package info.joriki.math.categories;

import java.util.Collection;

import info.joriki.util.Pair;

public class PosetCategory<T> extends SimpleCategory<T,PosetMorphism<T>> {
    public PosetCategory (Collection<T> objects,Collection<Pair<T,T>> morphisms) {
        super (objects,morphisms);
    }

    protected PosetMorphism<T> createMorphism (T source,T target) {
        return new PosetMorphism<> (source,target);
    }
    
    public PosetMorphism<T> morphism (T source,T target) {
        return createMorphism (source,target);
    }
    
    public PosetMorphism<T> identity (T t) {
        return new PosetMorphism<> (t,t);
    }

    public PosetMorphism<T> compose (PosetMorphism<T> m1,PosetMorphism<T> m2) {
        return new PosetMorphism<> (m1.source (),m2.target ());
    }
}
