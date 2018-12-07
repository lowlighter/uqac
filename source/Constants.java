/**
 * Contient les constantes pré-calculées pour les cases et types de pièces.
 */
public abstract class Constants {

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
    protected static final long A1 = (1L << 0); 
    protected static final long B1 = (1L << 1); 
    protected static final long C1 = (1L << 2); 
    protected static final long D1 = (1L << 3); 
    protected static final long E1 = (1L << 4); 
    protected static final long F1 = (1L << 5); 
    protected static final long G1 = (1L << 6); 
    protected static final long H1 = (1L << 7);
    protected static final long A2 = (1L << 8); 
    protected static final long B2 = (1L << 9); 
    protected static final long C2 = (1L << 10); 
    protected static final long D2 = (1L << 11); 
    protected static final long E2 = (1L << 12); 
    protected static final long F2 = (1L << 13); 
    protected static final long G2 = (1L << 14); 
    protected static final long H2 = (1L << 15);
    protected static final long A3 = (1L << 16); 
    protected static final long B3 = (1L << 17); 
    protected static final long C3 = (1L << 18); 
    protected static final long D3 = (1L << 19); 
    protected static final long E3 = (1L << 20); 
    protected static final long F3 = (1L << 21); 
    protected static final long G3 = (1L << 22); 
    protected static final long H3 = (1L << 23);
    protected static final long A4 = (1L << 24); 
    protected static final long B4 = (1L << 25); 
    protected static final long C4 = (1L << 26); 
    protected static final long D4 = (1L << 27); 
    protected static final long E4 = (1L << 28); 
    protected static final long F4 = (1L << 29); 
    protected static final long G4 = (1L << 30); 
    protected static final long H4 = (1L << 31);
    protected static final long A5 = (1L << 32); 
    protected static final long B5 = (1L << 33); 
    protected static final long C5 = (1L << 34); 
    protected static final long D5 = (1L << 35); 
    protected static final long E5 = (1L << 36); 
    protected static final long F5 = (1L << 37); 
    protected static final long G5 = (1L << 38); 
    protected static final long H5 = (1L << 39);
    protected static final long A6 = (1L << 40); 
    protected static final long B6 = (1L << 41); 
    protected static final long C6 = (1L << 42); 
    protected static final long D6 = (1L << 43); 
    protected static final long E6 = (1L << 44); 
    protected static final long F6 = (1L << 45); 
    protected static final long G6 = (1L << 46); 
    protected static final long H6 = (1L << 47);
    protected static final long A7 = (1L << 48); 
    protected static final long B7 = (1L << 49); 
    protected static final long C7 = (1L << 50); 
    protected static final long D7 = (1L << 51); 
    protected static final long E7 = (1L << 52); 
    protected static final long F7 = (1L << 53); 
    protected static final long G7 = (1L << 54); 
    protected static final long H7 = (1L << 55);
    protected static final long A8 = (1L << 56); 
    protected static final long B8 = (1L << 57); 
    protected static final long C8 = (1L << 58); 
    protected static final long D8 = (1L << 59); 
    protected static final long E8 = (1L << 60); 
    protected static final long F8 = (1L << 61); 
    protected static final long G8 = (1L << 62); 
    protected static final long H8 = (1L << 63);


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
