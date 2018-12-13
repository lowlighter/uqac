import java.util.SortedSet; 
import java.util.TreeSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.Arrays;
import java.util.Collections;

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

	private int nbthreads = BestMove.THREAD+1;

	/**
	 * Appel du thread.
	 * Recherche par alphabeta le meilleur coup.
	 * Celui-ci est formatté de la façon suivante : uci@score (afin que le thread principal puisse déterminer le meilleur coup)
	 */
  public void run() {
		this.id = ((int)Thread.currentThread().getId()) % nbthreads;
		alphabeta(state);
	}

	/** Meilleur coup trouvé jusqu'à présent. */
	private String _best = "@0";

	public String best() {
		//System.out.println("info thread "+id+" : "+_best+" (ignore)");
		return _best;
	}

	private int _best_score;

	private void best(Move successor, int score) {
		int lscore = Math.abs(score);
		if ((successor != null)&&(lscore < INFINITY)&&((lscore > _best_score)||(_best == null))&&(!BestMove.timeout())) {
			_best_score = lscore;
			_best = Board.toUCI(successor)+"@"+lscore;
		}
		if (successor == null) {
			_best_score = 0;
			_best = null;
		}
	}

	/** Id du thread. */
	public int id;

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
		long time = System.currentTimeMillis();
		int v = 0; 
		Move lsuccessor;
		starting_point = state.moves.size();
		List<Move> lsuccessors = successors(state);
		
		Collections.shuffle(lsuccessors);

		String info = "";
		//Alpha beta (pour les blancs, il s'agit d'une version modifiée du maxvalue qui retient en mémoire le meilleur coup à jouer)
		if (state.player_turn() == Board.WHITE) {
			for (int depth = 1; depth <= MAXDEPTH; depth++) {
				//Récursion
				v = - INFINITY;
				int alpha = -INFINITY, beta = +INFINITY;
				for (int i = 0; i < lsuccessors.size(); i++) {  
					lsuccessor = lsuccessors.get(i);                                      
					state.apply(lsuccessor.move);
					v = Math.max(v, minvalue(state, alpha, beta, depth-1));
					state.revert();
					best(lsuccessor, v);
					alpha = Math.max(alpha, v);
				}
				info = "info thread "+id+" : depth "+depth+" (completed in "+(System.currentTimeMillis()-time)+" ms) (ignore)";
				if (BestMove.timeout()) break;
			}
		}
		//Alpha beta (pour les noirs, il s'agit d'une version modifiée du minvalue qui retient en mémoire le meilleur coup à jouer)
		else {
			for (int depth = 1; depth <= MAXDEPTH; depth++) {
				//Récursion
				v = + INFINITY;
				int alpha = -INFINITY, beta = +INFINITY;
				for (int i = 0; i < lsuccessors.size(); i++) {    
					lsuccessor = lsuccessors.get(i);   
					state.apply(lsuccessor.move);      
					v = Math.min(v, maxvalue(state, alpha, beta, depth-1));
					state.revert();
					best(lsuccessor, v);
					beta = Math.min(beta, v);
				}
				info = "info thread "+id+" : depth "+depth+" (completed in "+(System.currentTimeMillis()-time)+" ms) (ignore)";
				if (BestMove.timeout()) break;
			}
		}

		System.out.println(info);
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
			return BestMove.timeout() ? +INFINITY : utility(state);
		}
		
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
		if ((terminal(state))||(depth < 0)) {
			return BestMove.timeout() ? -INFINITY : utility(state);
		}
		
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
