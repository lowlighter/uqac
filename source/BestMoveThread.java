import java.util.SortedSet; 
import java.util.TreeSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.Arrays;

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
		this.id = Thread.currentThread().getId() % (Math.max(BestMove.THREAD, 1));
		//System.out.println("info thread "+id+" : started");
		alphabeta(state);
	}

	/** Meilleur coup trouvé jusqu'à présent. */
	private String _best = "@0";

	public String best() {
		System.out.println("info thread "+id+" : best "+_best);
		return _best;
	}

	private void best(Move successor, int score) {
		if ((successor != null)&&(!BestMove.timeout())) {
			_best = Board.toUCI(successor)+"@"+Math.abs(score);
		}
	}

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

	private int starting_point = 0;

	/**
	 * Alpha-beta minimax
	 * @param state Plateau
	 * @param depth Profondeur de récursion maximale
	 */
	private void alphabeta(Board state) {
		best(null, 0);
		//long time = System.currentTimeMillis();
		int v = 0;
		starting_point = state.moves.size();
		//Alpha beta (pour les blancs, il s'agit d'une version modifiée du maxvalue qui retient en mémoire le meilleur coup à jouer)
		if (state.player_turn() == state.WHITE) {
			for (int depth = 1; depth <= MAXDEPTH; depth++) {
				//Récursion
				//System.out.println("info thread "+id+" : depth "+depth+" (started)");
				v = - INFINITY;
				int alpha = -INFINITY, beta = +INFINITY;
				for (Move successor: successors(state)) {
					state.apply(successor.move);
					v = Math.max(v, minvalue(state, alpha, beta, depth-1));
					//state.print();
					//state.dominance();
					//System.out.println("info test "+Board.toUCI(successor)+" "+v);
					state.revert();
					if (v > alpha) best(successor, v);
					alpha = Math.max(alpha, v);
				}
				//System.out.println("info thread "+id+" : depth "+depth+" (completed in "+(System.currentTimeMillis()-time)+" ms)");
			}
		}
		//Alpha beta (pour les noirs, il s'agit d'une version modifiée du minvalue qui retient en mémoire le meilleur coup à jouer)
		else {
			for (int depth = 1; depth <= MAXDEPTH; depth++) {
				//Récursion
				//System.out.println("info thread "+id+" : depth "+depth+" (started)");
				v = + INFINITY;
				int alpha = -INFINITY, beta = +INFINITY;
				for (Move successor: successors(state)) {
					state.apply(successor.move);
					//state.print();
					//state.dominance();
					v = Math.min(v, maxvalue(state, alpha, beta, depth-1));
					//System.out.println("info test "+Board.toUCI(successor)+" "+v);
					state.revert();
					if (v < beta) best(successor, v);
					beta = Math.min(beta, v);
				}
				//System.out.println("info thread "+id+" : depth "+depth+" (completed in "+(System.currentTimeMillis()-time)+" ms)");
			}
		}
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
		if ((terminal(state))||(depth < 0)) {
			//System.out.println("info eval "+state.moves.subList(starting_point, state.moves.size())+" | "+utility(state));
			return utility(state);
		}
		
		//Récursion
		int v = - INFINITY;
		for (Move successor: successors(state)) {
			state.apply(successor.move);
			v = Math.max(v, minvalue(state, alpha, beta, depth-1));
			//System.out.println("info max "+state.moves.subList(starting_point, state.moves.size())+" "+v);
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
		if ((terminal(state))||(depth < 0)) {
			//System.out.println("info eval "+state.moves.subList(starting_point, state.moves.size())+" | "+utility(state));
			return utility(state);
		}
		
		//Récursion
		int v = + INFINITY;
		for (Move successor: successors(state)) {
			state.apply(successor.move);
			v = Math.min(v, maxvalue(state, alpha, beta, depth-1));
			//System.out.println("info min "+state.moves.subList(starting_point, state.moves.size())+" "+v);
			state.revert();
			if (v <= alpha) return v;
			beta = Math.min(beta, v);
		}
		return v;
	}

}
