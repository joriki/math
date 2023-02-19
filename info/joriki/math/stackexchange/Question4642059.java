package info.joriki.math.stackexchange;

import java.util.HashSet;
import java.util.Set;

public class Question4642059 {
    static int n;
    static int count;
    static int [] queens;
    static Set<String> solutions = new HashSet<>();
    
    public static void main(String [] args) {
        for (n = 2;n < 17;n++) {
            count = 0;
            queens = new int [n];
            solutions.clear();
            recurse (0);
            int reduced = solutions.size();
            if ((reduced & 1) != 0)
                throw new Error();
            System.out.println(n + " : " + reduced / 2 + " / " + count);
        }
    }
    
    static void recurse (int i) {
        if (i == n) {
            String canonical = "";
            for (int rotate = 0;rotate < 4;rotate++) {
                StringBuilder builder = new StringBuilder();
                for (int x = 0;x < n;x++)
                    for (int y = 0;y < n;y++) {
                        int k = x;
                        int l = y;
                        for (int m = 0;m < rotate;m++) {
                            int t = n - l - 1;
                            l = k;
                            k = t;
                        }
                        builder.append(queens [k] == l ? '!' : '?');
                    }
                String candidate = builder.toString();
                if (candidate.compareTo(canonical) > 0)
                    canonical = candidate;
            }
            solutions.add(canonical);
            count++;
        }
        else
            outer:
            for (queens [i] = 0;queens [i] < n;queens [i]++) {
                for (int j = 0;j < i;j++) {
                    int dx = i - j;
                    int dy = queens [j] - queens [i];
                    if (dy == 0 || dy == dx || dy == -dx)
                        continue outer;
                }

                for (int j = 1;j < i;j++)
                    for (int k = 0;k < j;k++)
                        if ((i - k) * (queens [i] - queens [j]) == (i - j) * (queens [i] - queens [k]))
                            continue outer;

                recurse (i + 1);
            }
    }
}
