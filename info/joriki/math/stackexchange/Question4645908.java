package info.joriki.math.stackexchange;

public class Question4645908 {
    static int n;
    static int [] counts;
    
    public static void main(String [] args) {
        for (n = 2;;n++) {
            counts = new int [] {0,0,n};
            System.out.println(n + " : " + recurse (0));
        }
    }
    
    static long recurse (int i) {
        if (i++ == n)
            return 1;
        
        long sum = 0;
        
        for (int j = 1;j <= 2;j++)
            if (counts [j] >= 2) {
                counts [j] -= 2;
                counts [j - 1] += 2;
                sum += (((counts [j] + 1) * (counts [j] + 2)) / 2) * recurse (i);
                counts [j - 1] -= 2;
                counts [j] += 2;
            }
        
        if (counts [1] >= 1 && counts [2] >= 1) {
            counts [2]--;
            counts [0]++;
            sum += (counts [1]) * (counts [2] + 1) * recurse (i);
            counts [0]--;
            counts [2]++;
        }
        
        return sum;
    }
}
