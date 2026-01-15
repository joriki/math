package info.joriki.math.categories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class Cone<T,U,N extends Morphism<U>> {
    U apex;
    Map<T,N> map;
    
    public Cone (U apex,Map<T,N> map) {
        this.apex = apex;
        this.map = map;
    }

    public Cone (U apex,List<T> objects,List<N> morphisms) {
        this (apex,new HashMap<> ());
        
        if (objects.size () != morphisms.size ())
            throw new IllegalArgumentException ();

        for (int i = 0;i < objects.size ();i++)
            map.put (objects.get (i),morphisms.get (i));
    }

    public String toString () {
        return apex + " : " + map;
    }
    
    public N map (T t) {
        return map.get (t);
    }

    
    static <T,M extends Morphism<T>,U,N extends Morphism<U>> Collection<Cone<T,U,N>> allCones(Functor<T,M,U,N> diagram) {
        var objects = new ArrayList<> (diagram.source.objects);
        Collection<Cone<T,U,N>> cones = new ArrayList<>();
        for (U apex : diagram.target.objects)
            recurse (diagram,apex,cones,objects,new Stack<> (),0);
        return cones;
    }
    
    static <T,M extends Morphism<T>,U,N extends Morphism<U>> Collection<Cone<T,U,N>> allCones(Functor<T,M,U,N> diagram,U apex) {
        Collection<Cone<T,U,N>> cones = new ArrayList<>();
        recurse (diagram,apex,cones,new ArrayList<> (diagram.source.objects),new Stack<> (),0);
        return cones;
    }
    
    static <T,U,N extends Morphism<U>> Cone<T,U,N> undual (Cone<T,U,DualMorphism<U,N>> cone) {
        return new Cone<> (cone.apex,cone.map.entrySet ().stream ().collect (Collectors.toMap (e -> e.getKey (),e -> e.getValue ().m)));
    }

    private static <T,M extends Morphism<T>,U,N extends Morphism<U>> void recurse(Functor<T,M,U,N> diagram,U apex,Collection<Cone<T,U,N>> cones,List<T> objects,Stack<N> morphisms,int index) {
        if (index == objects.size ()) {
            var cone = new Cone<> (apex,objects,morphisms);
            if (cone.commutes (diagram))
                cones.add (cone);
        }
        else
            for (N n : diagram.target.hom(apex,diagram.mapObject (objects.get (index)))) {
                morphisms.push (n);
                recurse (diagram,apex,cones,objects,morphisms,index + 1);
                morphisms.pop ();
            }
    }
    
    public <M extends Morphism<T>> boolean commutes (Functor<T,M,U,N> diagram) {
        return diagram.source.morphisms.stream ().allMatch(m -> diagram.target.compose (map (m.source ()),diagram.mapMorphism (m)).equals(map (m.target ())));
    }
}
