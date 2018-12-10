/**
 * Contient les constantes pré-calculées pour les cases et types de pièces.
 */
public abstract class Constants {

    /** Couleurs. */
    protected static final boolean WHITE = true;
    protected static final boolean BLACK = false;

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
    protected static final char PROMOTED_VOID = '-';
    protected static final char PROMOTED_ROOK = 'r';
    protected static final char PROMOTED_KNIGHT = 'n';
    protected static final char PROMOTED_BISHOP = 'b'; 
    protected static final char PROMOTED_QUEEN = 'q';
    
    /** Cases du plateau. */
    protected static final long VOID = 0L;
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


    protected static final short MOVE_TO = 0b0000111111000000;
    protected static final short MOVE_FROM =   0b0000000000111111;

    /** Lignes */

    protected static final long ROW_1 = 0b0000000000000000000000000000000000000000000000000000000011111111L;
    protected static final long ROW_2 = 0b0000000000000000000000000000000000000000000000001111111100000000L;
    protected static final long ROW_3 = 0b0000000000000000000000000000000000000000111111110000000000000000L;
    protected static final long ROW_4 = 0b0000000000000000000000000000000011111111000000000000000000000000L;
    protected static final long ROW_5 = 0b0000000000000000000000001111111100000000000000000000000000000000L;
    protected static final long ROW_6 = 0b0000000000000000111111110000000000000000000000000000000000000000L;
    protected static final long ROW_7 = 0b0000000011111111000000000000000000000000000000000000000000000000L;
    protected static final long ROW_8 = 0b1111111100000000000000000000000000000000000000000000000000000000L;

    /** Colonne  */
                                        
    protected static final long COLUMN_A = 0x101010101010101L;
    protected static final long COLUMN_B = 0x202020202020202L;
    protected static final long COLUMN_C = 0x404040404040404L;
    protected static final long COLUMN_D = 0x808080808080808L;
    protected static final long COLUMN_E = 0x1010101010101010L;
    protected static final long COLUMN_F = 0x2020202020202020L;
    protected static final long COLUMN_G = 0x4040404040404040L;
    protected static final long COLUMN_H = 0x8080808080808080L;

    /** Direction */

    protected static final int NORTH = 8;
    protected static final int SOUTH = -8;
    protected static final int EAST = 1;
    protected static final int WEST = -1;

    protected static final int NORTH_WEST = NORTH + WEST;
    protected static final int NORTH_EAST = NORTH + EAST;
    protected static final int SOUTH_WEST = SOUTH + WEST;
    protected static final int SOUTH_EAST = SOUTH + EAST;



    /** Magic numbers */

    protected static final long[] MAGIC_B = { 0x2910054208004104L, 0x2100630a7020180L, 0x5822022042000000L, 0x2ca804a100200020L, 0x204042200000900L,
        0x2002121024000002L, 0x80404104202000e8L, 0x812a020205010840L, 0x8005181184080048L, 0x1001c20208010101L, 0x1001080204002100L, 0x1810080489021800L,
        0x62040420010a00L, 0x5028043004300020L, 0xc0080a4402605002L, 0x8a00a0104220200L, 0x940000410821212L, 0x1808024a280210L, 0x40c0422080a0598L,
        0x4228020082004050L, 0x200800400e00100L, 0x20b001230021040L, 0x90a0201900c00L, 0x4940120a0a0108L, 0x20208050a42180L, 0x1004804b280200L,
        0x2048020024040010L, 0x102c04004010200L, 0x20408204c002010L, 0x2411100020080c1L, 0x102a008084042100L, 0x941030000a09846L, 0x244100800400200L,
        0x4000901010080696L, 0x280404180020L, 0x800042008240100L, 0x220008400088020L, 0x4020182000904c9L, 0x23010400020600L, 0x41040020110302L,
        0x412101004020818L, 0x8022080a09404208L, 0x1401210240484800L, 0x22244208010080L, 0x1105040104000210L, 0x2040088800c40081L, 0x8184810252000400L,
        0x4004610041002200L, 0x40201a444400810L, 0x4611010802020008L, 0x80000b0401040402L, 0x20004821880a00L, 0x8200002022440100L, 0x9431801010068L,
        0x1040c20806108040L, 0x804901403022a40L, 0x2400202602104000L, 0x208520209440204L, 0x40c000022013020L, 0x2000104000420600L, 0x400000260142410L,
        0x800633408100500L, 0x2404080a1410L, 0x138200122002900L };


    protected static final long[] MAGIC_R = { 0xa180022080400230L, 0x40100040022000L, 0x80088020001002L, 0x80080280841000L, 0x4200042010460008L,
        0x4800a0003040080L, 0x400110082041008L, 0x8000a041000880L, 0x10138001a080c010L, 0x804008200480L, 0x10011012000c0L, 0x22004128102200L,
        0x200081201200cL, 0x202a001048460004L, 0x81000100420004L, 0x4000800380004500L, 0x208002904001L, 0x90004040026008L, 0x208808010002001L,
        0x2002020020704940L, 0x8048010008110005L, 0x6820808004002200L, 0xa80040008023011L, 0xb1460000811044L, 0x4204400080008ea0L, 0xb002400180200184L,
        0x2020200080100380L, 0x10080080100080L, 0x2204080080800400L, 0xa40080360080L, 0x2040604002810b1L, 0x8c218600004104L, 0x8180004000402000L,
        0x488c402000401001L, 0x4018a00080801004L, 0x1230002105001008L, 0x8904800800800400L, 0x42000c42003810L, 0x8408110400b012L, 0x18086182000401L,
        0x2240088020c28000L, 0x1001201040c004L, 0xa02008010420020L, 0x10003009010060L, 0x4008008008014L, 0x80020004008080L, 0x282020001008080L,
        0x50000181204a0004L, 0x102042111804200L, 0x40002010004001c0L, 0x19220045508200L, 0x20030010060a900L, 0x8018028040080L, 0x88240002008080L,
        0x10301802830400L, 0x332a4081140200L, 0x8080010a601241L, 0x1008010400021L, 0x4082001007241L, 0x211009001200509L, 0x8015001002441801L,
        0x801000804000603L, 0xc0900220024a401L, 0x1000200608243L };

    /** Util */

    protected void print_all_bits(long bb){
        for(int i = 0; i < Long.numberOfLeadingZeros((long) bb); i++) {
            System.out.print('0');
        }
        if(Long.numberOfLeadingZeros(bb) != 64) {
            System.out.println(Long.toBinaryString((long) bb));
        } else {
            System.out.println(' ');
        }
    }
}
