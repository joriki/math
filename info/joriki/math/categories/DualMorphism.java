package info.joriki.math.categories;

class DualMorphism<T,M extends Morphism<T>> implements Morphism<T> {
    M m;
    
    DualMorphism (M m) {
        this.m = m;
    }

    public T source () {
        return m.target ();
    }

    public T target () {
        return m.source ();
    }
    
    public boolean equals (Object o) {
        return ((DualMorphism<T,M>) o).m.equals (m);
    }
    
    public int hashCode () {
        return m.hashCode ();
    }
};
