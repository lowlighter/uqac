import java.util.ArrayList;
import java.util.List;

/**
 * Méthodes pe
 */
public abstract class Bitboards extends Constants {

    /** Bitboard pions blancs. */
    public long bb_wp;
    /** Bitboard tour blancs. */
    public long bb_wr;
    /** Bitboard cavalier blancs. */
    public long bb_wn;
    /** Bitboard fou blancs. */
    public long bb_wb;
    /** Bitboard reine blancs. */
    public long bb_wq;
    /** Bitboard roi blancs. */
    public long bb_wk;

    /** Bitboard pions noirs. */
    public long bb_bp;
    /** Bitboard tour noirs. */
    public long bb_br;
    /** Bitboard cavalier noirs. */
    public long bb_bn;
    /** Bitboard fou noirs. */
    public long bb_bb;
    /** Bitboard reine noirs. */
    public long bb_bq;
    /** Bitboard roi noirs. */
    public long bb_bk;

    /** Liste des moves (DEBUG). */
    List<String> moves = new ArrayList<>();
    /** Liste des cases d'origines. */
    private List<Long> moves_from = new ArrayList<>();
    /** Liste des cases de déplacements. */
    private List<Long> moves_to = new ArrayList<>();
    /** Liste des pièces qui ont fait le déplacement. */
    private List<Character> moves_piece = new ArrayList<>();
    /** Liste des pièces qui ont été prises. */
    private List<Character> moves_taken = new ArrayList<>();
    /** Liste des promotions qui ont eu lieu. */
    private List<Character> moves_promoted = new ArrayList<>();
    /** Liste des flags déclenché par un déplacement (e.g. le déplacement des tours pour le roque) */
    private List<Long> moves_flags = new ArrayList<>();

    /**
     * Indique si la piece est flaggé
     * Ici, cela indique si la pièce a effectué 
     * @param piece
     * @param origin
     * @return
     */
    public boolean flagged(char piece, long origin) {
        if ((piece == WHITE_ROOK)||(piece == BLACK_ROOK))
            return moves_flags.indexOf(origin) >= 0;
        if ((piece == WHITE_KING)||(piece == BLACK_KING))
            return moves_piece.indexOf(piece) >= 0;
        return false;
    }
    
    /** Notation du dernier coup joué (en entier tel qu'il est passé dans arena). */
    protected String last_move = "";

    /**
     * Met à 1 le bit d'une case.
     * @param bitboard Bitboard
     * @param cell Nom de la cellule
     */
    private long set(long bitboard, long cell) {
        return bitboard | cell;
    }

    /** 
     * Récupère la valeur du bit d'une case.
     * @param bitboard Bitboard
     * @param cell Nom de la cellule
    */
    private boolean get(long bitboard, long cell) {
        return (bitboard & cell) != 0;
    }

    /**
     * Met à 0 le bit d'une case.
     * @param bitboard Bitboard
     * @param cell Nom de la cellule
     */
    private long clear(long bitboard, long cell) {
        return bitboard & ~cell;
    }

    /**
     * Met à 0 le bit de la case a et met à 1 le bit de la case b
     * @param bitboard Bitboard
     * @param a Nom de la cellule a
     * @param b Nom de la cellule b
     */
    private long clearset(long bitboard, long a, long b) {
        return set(clear(bitboard, a), b);
    }


    /** 
     * Met à jour la configuration du plateau
     * @param in Liste de déplacements
     */
    public void position(String in) {
        last_move = in;
        String[] input = in.split(" ");
        for (int i = 0; i < input.length; i++) {
            if (input[i].equals("moves")) continue;
            if (input[i].equals("startpos")) { startpos(); continue; }
            if (input[i].matches("[a-h][1-8][a-h][1-8][rnbq]?")) apply(input[i]);
        }
    }

    /**
     * Réinitialise la configuration du plateau
     */
    protected void startpos() {
        //Nettoyage
        moves.clear();
        moves_from.clear();
        moves_to.clear();
        moves_piece.clear();
        moves_taken.clear();
        moves_promoted.clear();
        moves_flags.clear();

        //Initialisation des pièces blanches
        bb_wp = 0b0000000000000000000000000000000000000000000000001111111100000000L;
        bb_wr = 0b0000000000000000000000000000000000000000000000000000000010000001L;
        bb_wn = 0b0000000000000000000000000000000000000000000000000000000001000010L;
        bb_wb = 0b0000000000000000000000000000000000000000000000000000000000100100L;
        bb_wq = 0b0000000000000000000000000000000000000000000000000000000000001000L;
        bb_wk = 0b0000000000000000000000000000000000000000000000000000000000010000L;

        //Initialisation des pièces noires
        bb_bp = 0b0000000011111111000000000000000000000000000000000000000000000000L;
        bb_br = 0b1000000100000000000000000000000000000000000000000000000000000000L;
        bb_bn = 0b0100001000000000000000000000000000000000000000000000000000000000L;
        bb_bb = 0b0010010000000000000000000000000000000000000000000000000000000000L;
        bb_bq = 0b0000100000000000000000000000000000000000000000000000000000000000L;
        bb_bk = 0b0001000000000000000000000000000000000000000000000000000000000000L;
    }

    /**
     * Joue un coup.
     * La légalité du coup n'est pas vérifiée !
     * Le roque est géré directement selon le coup joué
     * @param move - Coup (notation UCI)
     */
    public void apply(long from, long to, char promoted) {
        //Analyse du coup
        char piece = at(from);
        char taken = at(to);
        long flag = VOID;

        //Gestion du roque 
        if (piece == WHITE_KING) {
            if ((from == E1)&&(to == G1)) 
                move(WHITE_ROOK, H1, F1);
            if ((from == E1)&&(to == C1))
                move(WHITE_ROOK, A1, D1);
        }
        if (piece == BLACK_KING) {
            if ((from == E8)&&(to == G8)) 
                move(BLACK_ROOK, H8, F8);
            if ((from == E8)&&(to == C8))
                move(BLACK_ROOK, A8, D8);
        }
        if ((piece == WHITE_ROOK)&&(from == A1)) flag = A1;
        if ((piece == WHITE_ROOK)&&(from == H1)) flag = H1;
        if ((piece == BLACK_ROOK)&&(from == A8)) flag = A8;
        if ((piece == BLACK_ROOK)&&(from == H1)) flag = H8;

        //Déplacement de la pièce (et prise de la pièce adverse)
        move(piece, from, to);
        move(taken, to, VOID);

        //Promotion
        if (promoted != PROMOTED_VOID) 
            promote(to, promoted);
        
        //Enregistrement
        moves.add(toUCI(from)+toUCI(to));
        moves_from.add(from);
        moves_to.add(to);
        moves_piece.add(piece);
        moves_taken.add(taken);
        moves_promoted.add(promoted);
        moves_flags.add(flag);
    }
    public void apply(String move) {
        apply(fromUCI(move.substring(0, 2)), fromUCI(move.substring(2, 4)), move.length() == 5 ? move.charAt(4) : PROMOTED_VOID);
    }
    public void apply(short move) {
        //Gestion de la promotion
        char promote = PROMOTED_VOID;
        if ((move & MOVE_PROMOTE) > 0) {
            if ((move & MOVE_PROMOTE_QUEEN) > 0) promote = PROMOTED_QUEEN;
            else if ((move & MOVE_PROMOTE_KNIGHT) > 0) promote = PROMOTED_KNIGHT;
            else if ((move & MOVE_PROMOTE_ROOK) > 0) promote = PROMOTED_ROOK;
            else if ((move & MOVE_PROMOTE_BISHOP) > 0) promote = PROMOTED_BISHOP;
        }
        //Application
        apply(1L << (move & MOVE_FROM), 1L << ((move & MOVE_TO) >> 6), promote);
    }

    /**
     * Indique le n° du tour.
     */
    public int turn() {
        return moves_piece.size();
    }

    /**
     * Indique la couleur du joueur qui doit jouer.
     */
    public boolean player_turn() {
        return turn()%2 == 0 ? WHITE : BLACK;
    }

    /**
     * Annule le dernier coup joué.
     */
    public void revert() {
        //Récupération des données du coup
        int index = turn()-1;
        long from = moves_from.get(index);
        long to = moves_to.get(index);
        char piece = moves_piece.get(index);
        char taken = moves_taken.get(index);
        char promoted = moves_promoted.get(index);
        
        //Annulation du déplacement (et de la prise)
        move(piece, to, from);
        move(taken, VOID, to);

        //Annulation du la promotion
        if (promoted != PROMOTED_VOID)
            demote(to, promoted);
    
        //Enregistrement
        moves.remove(index);
        moves_from.remove(index);
        moves_to.remove(index);
        moves_piece.remove(index);
        moves_taken.remove(index);
        moves_promoted.remove(index);
        moves_flags.remove(index);
    }

    /**
     * Déplace une pièce sur le bitboard associé.
     * @param piece Pièce
     * @param from Départ
     * @param to Arrivé
     */
    private void move(char piece, long from, long to) {
        switch (piece) {
            case WHITE_PAWN:{ bb_wp = clearset(bb_wp, from, to); return; }
            case WHITE_ROOK:{ bb_wr = clearset(bb_wr, from, to); return; }
            case WHITE_KNIGHT:{ bb_wn = clearset(bb_wn, from, to); return; }
            case WHITE_BISHOP:{ bb_wb = clearset(bb_wb, from, to); return; }
            case WHITE_QUEEN:{ bb_wq = clearset(bb_wq, from, to); return; }
            case WHITE_KING:{ bb_wk = clearset(bb_wk, from, to); return; }
            case BLACK_PAWN:{ bb_bp = clearset(bb_bp, from, to); return; }
            case BLACK_ROOK:{ bb_br = clearset(bb_br, from, to); return; }
            case BLACK_KNIGHT:{ bb_bn = clearset(bb_bn, from, to); return; }
            case BLACK_BISHOP:{ bb_bb = clearset(bb_bb, from, to); return; }
            case BLACK_QUEEN:{ bb_bq = clearset(bb_bq, from, to); return; }
            case BLACK_KING:{ bb_bk = clearset(bb_bk, from, to); return; }
        }
    }

    /**
     * Promotion d'un pion.
     * @param cell - Case où se situe le pion
     * @param piece - Promotion
     */
    private void promote(long cell, char piece) {
        //Récupération du pion et suppression de celui-ci
        char pawn = at(cell);
        move(pawn, cell, VOID);
        
        //Promotion
        switch(pawn) {
            case WHITE_PAWN:{
                switch (piece) {
                    case PROMOTED_ROOK:{ bb_wr = set(bb_wr, cell); return; }
                    case PROMOTED_KNIGHT:{ bb_wn = set(bb_wn, cell); return; }
                    case PROMOTED_BISHOP:{ bb_wb = set(bb_wb, cell); return; }
                    case PROMOTED_QUEEN:{ bb_wq = set(bb_wq, cell); return; }
                }
            }
            case BLACK_PAWN:{
                switch (piece) {
                    case PROMOTED_ROOK:{ bb_br = set(bb_br, cell); return; }
                    case PROMOTED_KNIGHT:{ bb_bn = set(bb_bn, cell); return; }
                    case PROMOTED_BISHOP:{ bb_bb = set(bb_bb, cell); return; }
                    case PROMOTED_QUEEN:{ bb_bq = set(bb_bq, cell); return; }
                }
            }
        }
    }

    /**
     * Rétrograde une pièce au rang de pion
     * @param cell
     * @param piece
     */
    private void demote(long cell, char piece) {
        //Récupération du pion et suppression de celui-ci
        move(piece, cell, VOID);

        //Demotion
        if (color(piece) == WHITE)
            bb_wp = set(bb_wp, cell);
        else
            bb_bp = set(bb_bp, cell);
    }

    /**
     * Retourne le contenu de la case indiquée.
     * @param cell
     */
    public char at(long cell) {
        if (get(bb_wp, cell)) return WHITE_PAWN;
        if (get(bb_wr, cell)) return WHITE_ROOK;
        if (get(bb_wn, cell)) return WHITE_KNIGHT;
        if (get(bb_wb, cell)) return WHITE_BISHOP;
        if (get(bb_wq, cell)) return WHITE_QUEEN;
        if (get(bb_wk, cell)) return WHITE_KING;
        if (get(bb_bp, cell)) return BLACK_PAWN;
        if (get(bb_br, cell)) return BLACK_ROOK;
        if (get(bb_bn, cell)) return BLACK_KNIGHT;
        if (get(bb_bb, cell)) return BLACK_BISHOP;
        if (get(bb_bq, cell)) return BLACK_QUEEN;
        if (get(bb_bk, cell)) return BLACK_KING;
        return EMPTY;
    }

    public char at(long cell, boolean lower) {
        return Character.toLowerCase(at(cell));
    }

    /**
     * Retourne la couleur de la pièce.
     * @param piece
     */
    public boolean color(char piece) {
        return Character.isUpperCase(piece);
    }

    /**
     * Affiche le plateau de jeu
     */
    public void print() {
        StringBuilder b = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file <= 7; file++) {
                //Formattage
                long i = 1L << (rank*8 + file);
                if ((rank == 7)&&(file == 0)) { b.append("    a b c d e f g h\n  +----------------\n"); }
                if (file == 0) { b.append((rank+1)+" |"); }
                b.append(" ");
                b.append(at(i));
            }
            b.append("\n");
        }
        System.out.println(b.toString());
    }

    /** 
     * Convertie un long en une chaîne UCI.
     * @param long Case
     */
    public static String toUCI(long cell) {
        short i = 0;
        for (; i < 64; i++)
            if (((1L << i) & cell) != 0) break;
        return Character.toString((char)('a'+ (i%8))) + Character.toString((char)('0' + ~~(i/8) + 1));
    }

    /**
     * Converti un coup en une chaîne UCI complète.
     * @param m Coup
     * @return
     */
    public static String toUCI(Move m) {
        short move = m.move;
        return toUCI(1L << (move & MOVE_FROM))+toUCI(1L << ((move & MOVE_TO) >> 6));
    }

    /** 
     * Convertie une chaîne UCI en long
     * @param uci Chaîne UCI
     */
    public static long fromUCI(String uci) {
        return 1L << ((uci.charAt(0) - 'a') + 8*(uci.charAt(1) - '0' - 1));
    }

    

}