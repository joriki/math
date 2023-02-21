package info.joriki.math.stackexchange;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import info.joriki.math.combinatorics.Permutations;

public class Question4643411 {
    final static int nranks = 13;
    final static int nsuits = 4;
    final static int ncards = 5;
    final static int nflop  = 3;
    
    static class Card {
        int rank;
        int suit;
        
        void increment() {
            if (++rank == nranks) {
                rank = 0;
                suit++;
            }
        }
        
        void copy (Card c) {
            rank = c.rank;
            suit = c.suit;
        }
        
        public boolean equals(Object o) {
            Card c = (Card) o;
            return c.rank == rank && c.suit == suit;
        }
    }
    
    static Card [] board = new Card [ncards];
    
    static int count;
       
    static int [] [] flopPermutations = Permutations.getPermutations(nflop);
    static int [] [] suitPermutations = Permutations.getPermutations(nsuits);
    
    static Set<String> boards = new HashSet<>();

    public static void main (String [] args) {
        for (int i = 0;i < board.length;i++)
            board [i] = new Card();

        rankRecursion(0,0);
        System.out.println(count);

        cardRecursion(0);
        System.out.println(boards.size());
    }
    
    static void rankRecursion(int index,int firstRank) {
        if (index == ncards) {
            int rankCount = 0;
            for (Card card : board)
                if (card.rank == board [0].rank)
                    rankCount++;
            if (rankCount <= nsuits)
                suitRecursion(0,0);
        }
        else
            for (int rank = index < nflop ? firstRank : 0;rank < nranks;rank++) {
                board [index].rank = rank;
                rankRecursion (index + 1,rank);
            }
    }

    static int [] flopAssignments = new int [nsuits];
    
    static void suitRecursion(int index,int max) {
        if (index == ncards)
            count++;
        else
            outer:
            for (int suit = 0;suit <= max;suit++) {
                for (int i = 0;i < index;i++)
                    if (board [i].rank == board [index].rank && (board [i].suit == suit || (index < nflop && board [i].suit > suit)))
                        continue outer;

                if (index <= nflop || board [nflop].suit != suit)
                    for (int equivalent = 0;equivalent < suit;equivalent++)
                        if ((index <= nflop || board [nflop].suit != equivalent) && flopAssignments [equivalent] == flopAssignments [suit])
                            continue outer;

                board [index].suit = suit;
                
                if (index < nflop)
                    flopAssignments [suit] |= 1 << board [index].rank;
                suitRecursion(index + 1,suit < max || max == nsuits - 1 ? max : max + 1);
                if (index < nflop)
                    flopAssignments [suit] &= ~(1 << board [index].rank);
            }
    }
    
    static void cardRecursion(int index) {
        if (index == ncards) {
            Set<String> permuted = new TreeSet<>();
            for (int [] f : flopPermutations)
                for (int [] s : suitPermutations) {
                    StringBuilder boardBuilder = new StringBuilder();
                    for (int i = 0;i < ncards;i++) {
                        Card card = board [i < nflop ? f [i] : i];
                        boardBuilder.append((char) ('a' + card.rank)).append(s [card.suit]);
                    }
                    permuted.add(boardBuilder.toString());
                }
            boards.add(permuted.iterator().next());
        }
        else {
            Card card = board [index];
            if (index > 0 && index < nflop) {
                card.copy(board [index - 1]);
                card.increment();
            }
            else {
                card.rank = 0;
                card.suit = 0;
            }
         
            outer:
            for (;card.suit < nsuits;card.increment()) {
                if (index >= nflop)
                    for (int i = 0;i < index;i++)
                        if (board [i].equals(board [index]))
                            continue outer;
                cardRecursion(index + 1);
            }
        }
    }
}
