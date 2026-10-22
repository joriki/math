package info.joriki.math.stackexchange;

public class Question964813 {
    static final int maxHeapSize = 20;
    static final int maxTotal = 2 * maxHeapSize;
    static final int[][] nimber = new int[maxTotal + 1][maxTotal + 1];
    static final String periodicColor = "red";
    static final String sumColor = "green";

    public static void main(String[] args) {
        computeNimbers();
        printNimbers();
        System.out.println();
        printMathJax();
    }

    static void computeNimbers() {
        for (int total = 1; total <= maxTotal; total++)
            for (int first = 0; first <= total; first++) {
                int second = total - first;
                boolean[] reachable = new boolean[total * (total + 1) + 1];
                addMoves(reachable, first, second, false);
                addMoves(reachable, second, first, true);
                int mex = 0;
                while (reachable[mex])
                    mex++;
                nimber[first][second] = mex;
            }
    }

    static void addMoves(boolean[] reachable, int source, int target, boolean swapped) {
        for (int removed = 1; removed <= source; removed++)
            for (int moved = 0; moved <= (target == 0 ? 0 : source - removed); moved++) {
                int newSource = source - removed - moved;
                int newTarget = target + moved;
                reachable[swapped ? nimber[newTarget][newSource] : nimber[newSource][newTarget]] = true;
            }
    }

    static void printNimbers() {
        System.out.print("     ");
        for (int second = 0; second <= maxHeapSize; second++)
            System.out.printf("%3d", second);
        System.out.println();
        for (int first = 0; first <= maxHeapSize; first++) {
            System.out.printf("%3d: ", first);
            for (int second = 0; second <= maxHeapSize; second++)
                System.out.printf("%3d", nimber[first][second]);
            System.out.println();
        }
    }

    static void printMathJax() {
        System.out.printf("\\def\\x#1{\\color{%s}{#1}}%n", periodicColor);
        System.out.printf("\\def\\y#1{\\color{%s}{#1}}%n", sumColor);
        System.out.print("\\begin{array}{c|");
        for (int second = 0; second <= maxHeapSize; second++)
            System.out.print('r');
        System.out.println("}");
        System.out.print("&0");
        for (int second = 1; second <= maxHeapSize; second++)
            System.out.print("&" + second);
        System.out.println("\\\\");
        System.out.println("\\hline");
        for (int first = 0; first <= maxHeapSize; first++) {
            System.out.print(first);
            for (int second = 0; second <= maxHeapSize; second++)
                System.out.print("&" + mathJaxNimber(first, second));
            System.out.println("\\\\");
        }
        System.out.println("\\end{array}");
    }

    static String mathJaxNimber(int first, int second) {
        if (first == second)
            return Integer.toString(nimber[first][second]);
        int smaller = Math.min(first, second);
        int difference = Math.abs(first - second);
        int triangular = difference * (difference + 1) / 2;
        String command = smaller <= triangular ? "y" : "x";
        return "\\" + command + "{" + nimber[first][second] + "}";
    }
}
