/** 
 * Plateau de jeu.
 * Fonctionne avec des bitboards.
 */
public class Board extends BoardConstants {

    /**
     * Crée un nouveau plateau de jeu.
     * A voir comment on fera pour éviter de créer trop d'instance de Board pour les noeuds et si c'est couteux.
     */
    public Board() {
        init();
        move(WHITE_PAWN, A2, A4);
    }

    /**
     * Initialise les bitboards à la position initiale.
     */
    public void init() {
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
     * Déplace une pièce sur le bitboard associé.
     * La légalité du coup n'est pas vérifiée !
     * @param piece - Pièce
     * @param from - Départ
     * @param to - Arrivé
     */
    public void move(char piece, long from, long to) {
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
     * Met à 1 le bit d'une case.
     * @param bitboard - Bitboard
     * @param cell - Nom de la cellule
     */
    private long set(long bitboard, long cell) {
        return bitboard | cell;
    }

    /** 
     * Récupère la valeur du bit d'une case.
     * @param bitboard - Bitboard
     * @param cell - Nom de la cellule
    */
    private boolean get(long bitboard, long cell) {
        return (bitboard & cell) != 0;
    }

    /**
     * Met à 0 le bit d'une case.
     * @param bitboard - Bitboard
     * @param cell - Nom de la cellule
     */
    private long clear(long bitboard, long cell) {
        return bitboard & ~cell;
    }

    /**
     * Met à 0 le bit de la case a et met à 1 le bit de la case b
     * @param bitboard - Bitboard
     * @param a - Nom de la cellule a
     * @param b - Nom de la cellule b
     */
    private long clearset(long bitboard, long a, long b) {
        return set(clear(bitboard, a), b);
    }

    /**
     * Affiche le plateau de jeu.
     */
    public void print() {
        StringBuilder b = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file <= 7; file++) {
                //Formattage
                long i = 1L << (rank*8 + file);
                if ((rank == 7)&&(file == 0)) { b.append("   a b c d e f g h\n  +----------------\n"); }
                if (file == 0) { b.append((rank+1)+" |"); }
                b.append(" ");
                //Pièce
                if (get(bb_wp, i)) { b.append(WHITE_PAWN); continue; }
                if (get(bb_wr, i)) { b.append(WHITE_ROOK); continue; }
                if (get(bb_wn, i)) { b.append(WHITE_KNIGHT); continue; }
                if (get(bb_wb, i)) { b.append(WHITE_BISHOP); continue; }
                if (get(bb_wq, i)) { b.append(WHITE_QUEEN); continue; }
                if (get(bb_wk, i)) { b.append(WHITE_KING); continue; }
                if (get(bb_bp, i)) { b.append(BLACK_PAWN); continue; }
                if (get(bb_br, i)) { b.append(BLACK_ROOK); continue; }
                if (get(bb_bn, i)) { b.append(BLACK_KNIGHT); continue; }
                if (get(bb_bb, i)) { b.append(BLACK_BISHOP); continue; }
                if (get(bb_bq, i)) { b.append(BLACK_QUEEN); continue; }
                if (get(bb_bk, i)) { b.append(BLACK_KING); continue; }
                b.append(EMPTY);
            }
            b.append("\n");
        }
        System.out.println(b.toString());
    }

}
