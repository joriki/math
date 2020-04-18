package info.joriki.math.algebra;

import java.util.Arrays;

public class AlgebraicInteger {
    public int [] x;

    public AlgebraicInteger(int ... x) {
        this.x = x;
    }

    public String toString() {
        return "AlgebraicInteger [" + Arrays.toString(x) + "]";
    }
}
