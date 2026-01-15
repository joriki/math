package info.joriki.math.categories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FiniteSetCategory extends Category<Set<Integer>,MapMorphism<Integer>>{
    public FiniteSetCategory (int n) {
        objects = new ArrayList<Set<Integer>> ();
        
        for (int i = 0;i <= n;i++)
            objects.add (IntStream.range (0,i).boxed ().collect (Collectors.toSet ()));

        morphisms = new ArrayList<MapMorphism<Integer>> ();
        
        objects.forEach (source -> objects.forEach (target -> {
            int s = source.size ();
            int t = target.size ();
            if (t != 0 || s == 0) {
                int lim = (int) Math.pow (t,s);
                List<Integer> arguments = new ArrayList<> (source);
                List<Integer> values = new ArrayList<> (target);
                for (int i = 0;i < lim;i++) {
                    Map<Integer,Integer> map = new HashMap<>();
                    for (int j = 0,code = i;j < s;j++,code /= t)
                        map.put (arguments.get (j),values.get (code % t));
                    morphisms.add (new MapMorphism<> (map,target));
                }
            }
        }));
    }
    
    public MapMorphism<Integer> identity (Set<Integer> t) {
        return new MapMorphism<> (t.stream ().collect (Collectors.toMap (i -> i,i -> i)),t);
    }

    public MapMorphism<Integer> compose (MapMorphism<Integer> m1,MapMorphism<Integer> m2) {
        return m1.target ().equals (m2.source ()) ? new MapMorphism<> (m1.map.keySet ().stream ().collect (Collectors.toMap (i -> i,i -> m2.map.get (m1.map.get (i)))),m2.target ()) : null;
    }
    
    public static void main (String [] args) {
        FiniteSetCategory c = new FiniteSetCategory (2);
//        Collection<Integer> singleton = c.ofSize (1);
//        var product = c.product (singleton,singleton);

        if (false) {
            
            Set<Integer> zero = c.initialObject ();
            Set<Integer> one = c.terminalObject ();
            Set<Integer> two = c.sum (one,one).apex;
            Set<Integer> three = c.sum (one,two).apex;
            Set<Integer> four = c.sum (one,three).apex;

        Set [] sets = {zero,one,two,three};
        
        for (Set<Integer> a : sets) {
            for (Set<Integer> b : sets)
                System.out.println(a + " x " + b + " = " + c.product (a,b));
            System.out.println();
        }
            
        for (Set<Integer> a : sets) {
            System.out.println();
            for (Set<Integer> b : sets)
                System.out.println(a + " + " + b + " = " + c.sum (a,b));
        }

        System.out.println ();
        System.out.println ("------------------");
        System.out.println ();
        
        
        Collection<MapMorphism<Integer>> hom23 = c.hom (three,two);
        Collection<MapMorphism<Integer>> hom43 = c.hom (three,three);
        
        MapMorphism<Integer> m2 = choose (hom43);
        MapMorphism<Integer> m4 = choose (hom43);
        
        MapMorphism<Integer> m3 = choose (hom43);
        
        System.out.println(m2);
        System.out.println(m4);
        System.out.println();
        System.out.println(c.coequalizer (m2,m4));
        System.out.println();
        System.out.println(m3);
        System.out.println();
        System.out.println(c.coequalizer (m3,m2));
        System.out.println(c.isCoequalizer (c.coequalizer (m3,m2),m2,m4));
        System.out.println();
        }

//        System.out.println("size: " + c.morphisms.size () + " " + c.hasEqualizers () + " " + c.hasPullbacks () + " " + c.hasProducts ());

        int count = 0;
        
        for (int i = 0;;i++) {
            System.out.println ("!");
//            var s = Subcategory.randomSubcategory (c,0.1,0.0002);
            var s = TWO;
            System.out.println(s.objects.size () + " / " + s.morphisms.size());
            if (s.morphisms.size () > 150)
                continue;
            boolean hasPullbacks = s.hasPullbacks ();
            System.out.println(hasPullbacks);
            if (hasPullbacks)
                for (var morphism : s.morphisms)
                    if (s.isEpimorphism (morphism)) {
                        if (count++ == 8875)
                            System.out.println("debugging");
                        System.out.println("count: " + count);
                        boolean essentiallyInjective = s.pullbackFunctor (morphism).isEssentiallyInjective ();
                        System.out.println ("essentially injective : " + essentiallyInjective);
                        if (!essentiallyInjective) {
                            System.out.println (s.morphisms);
                            throw new Error (morphism.toString ());
                        }
                    }
        }
        
//        System.out.println(s.objects);
//        
//        for (var morphism : s.morphisms)
//            System.out.println(morphism);
        
//        for (var morphism : s.morphisms)
//            if (s.isRegularEpimorphism (morphism))
//                System.out.println ("essentially injective : " + s.pullbackFunctor (morphism).isEssentiallyInjective ());
    }
    
    static Random random = new Random (0);
    
    static MapMorphism<Integer> choose (Collection<MapMorphism<Integer>> morphisms) {
        return new ArrayList<>(morphisms).get (random.nextInt(morphisms.size ()));
    }
}
