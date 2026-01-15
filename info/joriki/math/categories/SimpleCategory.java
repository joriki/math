package info.joriki.math.categories;

import java.util.ArrayList;
import java.util.Collection;

import info.joriki.util.Pair;

public abstract class SimpleCategory<T,M extends Morphism<T>> extends Category<T,M> {
    public SimpleCategory (Collection<T> objects,Collection<Pair<T,T>> morphisms) {
        this.objects = objects;
        this.morphisms = new ArrayList<> (morphisms.stream ().map (p -> createMorphism (p.s,p.t)).toList ());
        objects.forEach (o -> this.morphisms.add (createMorphism (o,o)));
    }
    
    public abstract M morphism (T source,T target);
    protected abstract M createMorphism (T source,T target);
}
