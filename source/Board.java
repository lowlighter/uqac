/** 
 * Plateau de jeu.
 * Fonctionne avec des bitboards.
 */
public class Board extends BoardConstants {

    /**
     * Crée un nouveau plateau de jeu.
     * A voir comment on fera pour éviter de créer trop d'instance de Board pour les noeuds et si c'est couteux.
     */
    public Board(boolean is_white) {
        init(is_white);
        MoveGenerator test = new MoveGenerator(this);
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
    }

    /**
     * Met la configuration du plateau à son état initial.
     */
    public void startpos() {
        System.out.println("info applying startpos (ignore)");

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
     * @param from - Départ
     * @param to - Arrivé
     */
    public void move(long from, long to) {
        char piece = at(from);
        if (to != VOID) move(to, VOID);
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
     * Déplace une pièce sur le bitboard associé.
     * La légalité du coup n'est pas vérifiée !
     * @param move - Coup (notation UCI)
     */
    public void move(String move) {
        System.out.println("info applying move "+move+" (ignore)");
        //Déplacement
        move(fromUCI(move.substring(0, 2)), fromUCI(move.substring(2, 4)));
        //Promotion
        if (move.length() == 5) promote(fromUCI(move.substring(2, 4)), move.charAt(4));
    }

    /**
     * Promotion d'un pion.
     * @param cell - Case où se situe le pion
     * @param piece - Pièce où 
     */
    public void promote(long cell, char piece) {
        //Récupération du pion et suppression de celui-ci
        System.out.println("info applying promotion ("+piece+") (ignore)");
        char pawn = at(cell);
        move(cell, VOID);
        
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
     * Retourne le contenu de la case indiquée.
     * @param cell
     */
    private char at(long cell) {
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
    String toUCI(long cell) {
        short i = 0;
        for (; i < 64; i++)
            if (get(1L << i, cell)) break;
        return Character.toString((char)('a'+ (i%8))) + Character.toString((char)('0' + ~~(i/8) + 1));
    }

    /** 
     * Convertie une chaîne UCI en long.
     * @param uci Chaîne UCI
     */
    long fromUCI(String uci) {
        return 1L << ((uci.charAt(0) - 'a') + 8*(uci.charAt(1) - '0' - 1));
    }

}