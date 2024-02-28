package info.joriki.math.stackexchange;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import info.joriki.math.algebra.BinaryOperation;
import info.joriki.math.algebra.BinaryOperations;
import info.joriki.math.combinatorics.Permutations;

public class Question4871463 {
    final static int n = 6;
    
    public static void main (String [] args) {
        List<Integer> elements = IntStream.range (0,n).boxed ().toList ();
        
        Set<BinaryOperation<Integer>> operations = new HashSet<> ();
        Set<Long> codes = new HashSet<> ();
        
        int [] [] permutations = Permutations.getPermutations (n - 1);
        
        BinaryOperations.allLeftCancellativeOperationsWithIdentity (n).filter (op ->
          BinaryOperations.isCommutative (op,elements) && 
          BinaryOperations.hasUniqueInverses (op,elements) &&
          BinaryOperations.isRightCancellative (op,elements) &&
          !BinaryOperations.isAssociative (op,elements)
        ).forEach (op -> {
            if (codes.add (toLong (op,elements))) {
                for (int [] permutation : permutations)
                    codes.add (toLong (permute (op,permutation),elements));
                operations.add (op);
            }
        });
        
        System.out.println ();
        System.out.println ("operations: " + codes.size());
        System.out.println ("equivalence classes: " + operations.size());
        System.out.println ();
        
        for (var operation : operations)
            System.out.println (BinaryOperations.toOperationTable (operation,elements));
    }

    public static long toLong (BinaryOperation<Integer> op,Collection<Integer> elements) {
        long code = 0;
        for (Integer row : elements)
            for (Integer column : elements) {
                code *= elements.size ();
                code += op.op (row,column);
            }
        return code;
    }

    public static BinaryOperation<Integer> permute (BinaryOperation<Integer> op,int [] permutation) {
        return new BinaryOperation<Integer> () {
            public Integer op (Integer t1,Integer t2) {
                if (t1 == 0)
                    return t2;
                if (t2 == 0)
                    return t1;
                Integer result = op.op (permutation [t1 - 1] + 1,permutation [t2 - 1] + 1);
                if (result == 0)
                    return result;
                for (int i = 0;i < permutation.length;i++)
                    if (permutation [i] == result - 1)
                        return i + 1;
                throw new InternalError ();
            }
        };
    }
    

}
