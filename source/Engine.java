//Paquet et dépendances

import java.util.Scanner;

/**
 * Moteur de jeu d'échec.
 */
public class Engine {

  /**
   * Point d'entrée de l'application.
   * @param args - arguments en ligne de commande
   */
    public static void main(String[] args) {
      
        //Flux d'entrée
        Scanner stdin = new Scanner(System.in);
        board = new Board(false);

        BestMove.init();

        //Boucle principale
        while (true) {

            //Récupération de la prochaine entrée (envoyée par Arena ou CLI)
            String in = stdin.nextLine();

            //Traitement de la commande
            if ("uci".equals(in)) { uci(); }
            else if ("isready".equals(in)) { ready(); }
            else if ("ucinewgame".equals(in)) { ucinewgame(); }
            else if (in.startsWith("position")) { position(in); }
            else if (in.startsWith("go")) { go(); }
            else if (in.startsWith("print")) { print(in); }
            else if (in.startsWith("setoption")) { setoption(in); }
            else if (in.equals("quit")) { stdin.close(); quit(); }
            
        }

    }

    /**
     * Affiche les informations sur le moteur de jeu.
     */
    private static void uci() {
        System.out.println("id name Kasparov");
        System.out.println("id author ???");
        System.out.println("uciok");
    }

    /**
     * Indique que le moteur de jeu est prêt.
     */
    private static void ready() {
        System.out.println("readyok");
    }

    /**
     * Méthode appelée lorsqu'une nouvelle partie d'échec démarre.
     */
    private static void ucinewgame() {
        board.init(true);
    }

    /** Plateau de jeu. */
    private static Board board;

    /**
     * Mise à jour de l'état du plateau.
     * @param in - état du plateau (e.g. : position startpos moves e2e4 e7e5 g1f3 b8c6)
     */
    private static void position(String in) {
        board.position(in);
    }

    /**
     * Affiche le prochain coup à jouer.
     * NB : la notation UCI requiert d'utiliser "bestmove " devant le coup à jouer.
     */
    private static void go() {
        board.bestmove();
    }

    /**
     * Affiche le plateau.
     */
    private static void print(String in) {
        String[] input = in.split(" ");
        if (input.length == 1) board.print();
        if ((input.length == 2)&&(input[1].equals("dominance"))) board.dominance();
    }

    /**
     * Ajoute une option pour le moteur de jeu.
     * @param in - option (e.g. : setoption name Nullmove value true)
     */
    private static void setoption(String in) {
    }

    /**
     * Quitte le programme. 
     */
    private static void quit() {
        System.exit(0);
    }


}






        