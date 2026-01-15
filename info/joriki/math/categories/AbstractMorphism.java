package info.joriki.math.categories;

public class AbstractMorphism<T> implements Morphism<T> {
    T source;
    T target;
    
    public AbstractMorphism (T source,T target) {
        this.source = source;
        this.target = target;
    }

    public T source () {
        return source;
    }

    public T target () {
        return target;
    }
    
    public String toString () {
        return source + " -> " + target;
    }
}
