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
    static int TIMEOUT = 1250;

    /** Profondeur max */
    static int MAXDEPTH = 100;

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
            if (thread.best() == null) continue;
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
            // pions
            if(piece == ANY_PAWN)
                score += color * pawn_mg(c,p,state);
            // pièces

            // roques

        }

        return score;
        //bonus[i][7 - square.y][Math.min(square.x, 7 - square.x)];
        
        //King danger
	}



    /**
     * calcule l'utilité d'un pion mid game
     * @param c position du pion
     * @param color couleur de la piece
     * @param state plateau
     * @return utilite du pion
     */
    private static int pawn_mg(int c, int color, Board state) {
	    int v = 0;
	    v -= isolated(c,color, state) ? 5 : 0;
	    v -= backward(c,color, state) ? 9 : 0;
	    v += connected(c,color, state) ? co_bonus(c,color, state) : 0;

	    return v;
    }

    /**
     * verifie si pion isolé
     * @param c position du pion
     * @param color couleur de la piece
     * @param state plateau
     * @return vrai si isolé
     */
    private static boolean isolated(int c, int color, Board state) {

        // verifie si pion a un pion allié dans une colonne adjacente
        int col = c%8;
        // si colonne A
        if(color == 1) {
            if(col == 0)
                for(int l = 0 ; l < 7 ; ++l) {
                    if(((0b1L << ((col+(8*l))+1)) & state.bb_wp) > 0) {
                        return false;
                    }
                }
                // ou H
            else if(col == 7)
                for(int l = 0 ; l < 7 ; ++l) {
                    if(((0b1L << ((col+(8*l))-1)) & state.bb_wp) > 0) {
                        return false;
                    }
                }
            else
                for(int l = 0 ; l < 7 ; ++l) {
                    if(((0b101L << ((col+(8*l))-1)) & state.bb_wp) > 0) {
                        return false;
                    }
                }
        }
        // pion noir
        else {
            if(col == 0)
                for(int l = 1 ; l < 8 ; ++l) {
                    if(((0b1L << ((col+(8*l))+1)) & state.bb_bp) > 0) {
                        return false;
                    }
                }
                // ou H
            else if(col == 7)
                for(int l = 1 ; l < 8 ; ++l) {
                    if(((0b1L << ((col+(8*l))-1)) & state.bb_bp) > 0) {
                        return false;
                    }
                }
            else
                for(int l = 1 ; l < 8 ; ++l) {
                    if(((0b101L << ((col+(8*l))-1)) & state.bb_bp) > 0) {
                        return false;
                    }
                }
        }
        return true;
    }

    /**
     * verifie si il a une case adjacente contenant un pion de meme couleur
     * @param c position du pion
     * @param color couleur de la piece
     * @param state plateau
     * @return vrai si pion allier dans une case adjacente
     */
    private static boolean backward(int c, int color, Board state) {
        // si pas de cases adj (bord adverse du plateau)
            //return sum(pos, backward);
        int col = c%8;
        int ligne = c/8;
        if(color == 1) {
            // check si il est derriere des pions de la meme couleur
            if(col == 0)
                for(int l = 0 ; l <= ligne ; ++l) {
                    if(((0b1L << ((col+(8*l))+1)) & state.bb_wp) > 0) {
                        return false;
                    }
                }
                // ou H
            else if(col == 7)
                for(int l = 0 ; l <= ligne ; ++l) {
                    if(((0b1L << ((col+(8*l))-1)) & state.bb_wp) > 0) {
                        return false;
                    }
                }
            else
                for(int l = 0 ; l <= ligne ; ++l) {
                    if(((0b101L << ((col+(8*l))-1)) & state.bb_wp) > 0) {
                        return false;
                    }
                }

            if(isolated(c,color,state)) return false;

            // verifie danger
            if(ligne > 1) {

                if(col == 0) {
                    if(((0b1L << ((c)-15)) & state.bb_bp) > 0) {
                        return true;
                    }
                }
                // ou H
                else if(col == 7) {
                    if(((0b1L << ((c)-17)) & state.bb_bp) > 0) {
                        return true;
                    }
                }
                else
                    if(((0b101L << ((c)-15)) & state.bb_bp) > 0) {
                        return true;
                    }
            }
            return false;
        }
        // pion noir
        else {
            // check si il est derriere des pions de la meme couleur
            if(col == 0)
                for(int l = 7 ; l >= ligne ; --l) {
                    if(((0b1L << ((col+(8*l))+1)) & state.bb_bp) > 0) {
                        return false;
                    }
                }
                // ou H
            else if(col == 7)
                for(int l = 7 ; l >= ligne ; --l) {
                    if(((0b1L << ((col+(8*l))-1)) & state.bb_bp) > 0) {
                        return false;
                    }
                }
            else
                for(int l = 7 ; l >= ligne ; --l) {
                    if(((0b101L << ((col+(8*l))-1)) & state.bb_bp) > 0) {
                        return false;
                    }
                }

            if(isolated(c,color,state)) return false;

            // verifie danger
            if(ligne < 6) {
                if(col == 0) {
                    if(((0b1L << ((c)+17)) & state.bb_wp) > 0) {
                        return true;
                    }
                }
                // ou H
                else if(col == 7) {
                    if(((0b1L << ((c)+15)) & state.bb_wp) > 0) {
                        return true;
                    }
                }
                else
                if(((0b101L << ((c)+15)) & state.bb_wp) > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * verifie si le pions est lié à d'autres
     * @param c position du pion
     * @param color couleur de la piece
     * @param state plateau
     * @return vrai si lié
     */
    private static boolean connected(int c, int color, Board state) {
	    if(supported(c,color,state) != 0 || phalanx(c,color,state)) return true;
	    return false;
    }

    /**
     * retourne le nombre de pions alliers qui supportent le pion c
     * @param c position du pion
     * @param state plateau
     * @return nombre de pions
     */
    private static int supported(int c, int color, Board state) {
        int col = c%8;
        if(color == 1) {
            if(col == 0)
                return ((((0b1L << ((c)+9)) & state.bb_wp) > 0)? 1 : 0);
            else if(col == 7)
                return ((((0b1L << ((c)+7)) & state.bb_wp) > 0)? 1 : 0);
            else
                return ((((0b1L << ((c)+7)) & state.bb_wp) > 0)? 1 : 0)
                        + ((((0b1L << ((c)+9)) & state.bb_wp) > 0)? 1 : 0);
        }
        else {
            if(col == 0)
                return ((((0b1L << ((c)-7)) & state.bb_bp) > 0)? 1 : 0);
            else if(col == 7)
                return ((((0b1L << ((c)-9)) & state.bb_bp) > 0)? 1 : 0);
            else
                return ((((0b1L << ((c)-7)) & state.bb_bp) > 0)? 1 : 0)
                        + ((((0b1L << ((c)-9)) & state.bb_bp) > 0)? 1 : 0) ;
        }
    }

    /**
     * verifie si un pion voisin est sur une case du meme rang
     * @param c position du pion
     * @param color couleur de la piece
     * @param state plateau
     * @return booleen
     */
    private static boolean phalanx(int c, int color, Board state) {
        int col = c%8;
        if(color == 1) {
            if(col != 0 && (((0b1L << ((c)-1)) & state.bb_wp) > 0))
                return true;
            if(col != 7 && (((0b1L << ((c)+1)) & state.bb_wp) > 0))
                return true;
        }
        else {
            if(col != 0 && (((0b1L << ((c)-1)) & state.bb_bp) > 0))
                return true;
            if(col != 7 && (((0b1L << ((c)+1)) & state.bb_bp) > 0))
                return true;
        }
        return false;
    }

    /**
     * calcule le bonus en fonction des liaisons du pion
     * @param c position du pion
     * @param color couleur de la piece
     * @param state plateau
     * @return bonus pour le pion
     */
    private static int co_bonus(int c, int color, Board state) {

        int ligne = c%8;

        if(ligne < 1 || ligne > 6) return 0;
        int v = 0;
        // blanc
        if(color == 1) {
            int bonus[] = {330, 175, 100, 65, 18, 24, 13, 0};
            v = bonus[ligne];
            v += ( phalanx(c,color,state) ? (bonus[ligne-1] - bonus[ligne])>>1 : 0);
            v >>= opposed(c, color, state);
            v += 17*supported(c,color,state);
        }
        // noir
        else {
            int bonus[] = {0, 13, 24, 18, 65, 100, 175, 330};
            v = bonus[ligne];
            v += ( phalanx(c,color,state) ? (bonus[ligne+1] - bonus[ligne])>>1 : 0);
            v >>= opposed(c, color, state);
            v += 17*supported(c,color,state);
        }

        return v;
    }

    /**
     * indique si un pion adverse se trouve devant
     * @param c position du pion
     * @param color couleur de la piece
     * @param state plateau
     * @return 0 si faux, 1 si vrai
     */
    private static int opposed(int c, int color, Board state) {
        int ligne = c/8;
        int col = c%8;

        if(color == 1) {
            for(int l = 0; l < ligne; ++l)
                if(((0b1L << (col+(l*8))) & state.bb_bp) > 0) return 1;
        }
        else {
            for(int l = 7; l > ligne; --l)
                if(((0b1L << (col+(l*8))) & state.bb_wp) > 0) return 1;
        }
        return 0;
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

