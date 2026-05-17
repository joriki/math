package info.joriki.math.stackexchange;

import java.util.Arrays;
import java.util.Locale;

/*
 * This program solves small instances of the territorial expansion game described at
 * https://math.stackexchange.com/questions/5136962
 *
 * It searches a modified game that is more favourable to B than the original game:
 *
 *   - A may not play in a cell weakly southeast of any cell reached by B.
 *   - B's exact cells behind its Pareto frontier are not stored. B may move as
 *     if only the frontier and the number of hidden moves behind it mattered.
 *
 * A win in this modified game is therefore a proof of an A win in the original
 * game. The converse is not asserted.
 *
 * Coordinates are zero-based. A starts in the northwest corner (0,0), B starts
 * in the southeast corner (rows-1, cols-1), and A moves first. A cell is
 * encoded as row * cols + col. Only odd dimensions are accepted here; if one
 * dimension is even, B has a mirroring strategy.
 *
 * The implementation uses only primitive arrays in the transposition table;
 * there is deliberately no Java object per stored position.
 *
 * For the largest run, give the JVM a large heap, e.g.
 *
 *   java -Xmx90g Question5136962 9
 */
public final class Question5136962 {
    /*
     * The largest supported board is 9 x 9. Its packed transposition-table key
     * uses exactly 128 bits:
     *
     *   81 bits for A's cells
     *   9 * 5 bits for B's row thresholds
     *   1 turn bit
     *   1 value bit
     */
    private static final int MAX_CELLS = 81;
    private static final int MAX_ROWS = 9;
    private static final int THRESHOLD_BITS = 5;

    /*
     * Java arrays are indexed by signed int, so the 9 x 9 run cannot store its
     * 2^32 hash slots in a single int[]. The table is segmented into fairly
     * large primitive arrays to avoid per-position objects and to keep GC/array
     * overhead moderate.
     */
    private static final int SLOT_SEG_BITS = 24;
    private static final int SLOT_SEG_SIZE = 1 << SLOT_SEG_BITS;
    private static final int SLOT_SEG_MASK = SLOT_SEG_SIZE - 1;

    private static final int ENTRY_SEG_BITS = 23;
    private static final int ENTRY_SEG_SIZE = 1 << ENTRY_SEG_BITS;
    private static final int ENTRY_SEG_MASK = ENTRY_SEG_SIZE - 1;

    private int rows;
    private int cols;
    private int cells;
    private int turnBit;
    private int valueBit;
    private boolean centerRush;

    private long fullLo;
    private long fullHi;
    private final long[] neighborLo = new long[MAX_CELLS];
    private final long[] neighborHi = new long[MAX_CELLS];

    /*
     * Segmented transposition table.
     *
     * The slot table may grow to 2^32 int slots for 9 x 9. Java arrays cannot
     * have that length, so both slots and dense entries are split into fixed
     * size segments. Slots store entry index + 1; zero means empty.
     */
    private int[][] slots;
    private long[][] entryLo;
    private long[][] entryHi;
    private long slotCapacity;
    private long entryCapacity;
    private int tableSize;

    private long calls;
    private long hits;
    private long progressInterval;
    private long nextProgress;
    private long progressLineInterval;
    private long nextProgressLine;
    private long progressStackInterval;
    private long nextProgressStack;
    private int progressDepth;
    private long progressPrefixTotal;
    private long progressPrefixCompleted;
    private long startNanos;

    private final int[] progressPath = new int[MAX_CELLS];
    private final int[] progressChildIndex = new int[MAX_CELLS];
    private final int[] progressChildCount = new int[MAX_CELLS];

    /*
     * Move arrays are indexed by depth * MAX_CELLS + localIndex. Reusing them
     * avoids allocating a Move object or array at every recursive call.
     *
     * Since the two starting cells are already occupied, a legal line has at most
     * cells - 2 plies. The array is sized for the largest supported board.
     */
    private static final int MAX_PLIES = MAX_CELLS - 2;
    private static final int MOVE_ARRAY_SIZE = MAX_PLIES * MAX_CELLS;

    private final int[] moveCell = new int[MOVE_ARRAY_SIZE];
    private final boolean[] moveFiller = new boolean[MOVE_ARRAY_SIZE];
    private final int[] scoreCenter = new int[MOVE_ARRAY_SIZE];
    private final int[] scoreTrap = new int[MOVE_ARRAY_SIZE];
    private final int[] scoreOppFrontier = new int[MOVE_ARRAY_SIZE];
    private final int[] scoreOwnFrontier = new int[MOVE_ARRAY_SIZE];
    private final int[] scoreCell = new int[MOVE_ARRAY_SIZE];

    private boolean tableValue;

    /*
     * Cell sets are represented as two long words. The high word is used only
     * for cells 64 through 80. Keeping this representation explicit avoids
     * allocating BitSet objects in the search.
     */
    private static long bitLo(int cell) {
        return cell < 64 ? 1L << cell : 0L;
    }

    private static long bitHi(int cell) {
        return cell < 64 ? 0L : 1L << (cell - 64);
    }

    private static boolean isEmpty(long lo, long hi) {
        return lo == 0L && hi == 0L;
    }

    private static boolean contains(long lo, long hi, int cell) {
        return cell < 64
            ? (lo & (1L << cell)) != 0L
            : (hi & (1L << (cell - 64))) != 0L;
    }

    private static int popcount(long lo, long hi) {
        return Long.bitCount(lo) + Long.bitCount(hi);
    }

    private static int first(long lo, long hi) {
        return lo != 0L ? Long.numberOfTrailingZeros(lo) : 64 + Long.numberOfTrailingZeros(hi);
    }

    private static long splitmix64(long x) {
        x += 0x9e3779b97f4a7c15L;
        x = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
        x = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
        return x ^ (x >>> 31);
    }

    private long setPackedBitLo(long lo, int bit) {
        return bit < 64 ? lo | (1L << bit) : lo;
    }

    private long setPackedBitHi(long hi, int bit) {
        return bit < 64 ? hi : hi | (1L << (bit - 64));
    }

    private long clearPackedBitLo(long lo, int bit) {
        return bit < 64 ? lo & ~(1L << bit) : lo;
    }

    private long clearPackedBitHi(long hi, int bit) {
        return bit < 64 ? hi : hi & ~(1L << (bit - 64));
    }

    private boolean packedBit(long lo, long hi, int bit) {
        return bit < 64 ? (lo & (1L << bit)) != 0L : (hi & (1L << (bit - 64))) != 0L;
    }

    private long keyLoWithoutValue(long lo) {
        return clearPackedBitLo(lo, valueBit);
    }

    private long keyHiWithoutValue(long hi) {
        return clearPackedBitHi(hi, valueBit);
    }

    private long packedHash(long lo, long hi) {
        lo = keyLoWithoutValue(lo);
        hi = keyHiWithoutValue(hi);
        return splitmix64(lo) ^ splitmix64(hi + 0x9e3779b97f4a7c15L);
    }

    /*
     * B's Pareto frontier is stored as one threshold per row.
     *
     * threshold[row] is the leftmost column in that row that is weakly dominated
     * by B. Thus all cells (row, col) with col >= threshold[row] are in B's
     * southeast-dominated region and are forbidden to A. If threshold[row] ==
     * cols, then B has not reached that row.
     *
     * The thresholds are nonincreasing as row increases. Only rows whose
     * threshold is strictly smaller than the previous row contain a visible
     * Pareto-frontier cell.
     */
    private int thresholdGet(long code, int row) {
        return (int)((code >>> (THRESHOLD_BITS * row)) & 31L);
    }

    private long thresholdSet(long code, int row, int value) {
        long mask = 31L << (THRESHOLD_BITS * row);
        return (code & ~mask) | ((long)value << (THRESHOLD_BITS * row));
    }

    /* Count visible frontier cells of B's Pareto frontier. */
    private int frontierCount(long code) {
        int count = 0;
        int previous = cols;

        for (int row = 0; row < rows; row++) {
            int threshold = thresholdGet(code, row);
            if (threshold < previous) {
                count++;
            }
            previous = threshold;
        }

        return count;
    }

    /* Low word of the visible frontier-cell bitboard. */
    private long frontierBitsLo(long code) {
        long result = 0L;
        int previous = cols;

        for (int row = 0; row < rows; row++) {
            int threshold = thresholdGet(code, row);
            if (threshold < previous) {
                result |= bitLo(row * cols + threshold);
            }
            previous = threshold;
        }

        return result;
    }

    /* High word of the visible frontier-cell bitboard. */
    private long frontierBitsHi(long code) {
        long result = 0L;
        int previous = cols;

        for (int row = 0; row < rows; row++) {
            int threshold = thresholdGet(code, row);
            if (threshold < previous) {
                result |= bitHi(row * cols + threshold);
            }
            previous = threshold;
        }

        return result;
    }

    /*
     * Cells forbidden to A: all cells weakly southeast of B's current Pareto
     * frontier. Old A cells can later lie inside this region if B expands past
     * them; they remain explicitly stored in A's bitboard.
     */
    private long forbiddenLo(long code) {
        long forbidden = 0L;

        for (int row = 0; row < rows; row++) {
            int threshold = thresholdGet(code, row);
            for (int col = threshold; col < cols; col++) {
                forbidden |= bitLo(row * cols + col);
            }
        }

        return forbidden;
    }

    private long forbiddenHi(long code) {
        long forbidden = 0L;

        for (int row = 0; row < rows; row++) {
            int threshold = thresholdGet(code, row);
            for (int col = threshold; col < cols; col++) {
                forbidden |= bitHi(row * cols + col);
            }
        }

        return forbidden;
    }

    /*
     * Update B's Pareto frontier after a visible B move. A cell (r,c) moves
     * thresholds leftward to at most c in row r and all lower rows.
     */
    private long bCodeAfterMove(long code, int cell) {
        int moveRow = cell / cols;
        int moveCol = cell % cols;

        for (int row = moveRow; row < rows; row++) {
            if (moveCol < thresholdGet(code, row)) {
                code = thresholdSet(code, row, moveCol);
            }
        }

        return code;
    }

    /* Initial B frontier generated by the single southeast-corner cell. */
    private long initialBCode() {
        long code = 0L;

        for (int row = 0; row < rows; row++) {
            int threshold = row == rows - 1 ? cols - 1 : cols;
            code = thresholdSet(code, row, threshold);
        }

        return code;
    }

    /*
     * Pack the low/high words of the transposition-table entry. The value bit
     * is stored in the same two words for memory efficiency, but is cleared
     * before hashing or comparing keys.
     */
    private long packLo(long aLo, long bCode, int turn, boolean value) {
        long lo = aLo;
        int offset = cells;

        for (int row = 0; row < rows; row++) {
            int threshold = thresholdGet(bCode, row);
            for (int i = 0; i < THRESHOLD_BITS; i++) {
                if ((threshold & (1 << i)) != 0) {
                    lo = setPackedBitLo(lo, offset + i);
                }
            }
            offset += THRESHOLD_BITS;
        }

        if (turn != 0) {
            lo = setPackedBitLo(lo, turnBit);
        }
        if (value) {
            lo = setPackedBitLo(lo, valueBit);
        }

        return lo;
    }

    private long packHi(long aHi, long bCode, int turn, boolean value) {
        long hi = aHi;
        int offset = cells;

        for (int row = 0; row < rows; row++) {
            int threshold = thresholdGet(bCode, row);
            for (int i = 0; i < THRESHOLD_BITS; i++) {
                if ((threshold & (1 << i)) != 0) {
                    hi = setPackedBitHi(hi, offset + i);
                }
            }
            offset += THRESHOLD_BITS;
        }

        if (turn != 0) {
            hi = setPackedBitHi(hi, turnBit);
        }
        if (value) {
            hi = setPackedBitHi(hi, valueBit);
        }

        return hi;
    }

    private void tableInit() {
        /*
         * Start with about one million slots and grow geometrically. For small
         * boards this keeps startup memory reasonable; for 9 x 9 the slot table
         * eventually grows to 2^32 slots.
         */
        slotCapacity = 1L << 20;
        entryCapacity = 1L << 20;
        tableSize = 0;
        slots = new int[slotSegmentCount(slotCapacity)][];
        entryLo = new long[4096][];
        entryHi = new long[4096][];
    }

    private static int slotSegmentCount(long capacity) {
        return (int)((capacity + SLOT_SEG_SIZE - 1L) >>> SLOT_SEG_BITS);
    }

    private int slotAt(long index) {
        int segment = (int)(index >>> SLOT_SEG_BITS);
        int[] array = slots[segment];
        return array == null ? 0 : array[(int)index & SLOT_SEG_MASK];
    }

    private void setSlot(long index, int value) {
        int segment = (int)(index >>> SLOT_SEG_BITS);
        int[] array = slots[segment];
        if (array == null) {
            array = new int[SLOT_SEG_SIZE];
            slots[segment] = array;
        }
        array[(int)index & SLOT_SEG_MASK] = value;
    }

    private void ensureEntrySegment(int index) {
        int segment = index >>> ENTRY_SEG_BITS;
        if (entryLo[segment] == null) {
            entryLo[segment] = new long[ENTRY_SEG_SIZE];
            entryHi[segment] = new long[ENTRY_SEG_SIZE];
        }
    }

    private long entryLoAt(int index) {
        return entryLo[index >>> ENTRY_SEG_BITS][index & ENTRY_SEG_MASK];
    }

    private long entryHiAt(int index) {
        return entryHi[index >>> ENTRY_SEG_BITS][index & ENTRY_SEG_MASK];
    }

    private void setEntry(int index, long lo, long hi) {
        ensureEntrySegment(index);
        entryLo[index >>> ENTRY_SEG_BITS][index & ENTRY_SEG_MASK] = lo;
        entryHi[index >>> ENTRY_SEG_BITS][index & ENTRY_SEG_MASK] = hi;
    }

    private void tableInsertSlot(long lo, long hi, int entryIndex) {
        /*
         * Insert the dense-entry index into the sparse slot table. This is
         * used both for normal insertion and when rebuilding the slot table
         * after growth.
         */
        long mask = slotCapacity - 1L;
        long index = packedHash(lo, hi) & mask;

        while (slotAt(index) != 0) {
            index = (index + 1L) & mask;
        }

        setSlot(index, entryIndex + 1);
    }

    private void tableRehash(long newCapacity) {
        /*
         * Growing the slot table changes the bucket mask, so all dense entries
         * must be reinserted into the new slots. The entries themselves are
         * not copied.
         */
        if (newCapacity > (1L << 32)) {
            throw new OutOfMemoryError("hash slot capacity above 2^32 is not supported");
        }

        slotCapacity = newCapacity;
        slots = new int[slotSegmentCount(slotCapacity)][];

        for (int i = 0; i < tableSize; i++) {
            tableInsertSlot(entryLoAt(i), entryHiAt(i), i);
        }
    }

    private void tableGrowEntries() {
        /*
         * The dense entry arrays are segmented, so growing the logical capacity
         * does not copy old entries. New long[] segments are allocated lazily
         * when their first entry is written.
         */
        if (entryCapacity >= Integer.MAX_VALUE - 1L) {
            throw new OutOfMemoryError("entry pool reached the 32-bit slot-index limit");
        }
        entryCapacity = Math.min(entryCapacity * 2L, Integer.MAX_VALUE - 1L);
    }

    private boolean tableGet(long keyLo, long keyHi) {
        /*
         * Linear probing. The stored value bit is ignored for equality; if the
         * key is found, tableValue is set from the value bit in the stored entry.
         */
        long keyCmpLo = keyLoWithoutValue(keyLo);
        long keyCmpHi = keyHiWithoutValue(keyHi);
        long mask = slotCapacity - 1L;
        long index = packedHash(keyLo, keyHi) & mask;

        for (;;) {
            int slot = slotAt(index);
            if (slot == 0) {
                return false;
            }

            int entryIndex = slot - 1;
            long lo = entryLoAt(entryIndex);
            long hi = entryHiAt(entryIndex);
            if (keyLoWithoutValue(lo) == keyCmpLo && keyHiWithoutValue(hi) == keyCmpHi) {
                tableValue = packedBit(lo, hi, valueBit);
                return true;
            }
            index = (index + 1L) & mask;
        }
    }

    private void tableSet(long keyLo, long keyHi, boolean value) {
        /*
         * Store an exact Boolean minimax value. This table does not store
         * alpha-beta bounds or heuristic scores.
         */
        long keyCmpLo = keyLoWithoutValue(keyLo);
        long keyCmpHi = keyHiWithoutValue(keyHi);
        long mask = slotCapacity - 1L;
        long index = packedHash(keyLo, keyHi) & mask;

        for (;;) {
            int slot = slotAt(index);
            if (slot == 0) {
                break;
            }

            int entryIndex = slot - 1;
            long lo = entryLoAt(entryIndex);
            long hi = entryHiAt(entryIndex);
            if (keyLoWithoutValue(lo) == keyCmpLo && keyHiWithoutValue(hi) == keyCmpHi) {
                if (value) {
                    lo = setPackedBitLo(lo, valueBit);
                    hi = setPackedBitHi(hi, valueBit);
                } else {
                    lo = clearPackedBitLo(lo, valueBit);
                    hi = clearPackedBitHi(hi, valueBit);
                }
                setEntry(entryIndex, lo, hi);
                return;
            }
            index = (index + 1L) & mask;
        }

        if (tableSize >= Integer.MAX_VALUE - 1) {
            throw new OutOfMemoryError("transposition table reached the 32-bit slot-index limit");
        }
        if (tableSize == entryCapacity) {
            tableGrowEntries();
        }
        if ((long)(tableSize + 1) * 10L >= slotCapacity * 7L) {
            tableRehash(slotCapacity * 2L);
            mask = slotCapacity - 1L;
            index = packedHash(keyLo, keyHi) & mask;
            while (slotAt(index) != 0) {
                index = (index + 1L) & mask;
            }
        }

        if (value) {
            keyLo = setPackedBitLo(keyLo, valueBit);
            keyHi = setPackedBitHi(keyHi, valueBit);
        } else {
            keyLo = clearPackedBitLo(keyLo, valueBit);
            keyHi = clearPackedBitHi(keyHi, valueBit);
        }

        setEntry(tableSize, keyLo, keyHi);
        setSlot(index, tableSize + 1);
        tableSize++;
    }

    private long frontierLo(long playerLo, long playerHi, long occupiedLo, long occupiedHi) {
        /*
         * Ordinary graph frontier: empty cells adjacent by an edge to the given
         * territory. For B in the modified game, the "territory" passed here
         * is the set of visible Pareto-frontier cells.
         */
        long resultLo = 0L;
        long lo = playerLo;
        long hi = playerHi;

        while (lo != 0L || hi != 0L) {
            int cell = first(lo, hi);
            resultLo |= neighborLo[cell];
            if (lo != 0L) {
                lo &= lo - 1L;
            } else {
                hi &= hi - 1L;
            }
        }

        return (resultLo & fullLo) & ~occupiedLo;
    }

    private long frontierHi(long playerLo, long playerHi, long occupiedLo, long occupiedHi) {
        long resultHi = 0L;
        long lo = playerLo;
        long hi = playerHi;

        while (lo != 0L || hi != 0L) {
            int cell = first(lo, hi);
            resultHi |= neighborHi[cell];
            if (lo != 0L) {
                lo &= lo - 1L;
            } else {
                hi &= hi - 1L;
            }
        }

        return (resultHi & fullHi) & ~occupiedHi;
    }

    private long bCandidateMovesLo(long aLo, long bCode, long forbiddenLo) {
        /*
         * Visible B moves: B may move from any visible Pareto-frontier cell, not
         * just from the exact B cells that would exist in the original game.
         * A cells and cells already inside B's dominated region are excluded.
         */
        long frontierLo = frontierBitsLo(bCode);
        long frontierHi = frontierBitsHi(bCode);
        return frontierLo(frontierLo, frontierHi, 0L, 0L) & ~aLo & ~forbiddenLo;
    }

    private long bCandidateMovesHi(long aHi, long bCode, long forbiddenHi) {
        long frontierLo = frontierBitsLo(bCode);
        long frontierHi = frontierBitsHi(bCode);
        return frontierHi(frontierLo, frontierHi, 0L, 0L) & ~aHi & ~forbiddenHi;
    }

    private boolean bHasFiller(long aLo, long aHi, int turn, long bCode) {
        /*
         * A filler move represents B spending one move somewhere inside the
         * already dominated region without changing the Pareto frontier.
         *
         * The number of B moves already made is determined by A's move count
         * and the side to move:
         *
         *   hidden B moves = total B moves so far - visible frontier cells.
         *
         * Old A cells can later be swallowed by the forbidden region, but they
         * are still already occupied by A and therefore not available as hidden
         * B filler cells. We do not check whether the remaining empty cells are
         * reachable around those old A cells. Ignoring that obstruction only
         * gives B more options, which is sound for proving A wins.
         */
        int aCount = popcount(aLo, aHi);
        int bTotal = aCount - (turn == 1 ? 1 : 0);
        int visible = frontierCount(bCode);
        int hidden = bTotal - visible;
        long fLo = forbiddenLo(bCode);
        long fHi = forbiddenHi(bCode);
        int aForbidden = popcount(aLo & fLo, aHi & fHi);
        int capacity = popcount(fLo, fHi) - visible - aForbidden;

        return hidden < capacity;
    }

    private boolean bHasLegalMove(long aLo, long aHi, int turn, long bCode) {
        /* B can either change its visible frontier or spend a hidden filler move. */
        long fLo = forbiddenLo(bCode);
        long fHi = forbiddenHi(bCode);
        return !isEmpty(bCandidateMovesLo(aLo, bCode, fLo), bCandidateMovesHi(aHi, bCode, fHi))
            || bHasFiller(aLo, aHi, turn, bCode);
    }

    private static int abs(int value) {
        return value < 0 ? -value : value;
    }

    private int aRushCell(int index) {
        /*
         * Optional A move-ordering path. On square boards this is
         *
         *   (0,1), (1,1), (1,2), (2,2), ...
         *
         * On rectangular boards rows <= cols; A first walks along the long side
         * until the remaining route to the centre becomes diagonal.
         */
        int horizontalLead = (cols - rows) / 2;

        if (index < horizontalLead) {
            return index + 1;
        }

        int diagonalIndex = index - horizontalLead;
        int pair = diagonalIndex / 2;
        int row;
        int col;

        if ((diagonalIndex & 1) == 0) {
            row = pair;
            col = horizontalLead + pair + 1;
        } else {
            row = pair + 1;
            col = horizontalLead + pair + 1;
        }

        return row * cols + col;
    }

    private int centerRushScore(long playerLo, long playerHi, int turn, int cell) {
        /*
         * This score only changes the order in which moves are tried. The
         * solver does not assume the centre-rush move is correct; if it fails,
         * all other legal moves are still searched.
         */
        if (!centerRush || turn != 0) {
            return 0;
        }

        int centerRow = rows / 2;
        int centerCol = cols / 2;
        int centerCell = centerRow * cols + centerCol;
        if (contains(playerLo, playerHi, centerCell)) {
            return 0;
        }

        int pathLength = centerRow + centerCol;
        int nextPathCell = -1;
        int pathBonus = 0;

        for (int i = 0; i < pathLength; i++) {
            int pathCell = aRushCell(i);
            if (!contains(playerLo, playerHi, pathCell)) {
                if (nextPathCell < 0) {
                    nextPathCell = pathCell;
                }
                if (cell == pathCell) {
                    pathBonus = 500 - i;
                }
            }
        }

        if (cell == nextPathCell) {
            pathBonus += 1000;
        }

        int row = cell / cols;
        int col = cell % cols;
        int distance = abs(row - centerRow) + abs(col - centerCol);
        int diagonalOffset = (cols - rows) / 2;
        int diagonalDistance = abs((col - row) - diagonalOffset);

        return pathBonus - 10 * distance - diagonalDistance;
    }

    private boolean betterScores(
        int itemScoreCenter,
        int itemScoreTrap,
        int itemScoreOppFrontier,
        int itemScoreOwnFrontier,
        int itemScoreCell,
        int right
    ) {
        /*
         * Lexicographic move ordering:
         *
         *   1. Optional A centre-rush preference.
         *   2. Moves that leave the opponent with no frontier.
         *   3. Moves that minimize the opponent's frontier.
         *   4. Moves that maximize our frontier.
         *   5. Stable low-cell-number tie-breaker.
         */
        if (itemScoreCenter != scoreCenter[right]) {
            return itemScoreCenter > scoreCenter[right];
        }
        if (itemScoreTrap != scoreTrap[right]) {
            return itemScoreTrap > scoreTrap[right];
        }
        if (itemScoreOppFrontier != scoreOppFrontier[right]) {
            return itemScoreOppFrontier > scoreOppFrontier[right];
        }
        if (itemScoreOwnFrontier != scoreOwnFrontier[right]) {
            return itemScoreOwnFrontier > scoreOwnFrontier[right];
        }
        return itemScoreCell > scoreCell[right];
    }

    private void copyMove(int dst, int src) {
        moveCell[dst] = moveCell[src];
        moveFiller[dst] = moveFiller[src];
        scoreCenter[dst] = scoreCenter[src];
        scoreTrap[dst] = scoreTrap[src];
        scoreOppFrontier[dst] = scoreOppFrontier[src];
        scoreOwnFrontier[dst] = scoreOwnFrontier[src];
        scoreCell[dst] = scoreCell[src];
    }

    private int orderedMoves(long aLo, long aHi, long bCode, int turn, long aFrontierLo, long aFrontierHi, int depth) {
        /*
         * Generate exactly the legal moves in the modified game, then sort them
         * by the proof-preserving heuristic above. No move is removed by the
         * ordering.
         */
        int base = depth * MAX_CELLS;
        long forbiddenLo = forbiddenLo(bCode);
        long forbiddenHi = forbiddenHi(bCode);
        long playerFrontierLo;
        long playerFrontierHi;
        long opponentFrontierLo;
        long opponentFrontierHi;
        long playerLo;
        long playerHi;

        if (turn == 0) {
            playerFrontierLo = aFrontierLo & ~forbiddenLo;
            playerFrontierHi = aFrontierHi & ~forbiddenHi;
            opponentFrontierLo = bCandidateMovesLo(aLo, bCode, forbiddenLo);
            opponentFrontierHi = bCandidateMovesHi(aHi, bCode, forbiddenHi);
            playerLo = aLo;
            playerHi = aHi;
        } else {
            playerFrontierLo = bCandidateMovesLo(aLo, bCode, forbiddenLo);
            playerFrontierHi = bCandidateMovesHi(aHi, bCode, forbiddenHi);
            opponentFrontierLo = aFrontierLo;
            opponentFrontierHi = aFrontierHi;
            playerLo = frontierBitsLo(bCode);
            playerHi = frontierBitsHi(bCode);
        }

        long originalPlayerFrontierLo = playerFrontierLo;
        long originalPlayerFrontierHi = playerFrontierHi;
        int count = 0;

        while (playerFrontierLo != 0L || playerFrontierHi != 0L) {
            int cell = first(playerFrontierLo, playerFrontierHi);
            long moveLo = bitLo(cell);
            long moveHi = bitHi(cell);
            long nextOpponentFrontierLo = opponentFrontierLo & ~moveLo;
            long nextOpponentFrontierHi = opponentFrontierHi & ~moveHi;
            long nextPlayerFrontierLo;
            long nextPlayerFrontierHi;

            if (turn == 0) {
                /*
                 * A plays a visible cell. A's frontier grows from that cell,
                 * then occupied and Pareto-forbidden cells are removed.
                 */
                long nextALo = aLo | moveLo;
                long nextAHi = aHi | moveHi;
                long bFrontierLo = frontierBitsLo(bCode);
                long bFrontierHi = frontierBitsHi(bCode);
                long nextOccupiedLo = nextALo | bFrontierLo;
                long nextOccupiedHi = nextAHi | bFrontierHi;

                nextPlayerFrontierLo =
                    ((originalPlayerFrontierLo | neighborLo[cell]) & fullLo & ~nextOccupiedLo) & ~forbiddenLo;
                nextPlayerFrontierHi =
                    ((originalPlayerFrontierHi | neighborHi[cell]) & fullHi & ~nextOccupiedHi) & ~forbiddenHi;
            } else {
                /*
                 * A visible B move may move the Pareto frontier leftward and
                 * thereby newly forbid some of A's frontier cells.
                 */
                long nextBCode = bCodeAfterMove(bCode, cell);
                long nextForbiddenLo = forbiddenLo(nextBCode);
                long nextForbiddenHi = forbiddenHi(nextBCode);
                nextPlayerFrontierLo = bCandidateMovesLo(aLo, nextBCode, nextForbiddenLo);
                nextPlayerFrontierHi = bCandidateMovesHi(aHi, nextBCode, nextForbiddenHi);
                nextOpponentFrontierLo &= ~nextForbiddenLo;
                nextOpponentFrontierHi &= ~nextForbiddenHi;
            }

            int target = base + count++;
            moveCell[target] = cell;
            moveFiller[target] = false;
            scoreCenter[target] = centerRushScore(playerLo, playerHi, turn, cell);
            scoreTrap[target] = isEmpty(nextOpponentFrontierLo, nextOpponentFrontierHi) ? 1 : 0;
            scoreOppFrontier[target] = -popcount(nextOpponentFrontierLo, nextOpponentFrontierHi);
            scoreOwnFrontier[target] = popcount(nextPlayerFrontierLo, nextPlayerFrontierHi);
            scoreCell[target] = -cell;

            if (playerFrontierLo != 0L) {
                playerFrontierLo &= playerFrontierLo - 1L;
            } else {
                playerFrontierHi &= playerFrontierHi - 1L;
            }
        }

        if (turn == 1 && bHasFiller(aLo, aHi, turn, bCode)) {
            /*
             * Hidden B filler move. It does not change B's frontier, but it is
             * a real move in the game tree because B must make the same number
             * of moves as in the uncompressed play.
             */
            long nextAFrontierLo = aFrontierLo & ~forbiddenLo;
            long nextAFrontierHi = aFrontierHi & ~forbiddenHi;
            int target = base + count++;
            moveCell[target] = -1;
            moveFiller[target] = true;
            scoreCenter[target] = 0;
            scoreTrap[target] = isEmpty(nextAFrontierLo, nextAFrontierHi) ? 1 : 0;
            scoreOppFrontier[target] = -popcount(nextAFrontierLo, nextAFrontierHi);
            scoreOwnFrontier[target] = popcount(playerFrontierLo, playerFrontierHi);
            scoreCell[target] = -MAX_CELLS;
        }

        for (int i = 1; i < count; i++) {
            /* At most 81 moves, so insertion sort is simple and adequate. */
            int itemCell = moveCell[base + i];
            boolean itemFiller = moveFiller[base + i];
            int itemScoreCenter = scoreCenter[base + i];
            int itemScoreTrap = scoreTrap[base + i];
            int itemScoreOppFrontier = scoreOppFrontier[base + i];
            int itemScoreOwnFrontier = scoreOwnFrontier[base + i];
            int itemScoreCell = scoreCell[base + i];
            int j = i;

            while (j > 0) {
                int right = base + j - 1;
                if (!betterScores(
                    itemScoreCenter,
                    itemScoreTrap,
                    itemScoreOppFrontier,
                    itemScoreOwnFrontier,
                    itemScoreCell,
                    right
                )) {
                    break;
                }
                copyMove(base + j, right);
                j--;
            }

            moveCell[base + j] = itemCell;
            moveFiller[base + j] = itemFiller;
            scoreCenter[base + j] = itemScoreCenter;
            scoreTrap[base + j] = itemScoreTrap;
            scoreOppFrontier[base + j] = itemScoreOppFrontier;
            scoreOwnFrontier[base + j] = itemScoreOwnFrontier;
            scoreCell[base + j] = itemScoreCell;
        }

        return count;
    }

    private void printCell(StringBuilder out, int cell) {
        if (cell < 0) {
            out.append("filler");
        } else {
            out.append('(').append(cell / cols).append(',').append(cell % cols).append(')');
        }
    }

    private String progressPrefix(int depth, int limit) {
        StringBuilder out = new StringBuilder();
        int shown = Math.min(depth, limit);

        for (int i = 0; i < shown; i++) {
            if (i > 0) {
                out.append(' ');
            }
            out.append((i & 1) == 0 ? 'A' : 'B');
            printCell(out, progressPath[i]);
        }
        if (shown < depth) {
            out.append(" ...");
        }

        return out.toString();
    }

    private void reportPrefixCompletion(int depth, String kind) {
        /* Optional diagnostic mode for counting completed search prefixes. */
        progressPrefixCompleted++;
        System.err.printf(
            Locale.ROOT,
            "prefix progress: completed=%d total_seen=%d calls=%d stored=%d %s prefix=%s%n",
            progressPrefixCompleted,
            progressPrefixTotal,
            calls,
            tableSize,
            kind,
            progressPrefix(depth, depth)
        );
    }

    private double roughStackFraction(int depth) {
        /*
         * A crude progress indicator based on child indices along the current
         * DFS stack. It is not a mathematically meaningful percentage of work.
         */
        double fraction = 0.0;
        double weight = 1.0;
        int limit = Math.min(depth, MAX_CELLS);

        for (int i = 0; i < limit; i++) {
            int count = progressChildCount[i];
            int index = progressChildIndex[i];
            if (count <= 0) {
                break;
            }
            weight /= count;
            fraction += index * weight;
        }

        return fraction;
    }

    private void printStackProgress(int depth) {
        double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
        int limit = Math.min(depth, 20);
        StringBuilder stack = new StringBuilder();

        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                stack.append(' ');
            }
            stack.append((i & 1) == 0 ? 'A' : 'B')
                .append(progressChildIndex[i] + 1)
                .append('/')
                .append(progressChildCount[i]);
        }
        if (limit < depth) {
            stack.append(" ...");
        }

        System.err.printf(
            Locale.ROOT,
            "stack progress: calls=%d stored=%d elapsed=%.0fs depth=%d rough=%.9f stack=%s line=%s%n",
            calls,
            tableSize,
            elapsed,
            depth,
            roughStackFraction(depth),
            stack,
            progressPrefix(depth, 20)
        );
    }

    private boolean win(long aLo, long aHi, long bCode, int turn, long aFrontierLo, long aFrontierHi, int depth) {
        /*
         * Exact Boolean minimax:
         *
         *   win(position) = exists legal child with !win(child).
         *
         * If no legal child exists, count is zero, the loop below is skipped,
         * and result remains false. Thus the side to move loses. There is no
         * heuristic evaluation of leaves.
         */
        if (depth > cells - 2) {
            throw new IllegalStateException("search depth bound exceeded: " + depth);
        }

        calls++;
        boolean progressPrefixNode = progressDepth > 0 && depth == progressDepth;

        if (progressPrefixNode) {
            /* This is only for progress reporting on long runs. */
            progressPrefixTotal++;
        }

        if (progressInterval != 0 && calls >= nextProgress) {
            double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
            double callsPerSecond = elapsed > 0.0 ? calls / elapsed : 0.0;
            System.err.printf(
                Locale.ROOT,
                "progress: calls=%d hits=%d stored=%d elapsed=%.0fs calls/s=%.0f%n",
                calls,
                hits,
                tableSize,
                elapsed,
                callsPerSecond
            );
            do {
                nextProgress += progressInterval;
            } while (calls >= nextProgress);
        }

        if (progressLineInterval != 0 && calls >= nextProgressLine) {
            double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
            System.err.printf(
                Locale.ROOT,
                "line progress: calls=%d stored=%d elapsed=%.0fs depth=%d line=%s%n",
                calls,
                tableSize,
                elapsed,
                depth,
                progressPrefix(depth, 20)
            );
            do {
                nextProgressLine += progressLineInterval;
            } while (calls >= nextProgressLine);
        }

        if (progressStackInterval != 0 && calls >= nextProgressStack) {
            printStackProgress(depth);
            do {
                nextProgressStack += progressStackInterval;
            } while (calls >= nextProgressStack);
        }

        long keyLo = packLo(aLo, bCode, turn, false);
        long keyHi = packHi(aHi, bCode, turn, false);

        if (tableGet(keyLo, keyHi)) {
            /* Transposition hit: reuse the exact value already proved. */
            hits++;
            if (progressPrefixNode) {
                reportPrefixCompletion(depth, "hit");
            }
            return tableValue;
        }

        int count = orderedMoves(aLo, aHi, bCode, turn, aFrontierLo, aFrontierHi, depth);
        boolean result = false;
        int base = depth * MAX_CELLS;

        if (depth < MAX_CELLS) {
            progressChildCount[depth] = count;
        }

        for (int i = 0; i < count; i++) {
            int index = base + i;
            int cell = moveCell[index];
            if (depth < MAX_CELLS) {
                progressChildIndex[depth] = i;
            }
            if ((progressDepth > 0 || progressLineInterval != 0 || progressStackInterval != 0) && depth < MAX_CELLS) {
                progressPath[depth] = cell;
            }

            if (turn == 0) {
                /*
                 * If B has no legal reply after A's move, the child is losing
                 * for B immediately and no recursive call is needed.
                 */
                long moveLo = bitLo(cell);
                long moveHi = bitHi(cell);
                long nextALo = aLo | moveLo;
                long nextAHi = aHi | moveHi;
                long forbiddenLo = forbiddenLo(bCode);
                long forbiddenHi = forbiddenHi(bCode);
                long nextAFrontierLo = ((aFrontierLo | neighborLo[cell]) & fullLo & ~nextALo) & ~forbiddenLo;
                long nextAFrontierHi = ((aFrontierHi | neighborHi[cell]) & fullHi & ~nextAHi) & ~forbiddenHi;

                if (!bHasLegalMove(nextALo, nextAHi, 1, bCode)) {
                    result = true;
                    break;
                }
                if (!win(nextALo, nextAHi, bCode, 1, nextAFrontierLo, nextAFrontierHi, depth + 1)) {
                    result = true;
                    break;
                }
            } else {
                /*
                 * If B's move leaves A with no frontier, the child is losing
                 * for A immediately.
                 */
                long nextBCode = bCode;
                long nextAFrontierLo = aFrontierLo;
                long nextAFrontierHi = aFrontierHi;

                if (!moveFiller[index]) {
                    long moveLo = bitLo(cell);
                    long moveHi = bitHi(cell);
                    nextBCode = bCodeAfterMove(bCode, cell);
                    nextAFrontierLo = (aFrontierLo & ~moveLo) & ~forbiddenLo(nextBCode);
                    nextAFrontierHi = (aFrontierHi & ~moveHi) & ~forbiddenHi(nextBCode);
                } else {
                    nextAFrontierLo = aFrontierLo & ~forbiddenLo(bCode);
                    nextAFrontierHi = aFrontierHi & ~forbiddenHi(bCode);
                }

                if (isEmpty(nextAFrontierLo, nextAFrontierHi)) {
                    result = true;
                    break;
                }
                if (!win(aLo, aHi, nextBCode, 0, nextAFrontierLo, nextAFrontierHi, depth + 1)) {
                    result = true;
                    break;
                }
            }
        }

        tableSet(keyLo, keyHi, result);

        if (progressPrefixNode) {
            reportPrefixCompletion(depth, "searched");
        }

        return result;
    }

    private void init(int inputRows, int inputCols, boolean useCenterRush) {
        /*
         * The board is symmetric under swapping rows and columns. Normalize to
         * rows <= cols; this also keeps the rectangular centre-rush ordering
         * simple.
         */
        if (inputRows > inputCols) {
            int tmp = inputRows;
            inputRows = inputCols;
            inputCols = tmp;
        }
        if (inputRows < 3 || inputCols < 3 || (inputRows & 1) == 0 || (inputCols & 1) == 0) {
            /*
             * Even dimensions are deliberately not searched here. On those
             * boards B has a direct mirroring strategy.
             */
            throw new IllegalArgumentException("both board dimensions must be odd integers at least 3");
        }
        if (inputRows > MAX_ROWS || inputRows * inputCols > MAX_CELLS || inputCols > 31) {
            throw new IllegalArgumentException("packed solver requires rows <= 9, cols <= 31, cells <= 81");
        }

        rows = inputRows;
        cols = inputCols;
        cells = rows * cols;
        turnBit = cells + THRESHOLD_BITS * rows;
        valueBit = turnBit + 1;
        centerRush = useCenterRush;

        if (cells > 64) {
            fullLo = -1L;
            fullHi = (1L << (cells - 64)) - 1L;
        } else {
            fullLo = (1L << cells) - 1L;
            fullHi = 0L;
        }

        if (valueBit >= 128) {
            /*
             * For 9 x 9, valueBit is 127, which still fits. Larger square
             * boards would need a different packed representation.
             */
            throw new IllegalArgumentException("packed key does not fit in 128 bits");
        }

        Arrays.fill(neighborLo, 0L);
        Arrays.fill(neighborHi, 0L);
        /* Precompute four-neighbour masks for every cell. */
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int cell = row * cols + col;
                long lo = 0L;
                long hi = 0L;

                if (row > 0) {
                    lo |= bitLo((row - 1) * cols + col);
                    hi |= bitHi((row - 1) * cols + col);
                }
                if (row + 1 < rows) {
                    lo |= bitLo((row + 1) * cols + col);
                    hi |= bitHi((row + 1) * cols + col);
                }
                if (col > 0) {
                    lo |= bitLo(row * cols + col - 1);
                    hi |= bitHi(row * cols + col - 1);
                }
                if (col + 1 < cols) {
                    lo |= bitLo(row * cols + col + 1);
                    hi |= bitHi(row * cols + col + 1);
                }

                neighborLo[cell] = lo;
                neighborHi[cell] = hi;
            }
        }

        tableInit();
    }

    private boolean solve() {
        /*
         * Initial position: A at (0,0), B represented by the Pareto frontier of
         * the single southeast-corner cell, and A to move.
         */
        long aLo = 1L;
        long aHi = 0L;
        long bCode = initialBCode();
        long bFrontierLo = frontierBitsLo(bCode);
        long bFrontierHi = frontierBitsHi(bCode);
        long occupiedLo = aLo | bFrontierLo;
        long occupiedHi = aHi | bFrontierHi;
        long forbiddenLo = forbiddenLo(bCode);
        long forbiddenHi = forbiddenHi(bCode);
        long aFrontierLo = frontierLo(aLo, aHi, occupiedLo, occupiedHi) & ~forbiddenLo;
        long aFrontierHi = frontierHi(aLo, aHi, occupiedLo, occupiedHi) & ~forbiddenHi;

        startNanos = System.nanoTime();
        return win(aLo, aHi, bCode, 0, aFrontierLo, aFrontierHi, 0);
    }

    private static void usage() {
        System.err.println(
            "usage: java Question5136962 [--no-center-rush] [--progress-calls N] [--progress-line N] " +
            "[--progress-stack N] [--progress-depth D] n\n" +
            "       java Question5136962 [--no-center-rush] [--progress-calls N] [--progress-line N] " +
            "[--progress-stack N] [--progress-depth D] rows cols"
        );
    }

    public static void main(String[] args) {
        /*
         * Typical use:
         *
         *   javac Question5136962.java
         *   java -Xmx90g Question5136962 9
         *
         * Progress options are optional and intended only for long runs.
         */
        boolean centerRush = true;
        long progressInterval = 0L;
        long progressLineInterval = 0L;
        long progressStackInterval = 0L;
        int progressDepth = 0;
        int argIndex = 0;

        try {
            while (argIndex < args.length && args[argIndex].startsWith("--")) {
                switch (args[argIndex]) {
                    case "--no-center-rush":
                        centerRush = false;
                        argIndex++;
                        break;
                    case "--progress-calls":
                        progressInterval = parsePositiveLong(args, ++argIndex);
                        argIndex++;
                        break;
                    case "--progress-line":
                        progressLineInterval = parsePositiveLong(args, ++argIndex);
                        argIndex++;
                        break;
                    case "--progress-stack":
                        progressStackInterval = parsePositiveLong(args, ++argIndex);
                        argIndex++;
                        break;
                    case "--progress-depth":
                        long depth = parsePositiveLong(args, ++argIndex);
                        if (depth >= MAX_CELLS) {
                            throw new IllegalArgumentException();
                        }
                        progressDepth = (int)depth;
                        argIndex++;
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }

            if (args.length != argIndex + 1 && args.length != argIndex + 2) {
                throw new IllegalArgumentException();
            }

            int rows = Integer.parseInt(args[argIndex]);
            int cols = args.length == argIndex + 2 ? Integer.parseInt(args[argIndex + 1]) : rows;

            Question5136962 solver = new Question5136962();
            solver.init(rows, cols, centerRush);
            solver.progressInterval = progressInterval;
            solver.nextProgress = progressInterval;
            solver.progressLineInterval = progressLineInterval;
            solver.nextProgressLine = progressLineInterval;
            solver.progressStackInterval = progressStackInterval;
            solver.nextProgressStack = progressStackInterval;
            solver.progressDepth = progressDepth;

            long start = System.nanoTime();
            boolean aWins = solver.solve();
            double elapsed = (System.nanoTime() - start) / 1_000_000_000.0;

            System.out.printf(Locale.ROOT, "board = %d x %d%n", solver.rows, solver.cols);
            if (solver.centerRush) {
                System.out.println("center rush = A");
            }
            System.out.println("packed TT = on");
            System.out.println("compressed B frontier = on");
            System.out.printf(Locale.ROOT, "winner = %c%n", aWins ? 'A' : 'B');
            System.out.printf(Locale.ROOT, "recursive calls = %d%n", solver.calls);
            System.out.printf(Locale.ROOT, "transposition hits = %d%n", solver.hits);
            System.out.printf(Locale.ROOT, "stored positions = %d%n", solver.tableSize);
            System.out.printf(Locale.ROOT, "hash slots = %d%n", solver.slotCapacity);
            System.out.printf(Locale.ROOT, "time = %.2fs%n", elapsed);
        } catch (RuntimeException ex) {
            usage();
            if (ex.getMessage() != null) {
                System.err.println(ex.getMessage());
            }
            System.exit(2);
        }
    }

    private static long parsePositiveLong(String[] args, int index) {
        if (index >= args.length) {
            throw new IllegalArgumentException();
        }
        long value = Long.parseLong(args[index]);
        if (value <= 0L) {
            throw new IllegalArgumentException();
        }
        return value;
    }
}
