package info.joriki.math.categories;

import java.util.HashMap;
import java.util.Map;

public abstract class Functor<T,M extends Morphism<T>,U,N extends Morphism<U>> {
    Category<T,M> source;
    Category<U,N> target;
    
    public Functor(Category<T,M> source,Category<U,N> target) {
        this.source = source;
        this.target = target;
    }

    public static <T,M extends Morphism<T>,U,N extends Morphism<U>> Functor<T,M,U,N> construct (Category<T,M> source,Category<U,N> target,Map<T,U> objectMap,Map<M,N> morphismMap) {
        var morphismMapWithIdentities = new HashMap<> (morphismMap);
        
        source.objects.stream ().forEach (o -> morphismMapWithIdentities.put (source.identity (o),target.identity (objectMap.get (o))));
        
        return new Functor<T,M,U,N> (source,target) {
            U mapObject (T t) {
                return objectMap.get (t);
            }

            N mapMorphism (M m) {
                return morphismMapWithIdentities.get (m);
            }
            
            public String toString () {
                return "(objects: " + objectMap + ", morphisms: " + morphismMapWithIdentities + ")";
            }
        };
    }
    
    abstract U mapObject (T t);
    abstract N mapMorphism (M m);
    
    public boolean isEssentiallyInjective () {
        return source.objects.stream ().allMatch (a -> source.objects.stream ().allMatch (b -> source.areIsomorphic (a,b) || !target.areIsomorphic (mapObject (a),mapObject (b))));
    }
}
