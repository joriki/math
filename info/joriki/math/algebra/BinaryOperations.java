package info.joriki.math.algebra;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import info.joriki.math.combinatorics.Permutations;

public class BinaryOperations {
    private BinaryOperations () {}
    
    public static <T> boolean isAssociative (BinaryOperation<T> op,Collection<T> elements) {
        return elements.stream ().allMatch (a -> elements.stream ().allMatch (b -> elements.stream ().allMatch (c -> op.op (a,op.op (b,c)).equals (op.op (op.op (a,b),c)))));
    }

    public static <T> boolean isCommutative (BinaryOperation<T> op,Collection<T> elements) {
        return elements.stream ().allMatch(a -> elements.stream ().allMatch (b -> op.op (a,b).equals (op.op (b,a))));
    }
    
    public static <T> boolean hasLeftIdentity (BinaryOperation<T> op,Collection<T> elements) {
        return elements.stream ().anyMatch (e -> elements.stream ().allMatch (a -> op.op (e,a).equals (a)));
    }

    public static <T> boolean hasRightIdentity (BinaryOperation<T> op,Collection<T> elements) {
        return elements.stream ().anyMatch (e -> elements.stream ().allMatch(a -> op.op (a,e).equals (a)));
    }

    public static <T> boolean hasIdentity (BinaryOperation<T> op,Collection<T> elements) {
        return identity (op,elements) != null; 
    }

    public static <T> T identity (BinaryOperation<T> op,Collection<T> elements) {
        return elements.stream ().filter (e -> elements.stream ().allMatch (a -> op.op (a,e).equals (a) && op.op (e,a).equals (a))).findAny ().orElse (null);
    }
    
    public static <T> boolean hasLeftInverses (BinaryOperation<T> op,Collection<T> elements) {
        T identity = identity (op,elements);
        return identity != null && elements.stream ().allMatch (a -> elements.stream ().anyMatch (b -> op.op (b,a).equals(identity)));  
    }

    public static <T> boolean hasRightInverses (BinaryOperation<T> op,Collection<T> elements) {
        T identity = identity (op,elements);
        return identity != null && elements.stream ().allMatch (a -> elements.stream ().anyMatch (b -> op.op (a,b).equals(identity)));  
    }

    public static <T> boolean hasInverses (BinaryOperation<T> op,Collection<T> elements) {
        return hasLeftInverses (op,elements) && hasRightInverses (op,elements);
    }
    
    public static <T> boolean hasUniqueLeftInverses (BinaryOperation<T> op,Collection<T> elements) {
        T identity = identity (op,elements);
        return identity != null && elements.stream ().allMatch (a -> elements.stream ().filter (b -> op.op (b,a).equals (identity)).count () == 1);  
    }
    
    public static <T> boolean hasUniqueRightInverses (BinaryOperation<T> op,Collection<T> elements) {
        T identity = identity (op,elements);
        return identity != null && elements.stream ().allMatch (a -> elements.stream ().filter (b -> op.op (a,b).equals (identity)).count () == 1);  
    }

    public static <T> boolean hasUniqueInverses (BinaryOperation<T> op,Collection<T> elements) {
        return hasUniqueLeftInverses (op,elements) && hasUniqueRightInverses (op,elements);
    }
    
    public static <T> boolean isLeftCancellative (BinaryOperation<T> op,Collection<T> elements) {
        return elements.stream ().allMatch (a -> {
            Set<T> products = new HashSet<> ();
            return elements.stream().allMatch (b -> products.add (op.op (a,b)));
        });
    }
    
    public static <T> boolean isRightCancellative (BinaryOperation<T> op,Collection<T> elements) {
        return elements.stream ().allMatch (a -> {
            Set<T> products = new HashSet<> ();
            return elements.stream ().allMatch (b -> products.add (op.op (b,a)));
        });
    }

    public static <T> boolean isCancellative (BinaryOperation<T> op,Collection<T> elements) {
        return isLeftCancellative (op,elements) && isRightCancellative (op,elements);
    }
    
    // stream of all binary operations on n elements
    public static Stream<BinaryOperation<Integer>> allOperations (int n) {
        return LongStream.range (0,(long) Math.pow (n,n * n)).mapToObj (code -> new BinaryOperation<Integer> () {
            public Integer op (Integer t1,Integer t2) {
                return ((int) (code / (long) Math.pow (n,t1 * n + t2))) % n;
            }
        });
    }
    
    // stream of all commutative binary operations on n elements
    public static Stream<BinaryOperation<Integer>> allCommutativeOperations (int n) {
        return LongStream.range (0,(long) Math.pow (n,(n * (n + 1)) / 2)).mapToObj (code -> new BinaryOperation<Integer> () {
            public Integer op (Integer t1,Integer t2) {
                if (t1 > t2) {
                    Integer t = t1;
                    t1 = t2;
                    t2 = t;
                }
                return ((int) (code / (long) Math.pow(n,(t2 * (t2 + 1)) / 2 + t1))) % n;
            }
        });
    }

    // stream of all commutative binary operations with identity on n elements
    public static Stream<BinaryOperation<Integer>> allCommutativeOperationsWithIdentity (int n) {
        return LongStream.range (0,(long) Math.pow (n,(n * (n - 1)) / 2)).mapToObj (code -> new BinaryOperation<Integer> () {
            public Integer op (Integer t1,Integer t2) {
                if (t1 == 0)
                    return t2;
                if (t2 == 0)
                    return t1;
                if (t1 > t2) {
                    Integer t = t1;
                    t1 = t2;
                    t2 = t;
                }
                return ((int) (code / (long) Math.pow (n,(t2 * (t2 - 1)) / 2 + t1 - 1))) % n;
            }
        });
    }

    // stream of all left-cancellative binary operations with identity on n elements
    public static Stream<BinaryOperation<Integer>> allLeftCancellativeOperationsWithIdentity (int n) {
        int [] [] permutations = Permutations.getPermutations (n - 1);
        return LongStream.range (0,(long) Math.pow (permutations.length,n - 1)).mapToObj (code -> new BinaryOperation<Integer> () {
            public Integer op (Integer t1,Integer t2) {
                if (t1 == 0)
                    return t2;
                if (t2 == 0)
                    return t1;
                int result = permutations [(int) ((code / (long) Math.pow (permutations.length,t1 - 1)) % permutations.length)] [t2 - 1];
                if (result >= t1)
                    result++;
                return result;
            }
        });
    }

    final static String format = "%3s";
    final static String gap = " ";
    
    public static <T> String toOperationTable (BinaryOperation<T> op,Collection<T> elements) {
        StringBuilder builder = new StringBuilder ();
        builder.append (String.format (format,"")).append (gap);
        elements.stream ().forEach (e -> builder.append (' ').append (String.format (format,e.toString ())));
        builder.append ('\n');
        for (T row : elements) {
            builder.append (String.format (format,row)).append(gap);
            for (T column : elements)
                builder.append (' ').append (String.format (format,op.op (row,column)));
            builder.append ('\n');
        }
        builder.append ('\n');
        return builder.toString ();
    }
}
