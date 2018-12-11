import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashMap;


/**
 * Contient les constantes pré-calculées pour les cases et types de pièces.
 */
public abstract class BestMove{

    /** Nombre de threads. */
    static int THREAD = 7;

    /** Temps max d'exécution des threads (en ms) */
    static int TIMEOUT = 700;

    /** Profondeur max */
    static int MAXDEPTH = 10000;

    /** Pool de threads. */
    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD);


    /**
     * Instantie les threads.
     */
    public static void init() {
        for (int i = 0; i < THREAD; i++) { 
            executor.submit(() -> { return null; });
        }
    }

    /** Threads instantiés lors du computing. */
    static private List<BestMoveThread> threads = new ArrayList<>();

    /**
     * Retourne le meilleur move.
     * @param state Plateau
     * @param depth Profondeur
     * @return
     */
    public static String compute(Board state) {

        //Appel des threads
        time = System.currentTimeMillis();
        threads.clear();
        for (int i = 0; i < THREAD; i++) { 
            BestMoveThread thread = new BestMoveThread(state.clone());
            threads.add(thread);
            executor.submit(thread);
        }
        try { Thread.sleep(TIMEOUT); } catch (Exception e) {}

        //Meilleur coup
        String best_move = "";
        int best_score = -1;
    
        //Récupération de leur valeurs après le temps imparti
        for(BestMoveThread thread : threads) {
            String[] move = thread.best.split("@");
            if (move[0].length() > 0) {
                int score = Integer.parseInt(move[1]);
                System.out.println("info thread "+thread.id+" found : "+move[0]+" (valued at "+score+") (ignore)");
                if (score > best_score) {
                    best_score = score;
                    best_move = move[0];
                }
            }
        }

        System.out.println("info selected best move : "+best_move+" [score : "+best_score+" | time "+(System.currentTimeMillis()-time)+" ms] (ignore)");
        return best_move;
    }

    /** Temps de départ. */
    static long time = 0;

    /**
	 * Indique s'il s'agit d'un état terminal ou si le temps a été dépassé.
	 * @param state Plateau
	 */
	public static boolean terminal(Board state) {
		return false || (System.currentTimeMillis() - time >= TIMEOUT);
	}

	/**
	 * Retourne la valeur d'utilité de l'état
	 * @param state Plateau
	 */
	public static int utility(Board state) {
		return 0
		+ 1 * Long.bitCount(state.bb_wp)
		+ 5 * Long.bitCount(state.bb_wr)
		+ 3 * Long.bitCount(state.bb_wn)
		+ 3 * Long.bitCount(state.bb_wb)
		+ 9 * Long.bitCount(state.bb_wq)
		+ 1000 * Long.bitCount(state.bb_wk)
		- 1 * Long.bitCount(state.bb_wp)
		- 5 * Long.bitCount(state.bb_br)
		- 3 * Long.bitCount(state.bb_bn)
		- 3 * Long.bitCount(state.bb_bb)
		- 9 * Long.bitCount(state.bb_bq)
        - 1000 * Long.bitCount(state.bb_bk)
		;
	}
    

}