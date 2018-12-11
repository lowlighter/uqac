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
import java.util.concurrent.Callable;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashMap;


/**
 * Contient les constantes pré-calculées pour les cases et types de pièces.
 */
public abstract class BestMove{

    /** Nombre de threads. */
    static int THREAD = 8;

    /** Temps max d'exécution des threads (en ms) */
    static int TIMEOUT = 700;

    /** Profondeur max */
    static int MAXDEPTH = 1000;

    /** Pool de threads. */
    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD);


    public static void init() {
        for (int i = 0; i < THREAD; i++) { 
            executor.submit(() -> { return null; });
        }
        System.out.println("info number of threads : "+executor.getActiveCount());
    }

    /**
     * Retourne le meilleur move.
     * @param state Plateau
     * @param depth Profondeur
     * @return
     */
    public static String compute(Board state) {

        long time = System.currentTimeMillis();

        //Appel des threads
        Map<BestMoveThread, Future<String>> futures = new HashMap<>();
        for (int i = 0; i < THREAD; i++) { 
            BestMoveThread callable = new BestMoveThread(state.clone());
            Future<String> future = executor.submit(callable);
            callable.future = future;
            futures.put(callable, future);
        }

        //Meilleur coup
        String best_move = "";
        int best_score = -1;
    
        //Récupération de leur valeurs après le temps imparti
        for(Map.Entry<BestMoveThread, Future<String>> entry : futures.entrySet()){
            Future<String> future = entry.getValue();
            BestMoveThread callable = entry.getKey();
        
            try { future.get(TIMEOUT, TimeUnit.MILLISECONDS); } 
            catch (TimeoutException e) { future.cancel(true); }
            catch (Exception e) { future.cancel(true); e.printStackTrace(); }

            String[] move = callable.best.split("@");
            if (move[0].length() > 0) {
                int score = Integer.parseInt(move[1]);
                System.out.println("info thread "+callable.id+" found : "+move[0]+" ("+score+")");
                if (score > best_score) {
                    best_score = score;
                    best_move = move[0];
                }
            }
        }

        System.out.println("info selected best move : "+best_move+" [score : "+best_score+" | time "+(System.currentTimeMillis()-time)+" ms]");
        return best_move;
    }

    /**
	 * Indique s'il s'agit d'un état terminal
	 * @param state Plateau
	 */
	public static boolean terminal(Board state) {
		return false;
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