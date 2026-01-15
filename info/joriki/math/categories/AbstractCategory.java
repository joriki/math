package info.joriki.math.categories;

import java.util.Collection;

import info.joriki.util.Pair;

// This is for categories with only trivial composition
public class AbstractCategory<T> extends SimpleCategory<T,AbstractMorphism<T>> {
    public AbstractCategory (Collection<T> objects,Collection<Pair<T,T>> morphisms) {
        super (objects,morphisms);
    }

    protected AbstractMorphism<T> createMorphism (T source,T target) {
        return new AbstractMorphism<> (source,target);
    }
    
    public AbstractMorphism<T> morphism (T source,T target) {
        return morphisms.stream ().filter (m -> m.source ().equals (source) && m.target ().equals (target)).findAny ().orElse (null);
    }

    public AbstractMorphism<T> identity (T t) {
        return morphism (t,t);
    }

    public AbstractMorphism <T> compose (AbstractMorphism<T> m1,AbstractMorphism<T> m2) {
        if (m1.source () == m1.target ())
            return m1;
        if (m2.source () == m2.target ())
            return m2;
        throw new Error ("non-trivial composition");
    }
}
