import java.util.SortedSet; 
import java.util.TreeSet;
import java.util.List;

/**
 * Contient les constantes pré-calculées pour les cases et types de pièces.
 */
public abstract class BestMove{

    /**
     * Retourne le meilleur move.
     * @param state Plateau
     * @param depth Profondeur
     * @return
     */
    public static String compute(Board state, int depth) {
        Move best = alphabeta(state, depth);
        return Board.toUCI(best);
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
     * //TODO : utilsier un sorted set //
     */
    /*private static SortedSet<Move> successors(Board state) {
        SortedSet<Move> moves = new TreeSet<>();
        return moves;
    }*/
    private static List<Move> successors(Board state) {
        return state.generator.get_legal_moves(state.player_turn());
    }

    /**
     * Alpha-beta minimax
     * @param state Plateau
     * @param depth Profondeur de récursion maximale
     */
    private static Move alphabeta(Board state, int depth) {
        Move best = null;
        
        //Récursion
        int v = - INFINITY;
        int alpha = -INFINITY, beta = +INFINITY;
        for (Move successor: successors(state)) {
            state.apply(successor.move);
            v = Math.max(v, minvalue(state, alpha, beta, depth-1));
            state.revert();
            if (v > alpha) best = successor;
            alpha = Math.max(alpha, v);
        }

        return best;
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
        for (Move successor: successors(state)) {
            state.apply(successor.move);
            v = Math.max(v, minvalue(state, alpha, beta, depth-1));
            state.revert();
            if (v >= beta) return v;
            alpha = Math.max(alpha, v);
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
        for (Move successor: successors(state)) {
            state.apply(successor.move);
            v = Math.min(v, maxvalue(state, alpha, beta, depth-1));
            state.revert();
            if (v <= alpha) return v;
            beta = Math.min(beta, v);
        }
        return v;
    }

}