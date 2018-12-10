import java.util.SortedSet; 
import java.util.TreeSet;

/**
 * Contient les constantes pré-calculées pour les cases et types de pièces.
 */
public abstract class BestMove {

    public static void compute(Board state, int depth) {
        alphabeta(state, depth);
    }

    /** Valeur de l'infini */
    private static int INFINITY = Integer.MAX_VALUE;

    /**
     * Indique s'il s'agit d'un état terminal
     * @param state Plateau
     */
    private static boolean terminal(Board state) {
        return false;
    }

    /**
     * Retourne la valeur d'utilité de l'état
     * @param state Plateau
     */
    private static int utility(Board state) {
        return 0;
    }

    /** 
     * Enumère les successeurs possible de l'état actuel 
     * @param state Plateau
     */
    private static SortedSet<Move> successors(Board state) {
        SortedSet<Move> moves = new TreeSet<>();
        return moves;
    }

    /**
     * Alpha-beta minimax
     * @param state Plateau
     * @param depth Profondeur de récursion maximale
     */
    private static int alphabeta(Board state, int depth) {
        return maxvalue(state, -INFINITY, +INFINITY, depth);
    }

    /**
     * Maxvalue - Alpha-beta
     * @param state Plateau
     * @param alpha Alpha
     * @param beta Beta
     * @param depth Profondeur de récursion maximale
     */
    private static int maxvalue(Board state, int alpha, int beta, int depth) {
        //Etat terminal ou profondeur maximale atteinte
        if ((terminal(state))||(depth < 0)) 
            return utility(state);
        
        //Récursion
        int v = - INFINITY;
        for (Move move: successors(state)) {
            v = Math.max(v, minvalue(state, alpha, beta, depth-1));
            if (v >= beta) return v;
        }
        return v;
    }

    /**
     * Minvalue - Alpha-beta
     * @param state Plateau
     * @param alpha Alpha
     * @param beta Beta
     * @param depth Profondeur de récursion maximale
     */
    private static int minvalue(Board state, int alpha, int beta, int depth) {
        //Etat terminal ou profondeur maximale atteinte
        if ((terminal(state))||(depth < 0)) 
            return utility(state);
        
        //Récursion
        int v = + INFINITY;
        for (Move move: successors(state)) {
            v = Math.min(v, maxvalue(state, alpha, beta, depth-1));
            if (v <= alpha) return v;
        }
        return v;
    }

}
