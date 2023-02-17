package info.joriki.math.stackexchange;

import info.joriki.math.algebra.BigRational;

public class Question4640835 {
    final static int nrounds = 100;
    final static int nfaces = 20;
    final static BigRational weight = new BigRational (1,nfaces);
    
    final static int PLAYER = 0;
    final static int CASINO = 1;
    
    static BigRational [] [] [] values = new BigRational [2] [nrounds] [nfaces];
    static boolean [] [] [] reroll = new boolean [2] [nrounds] [nfaces];
    
    public static void main(String [] args) {
        System.out.println(value (PLAYER,0,0));

        System.out.println();
        
        for (int i = 0;i < nrounds - 1;i++) {
            System.out.print((i + 1) + " :");
            for (int j = 0;j < nfaces;j++)
                if (!reroll [PLAYER] [i] [j]) {
                    System.out.print(" " + j);
                    break;
                }
            for (int j = nfaces - 1;j >= 0;j--)
                if (!reroll [CASINO] [i] [j]) {
                    System.out.print(" " + (j + 2));
                    break;
                }
            System.out.println();
        }
    }
    
    static BigRational value (int who,int round,int face) {
        if (values [who] [round] [face] == null)
            values [who] [round] [face] = computeValue(who,round,face);
        return values [who] [round] [face];
    }
    
    static BigRational computeValue (int who,int round,int face) {
        BigRational money = new BigRational (face + 1);
        
        if (who == PLAYER && round == nrounds - 1)
            return money;

        BigRational onReroll = average (PLAYER,round + 1);
        BigRational onTake;
        
        switch (who) {
        case PLAYER:
            onTake = BigRational.sum(money,value (CASINO,round,face));
            return (reroll [PLAYER] [round] [face] = onReroll.compareTo(onTake) > 0) ? onReroll : onTake;
        case CASINO:
            onTake = value(PLAYER,round + 1,face);
            return (reroll [CASINO] [round] [face] = onReroll.compareTo(onTake) < 0) ? onReroll : onTake;
        }
        throw new InternalError();
    }
    
    static BigRational average (int who,int round) {
        BigRational sum = BigRational.ZERO;
        for (int i = 0;i < nfaces;i++)
            sum = BigRational.sum(sum,value (who,round,i));
        return BigRational.product(sum,weight);
    }
}
