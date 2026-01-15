package info.joriki.math.categories;

public class SliceCategory<T,M extends Morphism<T>> extends Category<M,SliceCategory<T,M>.SliceMorphism>{
    Category<T,M> category;

    public class SliceMorphism implements Morphism<M>{
        M target;
        M morphism;

        SliceMorphism (M target,M morphism) {
            this.target = target;
            this.morphism = morphism;
        }

        public M source () {
            return category.compose (morphism,target);
        }

        public M target () {
            return target;
        }
        
        public boolean equals (Object o) {
            SliceMorphism m = (SliceMorphism) o;
            return m.target.equals (target) && m.morphism.equals (morphism);
        }
        
        public int hashCode () {
            return target.hashCode () * 31 ^ morphism.hashCode ();
        }
    }
    
    public SliceCategory (Category<T,M> category,T object) {
        this.category = category;
        
        objects = Collections.filter(category.morphisms,m -> m.target ().equals (object));
        morphisms = category.morphisms.stream ().flatMap (m -> objects.stream ().
                filter (o -> m.target ().equals (o.source ())).
                map (o -> new SliceMorphism (o,m))).toList ();
    }

    public SliceMorphism identity (M t) {
        return new SliceMorphism (t,category.identity (t.source ()));
    }

    public SliceMorphism compose (SliceMorphism m1,SliceMorphism m2) {
        M composition = category.compose (m1.morphism,m2.morphism);
        return composition == null ? null : new SliceMorphism (m2.target,composition);
    }
}
