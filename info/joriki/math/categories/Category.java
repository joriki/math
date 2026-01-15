package info.joriki.math.categories;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import info.joriki.util.Pair;

public abstract class Category<T,M extends Morphism<T>> {
    public final static AbstractCategory<Integer> ZERO = new AbstractCategory<> (List.of (),List.of ()); 
    public final static AbstractCategory<Integer> ONE  = new AbstractCategory<> (List.of (0),List.of ()); 
    public final static AbstractCategory<Integer> TWO  = new AbstractCategory<> (List.of (0,1),List.of (new Pair<Integer,Integer> (0,1))); 
    public final static AbstractCategory<Integer> PAIR = new AbstractCategory<> (List.of (0,1),List.of (new Pair<Integer,Integer> (0,1),new Pair<Integer,Integer> (0,1))); 
    public final static AbstractCategory<Integer> JOIN = new AbstractCategory<> (List.of (0,1,2),List.of (new Pair<Integer,Integer> (0,1),new Pair<Integer,Integer> (2,1)));
    
    Collection<T> objects;
    Collection<M> morphisms;
    abstract public M identity (T t);
    abstract public M compose (M m1,M m2);

    public boolean isEpimorphism (M morphism) {
        Set<M> compositions = new HashSet<> ();
        return morphisms.stream ().allMatch (m -> {
            M composition = compose (m,morphism);
            return composition == null || compositions.add (composition);
        });
    }

    public boolean isMonomorphism (M morphism) {
        Set<M> compositions = new HashSet<> ();
        return morphisms.stream ().allMatch (m -> {
            M composition = compose (morphism,m);
            return composition == null || compositions.add (composition);
        });
    }
    
    public boolean isRegularEpimorphism (M morphism) {
        return morphisms.stream ().anyMatch (m1 -> m1.target ().equals (morphism.source ()) && morphisms.stream ().anyMatch (m2 -> m2.source ().equals (m1.source ()) && m2.target ().equals (m1.target ()) && isCoequalizer (morphism,m1,m2)));
    }

    public M isomorphism (T a,T b) {
        M identity = identity (a);
        var hom = hom (b,a);
        return hom (a,b).stream ().filter (m -> hom.stream ().anyMatch (n -> compose (m,n).equals (identity))).findAny ().orElse (null);
    }
    
    public boolean areIsomorphic (T a,T b) {
        return isomorphism (a,b) != null;
    }
    
    public T terminalObject () {
        Cone<Integer,T,M> cone = limit (Functor.construct (ZERO,this,Map.of (),Map.of ()));
        return cone == null ? null : cone.apex;
    }

    public T initialObject () {
        return dual ().terminalObject ();
    }

    public Cone<Integer,T,M> product (T a,T b) {
        return limit (Functor.construct (discrete (2),this,Map.of (0,a,1,b),Map.of ()));
    }

    public Cone<Integer,T,M> sum (T a,T b) {
        var product = dual ().product (a,b);
        return product == null ? null : Cone.undual (product);
    }
    
    public boolean hasProducts () {
        return objects.stream ().allMatch (a -> objects.stream ().allMatch (b -> product (a,b) != null));
    }
    
    private Functor<Integer,AbstractMorphism<Integer>,T,M> equalizerDiagram (M a,M b) {
        if (!a.source ().equals ((b.source ())))
            throw new IllegalArgumentException();
        if (!a.target ().equals ((b.target ())))
            throw new IllegalArgumentException();
        var arrows = PAIR.morphisms.stream ().filter (m -> !m.source ().equals (m.target ())).toList ();
        return Functor.construct (PAIR,this,Map.of (0,a.source (),1,a.target ()),Map.of(arrows.get (0),a,arrows.get (1),b));
    }

    public M equalizer (M a,M b) {
        var limit = limit (equalizerDiagram (a,b));
        return limit == null ? null : limit.map (0);
    }

    public M coequalizer (M a,M b) {
        var equalizer = dual ().equalizer (dual (a),dual (b));
        return equalizer == null ? null : equalizer.m;
    }

    public boolean isEqualizer (M e,M a,M b) {
        Cone<Integer,T,M> cone = new Cone<> (e.source (),List.of (0,1),List.of (e,compose (e,a)));
        Functor<Integer,AbstractMorphism<Integer>,T,M> diagram = equalizerDiagram (a,b);
        return cone.commutes (diagram) && isLimit (diagram,cone);
    }
    
    public boolean isCoequalizer (M e,M a,M b) {
        return dual ().isEqualizer (dual (e),dual (a),dual (b));
    }

    public boolean hasEqualizers () {
        return morphisms.stream ().allMatch (a -> morphisms.stream ().filter (b -> a.source ().equals (b.source ()) && a.target ().equals (b.target ())).allMatch (b -> equalizer (a,b) != null));
    }
    
    public Cone<Integer,T,M> pullback (M a,M b) {
        if (!a.target ().equals ((b.target ())))
            throw new IllegalArgumentException();
        return limit (Functor.construct (JOIN,this,Map.of (0,a.source (),1,a.target (),2,b.source ()),Map.of(JOIN.morphism (0,1),a,JOIN.morphism (2,1),b)));
    }
    
    public Cone<Integer,T,M> pushout (M a,M b) {
        var pullback = dual ().pullback (dual (a),dual (b));
        return pullback == null ? null : Cone.undual (pullback);
    }

    public boolean hasPullbacks () {
        return morphisms.stream ().allMatch (a -> morphisms.stream ().filter (b -> a.target ().equals (b.target ())).allMatch (b -> pullback (a,b) != null));
    }
    
    public <U,N extends Morphism<U>> boolean factorsUniquely (Functor<U,N,T,M> diagram,Cone<U,T,M> cone,Cone<U,T,M> universalCone) {
        return hom (cone.apex,universalCone.apex).stream ().filter (m -> diagram.source.objects.stream ().allMatch (o -> compose (m,universalCone.map (o)).equals (cone.map (o)))).count () == 1;
    }
    
    public <U,N extends Morphism<U>> boolean isLimit (Functor<U,N,T,M> diagram,Cone<U,T,M> cone) {
        return Cone.allCones (diagram).stream ().allMatch (c -> factorsUniquely (diagram,c,cone));
    }

    public <U,N extends Morphism<U>> Cone<U,T,M> limit (Functor<U,N,T,M> diagram) {
        var cones = Cone.allCones (diagram);
        return cones.stream ().filter(c -> cones.stream ().allMatch (d -> factorsUniquely (diagram,d,c))).findAny().orElse(null);
    }
    
    public Collection<M> hom (T source,T target) {
        return Collections.filter (morphisms,m -> m.source ().equals (source) && m.target ().equals (target));
    }
    
    public Category<T,DualMorphism<T,M>> dual () {
        Category<T,DualMorphism<T,M>> dual = new Category<> () {
            public DualMorphism<T,M> identity (T t) {
                return Category.this.dual (Category.this.identity (t));
            }

            public DualMorphism<T,M> compose (DualMorphism<T,M> m1,DualMorphism<T,M> m2) {
                return Category.this.dual(Category.this.compose (m2.m,m1.m));
            }
        };
        
        dual.objects = objects;
        dual.morphisms = morphisms.stream().map (m -> dual (m)).toList ();
        
        return dual;
    }

    public DualMorphism<T,M> dual (M m) {
        return new DualMorphism<T,M>(m);
    }
    
    public static AbstractCategory<Integer> discrete (int n) {
        return new AbstractCategory<> (IntStream.range (0,n).boxed ().toList (),List.of ());
    }

    public Functor<M,SliceCategory<T,M>.SliceMorphism,M,SliceCategory<T,M>.SliceMorphism> pullbackFunctor (M m) {
        SliceCategory<T,M> sourceSlice = new SliceCategory<> (this,m.target ());
        SliceCategory<T,M> targetSlice = new SliceCategory<> (this,m.source ());
        return new Functor<> (sourceSlice,targetSlice) {
            M mapObject (M t) {
                return pullback (m,t).map (0);
            }

            SliceCategory<T,M>.SliceMorphism mapMorphism (SliceCategory<T,M>.SliceMorphism s) {
                Cone<Integer,T,M> sourcePullback = pullback (m,s.source ());
                Cone<Integer,T,M> targetPullback = pullback (m,s.target);
                M newSource = sourcePullback.map (0);
                M newTarget = targetPullback.map (0);
                return targetSlice.new SliceMorphism (newTarget,morphisms.stream ().filter (m -> compose (m,newTarget).equals (newSource) && compose (m,targetPullback.map (1)).equals (compose (sourcePullback.map (1),s.morphism))).findAny ().get ());
            }
        };
    }
}
