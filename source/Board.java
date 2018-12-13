import java.util.List;
import java.util.ArrayList;
/** 
 * Plateau de jeu.
 * Fonctionne avec des bitboards.
 */
public class Board extends Bitboards {

    //TODO : améliorer le clonage //

    /** Couleur */
    public boolean white;

    /**
     * Crée un nouveau plateau de jeu.
     * A voir comment on fera pour éviter de créer trop d'instance de Board pour les noeuds et si c'est couteux.
     */
    public Board(boolean is_white) {
        init(is_white);   
    }

    /**
     * Clone le board.
     */
    public Board clone() {
        Board b = new Board(this.white);
        b.position(this.last_move);
        return b;
    }

    public MoveGenerator generator;

    /**
     * Retourne le meilleur move à jouer
     */
    public void bestmove() {
        System.out.println("bestmove "+BestMove.compute(this));
    }

    /**
     * Initialise les bitboards à la position initiale.
     */
    public void init(boolean is_white) {
        //Initialisation du plateau
        startpos();

        // Initialisation de la couleur
        white = is_white;

        // Initialisation des directions
        set_direction(white);

        generator =  new MoveGenerator(this);
    }

    /**
     * Retourne un bit board representant les cases occupés par les pieces blanches
     */
    public long white_occupancy() {
        return bb_wp | bb_wr | bb_wn | bb_wb | bb_wq | bb_wk;
    }

    /**
     * Retourne un bit board representant les cases occupés par les pieces noires
     */
    public long black_occupancy() {
        return bb_bp | bb_br | bb_bn | bb_bb | bb_bq | bb_bk;
    }

    /**
     * Retourne un bit board representant les cases occupés par toutes les pieces
     */
    public long global_occupancy() {
        return white_occupancy() | black_occupancy();
    }

    /**
     * Retourne un bit board representant les cases vides du plateau
     */
    public long get_empty() {
        return ~global_occupancy();
    }

    /**
     * @param white - Couleur des pions souhaité
     * Retourne un bit board representant les cases occupés par tout les pions d'une couleur
     */
    public long get_pawns(boolean white) { return (white) ? bb_wp : bb_bp; }


    /**
     * @param white - Couleur du camp du moteur
     * Set les directions en fonction de notre couleur
     */
    public void set_direction(boolean white) {
    }

    /**
     * @param white - Couleur du camp du moteur
     * Retourne l'indice la position du roi
     */
    public int get_king_position(boolean white) {
        long king_pawns = (white) ? bb_wk : bb_bk;
        return Long.numberOfTrailingZeros(king_pawns);
    }

    /**
     * Indique si la piece est flag
     * @param piece
     */
    public void print_isflagged(String test) {
        char piece = test.charAt(0);
        long origin = VOID;
        if (test.length() == 2) {
            if (piece == WHITE_ROOK) origin = test.charAt(1) == 'a' ? A1 : H1;
            if (piece == BLACK_ROOK) origin = test.charAt(1) == 'a' ? A8 : H8;
        }
        System.out.println(flagged(piece, origin) ? "yes" : "no");
    }

    /**
     * Indique si un unique mouvement est légal
     * @param move
     */
    public void print_islegal(String move) {
        List<Move> lmoves = generator.get_legal_moves(color(at(fromUCI(move.substring(0, 2)))));
        List<String> slmoves = new ArrayList<>();
        for (Move lmove : lmoves) {
            slmoves.add(Board.toUCI(lmove));
        }
        System.out.println(slmoves.contains(move) ? "yes" : "no");
    }

    /**
     * Affiche tous les mouvements légaux
     */
    public void print_legal() {
        List<Move> lmoves = generator.get_legal_moves(WHITE);
        System.out.println("info legal moves (WHITE) : ");
        StringBuilder sb = new StringBuilder();
        for (Move lmove : lmoves) {
            sb.append(at(1L << (lmove.move & MOVE_FROM))+"-"+Board.toUCI(lmove)+" ");
        }
        System.out.println(sb.toString());
        lmoves = generator.get_legal_moves(BLACK);
        System.out.println("info legal moves (BLACK) : ");
        sb = new StringBuilder();
        for (Move lmove : lmoves) {
            sb.append(at(1L << (lmove.move & MOVE_FROM))+"-"+Board.toUCI(lmove)+" ");
        }
        System.out.println(sb.toString());
    }

    /**
     * Affiche le tour du jeu
     */
    public void print_turn() {
        System.out.println("info turn : "+(player_turn() == WHITE ? "WHITE" : "BLACK"));
    }

    /**
     * Affiche le score utilité
     */
    public void print_utility() {
        int u = BestMove.utility(this);
        System.out.println("Score : "+u+(u>=0 ? " (WHITE)" : " (BLACK)"));
        System.out.println("");
    }
}