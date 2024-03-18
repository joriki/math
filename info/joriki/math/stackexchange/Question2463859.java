package info.joriki.math.stackexchange;

public class Question2463859 {
    final static int significandBits = 52;
    final static int tableBits = 10;
    final static int fractionBits = significandBits - tableBits;
    final static double log2 = Math.log (2);

    final static double [] table = new double [(1 << tableBits) + 1];

    static {
        for (int bits = 0;bits <= 1 << tableBits;bits++)
            table [bits] = Math.log (1 + bits / (double) (1 << tableBits));
    }

    public static void main (String [] args) {
        for (int i = 0;i < 100;i++) {
            double x = Math.random () * Math.exp (200 * Math.random () - 100);
            System.out.println (Math.log (x) + " / " + fastLog (x));
        }
    }

    public static double fastLog (double x) {
        long bits = Double.doubleToLongBits (x);
        if ((bits & (1L << 63)) != 0)
            throw new IllegalArgumentException ("logarithm of negative number");
        int exponent = (int) (bits >> 52);
        int index = (int) (bits >> fractionBits) & ((1 << tableBits) - 1);
        double fraction = (bits & ((1 << fractionBits) - 1)) / (double) (1 << fractionBits);
        return (exponent - 1023) * log2 + fraction * table [index + 1] + (1 - fraction) * table [index];
    }
}
