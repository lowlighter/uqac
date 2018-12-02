/**
 * Contient les constantes pré-calculées pour les cases et types de pièces.
 */
public abstract class BoardConstants {
    
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

    /** Pièces. */
    protected static final char EMPTY = '.';
    protected static final char WHITE_PAWN = 'P';
    protected static final char WHITE_ROOK = 'R';
    protected static final char WHITE_KNIGHT = 'N';
    protected static final char WHITE_BISHOP = 'B'; 
    protected static final char WHITE_QUEEN = 'Q';
    protected static final char WHITE_KING = 'K';
    protected static final char BLACK_PAWN = 'p';
    protected static final char BLACK_ROOK = 'r';
    protected static final char BLACK_KNIGHT = 'n';
    protected static final char BLACK_BISHOP = 'b'; 
    protected static final char BLACK_QUEEN = 'q';
    protected static final char BLACK_KING = 'k';
    
    /** Cases du plateau. */
    protected static final long A1 = (1 << 0); 
    protected static final long B1 = (1 << 1); 
    protected static final long C1 = (1 << 2); 
    protected static final long D1 = (1 << 3); 
    protected static final long E1 = (1 << 4); 
    protected static final long F1 = (1 << 5); 
    protected static final long G1 = (1 << 6); 
    protected static final long H1 = (1 << 7);
    protected static final long A2 = (1 << 8); 
    protected static final long B2 = (1 << 9); 
    protected static final long C2 = (1 << 10); 
    protected static final long D2 = (1 << 11); 
    protected static final long E2 = (1 << 12); 
    protected static final long F2 = (1 << 13); 
    protected static final long G2 = (1 << 14); 
    protected static final long H2 = (1 << 15);
    protected static final long A3 = (1 << 16); 
    protected static final long B3 = (1 << 17); 
    protected static final long C3 = (1 << 18); 
    protected static final long D3 = (1 << 19); 
    protected static final long E3 = (1 << 20); 
    protected static final long F3 = (1 << 21); 
    protected static final long G3 = (1 << 22); 
    protected static final long H3 = (1 << 23);
    protected static final long A4 = (1 << 24); 
    protected static final long B4 = (1 << 25); 
    protected static final long C4 = (1 << 26); 
    protected static final long D4 = (1 << 27); 
    protected static final long E4 = (1 << 28); 
    protected static final long F4 = (1 << 29); 
    protected static final long G4 = (1 << 30); 
    protected static final long H4 = (1 << 31);
    protected static final long A5 = (1 << 32); 
    protected static final long B5 = (1 << 33); 
    protected static final long C5 = (1 << 34); 
    protected static final long D5 = (1 << 35); 
    protected static final long E5 = (1 << 36); 
    protected static final long F5 = (1 << 37); 
    protected static final long G5 = (1 << 38); 
    protected static final long H5 = (1 << 39);
    protected static final long A6 = (1 << 40); 
    protected static final long B6 = (1 << 41); 
    protected static final long C6 = (1 << 42); 
    protected static final long D6 = (1 << 43); 
    protected static final long E6 = (1 << 44); 
    protected static final long F6 = (1 << 45); 
    protected static final long G6 = (1 << 46); 
    protected static final long H6 = (1 << 47);
    protected static final long A7 = (1 << 48); 
    protected static final long B7 = (1 << 49); 
    protected static final long C7 = (1 << 50); 
    protected static final long D7 = (1 << 51); 
    protected static final long E7 = (1 << 52); 
    protected static final long F7 = (1 << 53); 
    protected static final long G7 = (1 << 54); 
    protected static final long H7 = (1 << 55);
    protected static final long A8 = (1 << 56); 
    protected static final long B8 = (1 << 57); 
    protected static final long C8 = (1 << 58); 
    protected static final long D8 = (1 << 59); 
    protected static final long E8 = (1 << 60); 
    protected static final long F8 = (1 << 61); 
    protected static final long G8 = (1 << 62); 
    protected static final long H8 = (1 << 63);


    /** Lignes */

    public static final long ROW_1 = 0b0000000000000000000000000000000000000000000000000000000011111111L;
    public static final long ROW_2 = 0b0000000000000000000000000000000000000000000000001111111100000000L;
    public static final long ROW_3 = 0b0000000000000000000000000000000000000000111111110000000000000000L;
    public static final long ROW_4 = 0b0000000000000000000000000000000011111111000000000000000000000000L;
    public static final long ROW_5 = 0b0000000000000000000000001111111100000000000000000000000000000000L;
    public static final long ROW_6 = 0b0000000000000000111111110000000000000000000000000000000000000000L;
    public static final long ROW_7 = 0b0000000011111111000000000000000000000000000000000000000000000000L;
    public static final long ROW_8 = 0b1111111100000000000000000000000000000000000000000000000000000000L;

    /** Colonne  */
    
    public static final long COLUMN_A = 0x101010101010101L;
    public static final long COLUMN_B = 0x202020202020202L;
    public static final long COLUMN_C = 0x404040404040404L;
    public static final long COLUMN_D = 0x808080808080808L;
    public static final long COLUMN_E = 0x1010101010101010L;
    public static final long COLUMN_F = 0x2020202020202020L;
    public static final long COLUMN_G = 0x4040404040404040L;
    public static final long COLUMN_H = 0x8080808080808080L;

    /** Couleur */

    public static boolean white;

    /** Direction */

    public long NORTH = 8;
    public long SOUTH = 8;
    public long EAST = 1;
    public long WEST = 1;

    public long NORTH_WEST = NORTH + WEST;
    public long NORTH_EAST = NORTH + EAST;
    public long SOUTH_WEST = SOUTH + WEST;
    public long SOUTH_EAST = SOUTH + EAST;

}
