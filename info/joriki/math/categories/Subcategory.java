package info.joriki.math.categories;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Subcategory<T,M extends Morphism<T>> extends Category<T,M> {
    Category<T,M> category;

    Subcategory (Category<T,M> category,Collection<T> objects,Collection<M> morphisms) {
        this.category = category;
        this.objects = objects;
        this.morphisms = morphisms;
    }

    public M identity (T t) {
        return category.identity (t);
    }

    public M compose (M m1,M m2) {
        return category.compose (m1,m2);
    }
    
    static Random random = new Random (354867);
    public static <T,M extends Morphism<T>> Subcategory<T,M> randomSubcategory (Category<T,M> category,double objectProportion,double morphismProportion) {
        List<T> objects = category.objects.stream ().filter (o -> random.nextDouble () < objectProportion).toList ();
        Set<M> morphisms = category.morphisms.stream ().filter (m -> objects.contains (m.source ()) && objects.contains (m.target ()) && (m.equals (category.identity (m.source ())) || random.nextDouble () < morphismProportion)).collect (Collectors.toSet ());
        while (morphisms.addAll (morphisms.stream ().flatMap (m1 -> morphisms.stream ().map (m2 -> category.compose (m1,m2)).filter (m -> m != null)).toList ()))
            ;
        return new Subcategory<> (category,objects,morphisms);
    }
}
