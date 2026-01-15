package info.joriki.math.categories;

import java.util.Map;
import java.util.Set;

public class MapMorphism<T> extends AbstractMorphism<Set<T>> {
    Map<T,T> map;
    
    public MapMorphism (Map<T,T> map,Set<T> target) {
        super (map.keySet (),target);
        
        this.map = map;
    }
    
    public int hashCode () {
        return map.hashCode () * 31 ^ target.hashCode ();
    }
    
    public boolean equals (Object o) {
        MapMorphism<T> m = (MapMorphism) o;
        return m.map.equals (map) && m.target.equals (target);
    }
    
    public String toString () {
        return map.toString () + " [" + target + "]";
    }
}
