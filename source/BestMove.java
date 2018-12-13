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
public abstract class BestMove extends Constants {

    /** Nombre de threads. */
    static int THREAD = 7;

    /** Temps max d'exécution des threads (en ms) */
    static int TIMEOUT = 900;

    /** Profondeur max */
    static int MAXDEPTH = 10000;

    /** Pool de threads. */
    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Math.max(THREAD, 1));

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
        
        //Threads principal
        BestMoveThread mthread = new BestMoveThread(state.clone());
        try {
            mthread.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        threads.add(mthread);

        //Meilleur coup
        String best_move = "????";
        int best_score = -1;
    
        //Récupération de leur valeurs après le temps imparti
        for(BestMoveThread thread : threads) {
            String[] move = thread.best().split("@");
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
		return false || timeout();
    }
    
    public static boolean timeout() {
        return (System.currentTimeMillis() - time >= TIMEOUT);
    }

	/**
	 * Retourne la valeur d'utilité de l'état
	 * @param state Plateau
	 */
	public static int utility(Board state) {

        int score = 0;

        for (int c = 0; c < 64; c++) {
            char piece = state.at(1L << c);
            if (piece == EMPTY) continue;
            int color = Character.isLowerCase(piece) ? -1 : +1; 
            char p = Character.toLowerCase(piece);

            
            score += color * ut_val.get(p);
            score += color * ut_mob.get(p)[7- ~~(c/8)][Math.min(c%8, 7-c%8)];
        }

            
        return score;
        //bonus[i][7 - square.y][Math.min(square.x, 7 - square.x)];
        
        //King danger
	}
    

    /**
     * Instantie les threads et autre trucs utilitaires.
     */
    public static void init() {
        //Threads
        for (int i = 0; i < THREAD; i++) { 
            executor.submit(() -> { return null; });
        }

        ut_val.put(ANY_PAWN, 136);
        ut_val.put(ANY_KNIGHT, 782);
        ut_val.put(ANY_BISHOP, 830);
        ut_val.put(ANY_ROOK, 1289);
        ut_val.put(ANY_QUEEN, 2529);
        ut_val.put(ANY_KING, 0);
    
        ut_mob.put(ANY_PAWN, new Integer[][]{{0,0,0,0},{-11,7,7,17},{-16,-3,23,23},{-14,-7,20,24},{-5,-2,-1,12},{-11,-12,-2,4},{-2,20,-10,-2},{0,0,0,0}});
        ut_mob.put(ANY_KNIGHT, new Integer[][]{{-169,-96,-80,-79},{-79,-39,-24,-9},{-64,-20,4,19},{-28,5,41,47},{-29,13,42,52},{-11,28,63,55},{-67,-21,6,37},{-200,-80,-53,-32}});
        ut_mob.put(ANY_BISHOP, new Integer[][]{{-49,-7,-10,-34},{-24,9,15,1},{-9,22,-3,12},{4,9,18,40},{-8,27,13,30},{-17,14,-6,6},{-19,-13,7,-11},{-47,-7,-17,-29}});
        ut_mob.put(ANY_ROOK, new Integer[][]{{-24,-15,-8,0},{-18,-5,-1,1},{-19,-10,1,0},{-21,-7,-4,-4},{-21,-12,-1,4},{-23,-10,1,6},{-11,8,9,12},{-25,-18,-11,2}});
        ut_mob.put(ANY_QUEEN, new Integer[][]{{3,-5,-5,4},{-3,5,8,12},{-3,6,13,7},{4,5,9,8},{0,14,12,5},{-4,10,6,8},{-5,6,10,8},{-2,-2,1,-2}});
        ut_mob.put(ANY_KING, new Integer[][]{{272,325,273,190},{277,305,241,183},{198,253,168,120},{169,191,136,108},{145,176,112,69},{122,159,85,36},{87,120,64,25},{64,87,49,0}});
    

    }

    static Map<Character, Integer> ut_val = new HashMap<>();

    static Map<Character, Integer[][]> ut_mob = new HashMap<>();
    

}

