import java.util.SortedSet; 
import java.util.TreeSet;
import java.util.List;
import java.util.concurrent.Future;

class BestMoveThread implements Runnable {
	
	/**
	 * Constructeur.
	 * @param state
	 */
	BestMoveThread(Board state) {
		this.state = state;
	}

	/** Copie du plateau. */
	private Board state;

	/**
	 * Appel du thread.
	 * Recherche par alphabeta le meilleur coup.
	 * Celui-ci est formatté de la façon suivante : uci@score (afin que le thread principal puisse déterminer le meilleur coup)
	 */
    public void run() {
		this.id = Thread.currentThread().getId() % BestMove.THREAD;
		//System.out.println("info thread "+id+" : started");
		alphabeta(state);
		//System.out.println("info thread "+id+" : found "+best);
	}

	/** Meilleur coup trouvé jusqu'à présent. */
	public String best = "@0";

	/** Id du thread. */
	public long id;

	/** Valeur de l'infini */
	private static int INFINITY = Integer.MAX_VALUE;

	/** Profondeur maximale (à configurer dans BestMove) */
	private static int MAXDEPTH = BestMove.MAXDEPTH;

	/**
	 * Indique s'il s'agit d'un état terminal
	 * @param state Plateau
	 */
	private boolean terminal(Board state) {
		return BestMove.terminal(state);
	}

	/**
	 * Retourne la valeur d'utilité de l'état
	 * @param state Plateau
	 */
	private int utility(Board state) {
		return BestMove.utility(state);
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
	private String alphabeta(Board state) {
		best = "@0";
		//long time = System.currentTimeMillis();
		int v = 0;
		//Alpha beta (pour les blancs, il s'agit d'une version modifiée du maxvalue qui retient en mémoire le meilleur coup à jouer)
		if (state.white) {
			for (int depth = 2; depth < MAXDEPTH; depth++) {
				//Récursion
				//System.out.println("info thread "+id+" : depth "+depth+" (started)");
				v = - INFINITY;
				int alpha = -INFINITY, beta = +INFINITY;
				for (Move successor: successors(state)) {
					state.apply(successor.move);
					v = Math.max(v, minvalue(state, alpha, beta, depth-1));
					state.revert();
					if (v >= alpha) best = Board.toUCI(successor)+"@"+Math.abs(v);
					alpha = Math.max(alpha, v);
				}
				//System.out.println("info thread "+id+" : depth "+depth+" (completed in "+(System.currentTimeMillis()-time)+" ms)");
			}
		}
		//Alpha beta (pour les noirs, il s'agit d'une version modifiée du minvalue qui retient en mémoire le meilleur coup à jouer)
		else {
			for (int depth = 2; depth < MAXDEPTH; depth++) {
				//Récursion
				//System.out.println("info thread "+id+" : depth "+depth+" (started)");
				v = + INFINITY;
				int alpha = -INFINITY, beta = +INFINITY;
				for (Move successor: successors(state)) {
					state.apply(successor.move);
					v = Math.min(v, maxvalue(state, alpha, beta, depth-1));
					state.revert();
					if (v <= beta) best = Board.toUCI(successor)+"@"+Math.abs(v);
					beta = Math.min(beta, v);
				}
				//System.out.println("info thread "+id+" : depth "+depth+" (completed in "+(System.currentTimeMillis()-time)+" ms)");
			}
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
	private int maxvalue(Board state, int alpha, int beta, int depth) {
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
	private int minvalue(Board state, int alpha, int beta, int depth) {
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
