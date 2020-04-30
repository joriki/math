package info.joriki.math.stackexchange;

public class Question3652336 {
    public static void main(String [] args) {
        outer:
        for (int n = 1;;n++) {
            int [] counts = new int [10];
            for (int j = 1;j <= n;j++)
                for (char c : String.valueOf(j).toCharArray())
                    counts [c - '0']++;
            for (int j = 1;j < 10;j++)
                for (int i = 0;i < j;i++)
                    if (counts [i] == counts [j])
                        continue outer;
            System.out.println(n);
            break;
        }
    }
}
