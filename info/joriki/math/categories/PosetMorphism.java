package info.joriki.math.categories;

class PosetMorphism<T> extends AbstractMorphism<T> {
    public PosetMorphism (T source,T target) {
        super (source,target);
    }

    public boolean equals (Object o) {
        PosetMorphism<T> m = (PosetMorphism<T>) o;
        return m.source.equals (source) && m.target.equals (target);
    }
    
    public int hashCode () {
        return 31 * source.hashCode () ^ target.hashCode ();
    }
}