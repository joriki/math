package info.joriki.math.stackexchange;

import info.joriki.math.algebra.BigRational;

public class Question4640291 {
    final static boolean rerollOnTake = true;
    
    final static int nrounds = 100;
    final static int nfaces = 20;
    final static BigRational weight = new BigRational (1,nfaces);
    
    static BigRational [] [] values = new BigRational [nrounds + 1] [nfaces];
    static boolean [] [] reroll = new boolean [nrounds + 1] [nfaces];
    
    public static void main(String [] args) {
        System.out.println(value (0,0));

        System.out.println();
        
        for (int i = 1;i < nrounds - 1;i++)
            for (int j = 0;j < nfaces;j++)
                if (!reroll [i] [j]) {
                    System.out.println((i + 1) + " : " + j);
                    break;
                }
    }
    
    static BigRational value (int round,int face) {
        if (values [round] [face] == null)
            values [round] [face] = computeValue(round,face);
        return values [round] [face];
    }
    
    static BigRational computeValue (int round,int face) {
        BigRational money = new BigRational (face + 1);
        if (round == nrounds)
            return BigRational.ZERO;
        if (round == nrounds - 1)
            return money;

        BigRational onReroll = average (round + 1);
        BigRational onTake = BigRational.sum(money,rerollOnTake ? average (round + 2) : value (round + 1,face));
        return (reroll [round] [face] = onReroll.compareTo(onTake) > 0) ? onReroll : onTake;
    }
    
    static BigRational average (int round) {
        BigRational sum = BigRational.ZERO;
        for (int i = 0;i < nfaces;i++)
            sum = BigRational.sum(sum,value (round,i));
        return BigRational.product(sum,weight);
    }
}
