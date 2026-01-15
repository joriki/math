package info.joriki.math.categories;

import java.util.Collection;
import java.util.function.Predicate;

public class Collections {
    private Collections () {}
    
    public static <T> Collection<T> filter (Collection<T> collection,Predicate <T> include) {
        return collection.stream ().filter (include).toList ();
    }
}
