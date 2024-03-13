package info.joriki.util;

import java.util.Arrays;

public class IntegerArray {
    public int [] a;

    public IntegerArray(int [] a) {
        this.a = a;
    }

    public boolean equals (Object o) {
        return Arrays.equals(((IntegerArray) o).a,a);
    }

    public int hashCode () {
        return Arrays.hashCode(a);
    }

    public String toString () {
        return Arrays.toString(a);
    }
}
