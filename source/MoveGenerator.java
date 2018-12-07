import java.util.*;


/** TODO: Pawn: TraillingZero promotion in black pawn case
  * Rook move
  * Bishop Move
  * King Move
  * Queen Move
  * Special moves (En-passant, Roque)
 */
public class MoveGenerator extends Constants {

    private Board board;
    private long[] knigth_mask;

    MoveGenerator(Board board) {
        this.board = board;
        init();
    }

    private void init(){
        init_knight_mask();
        knight_attack(board.bb_wn,true);

    }

    private void print_all_bits(long bb){
        for(int i = 0; i < Long.numberOfLeadingZeros((long) bb); i++) {
            System.out.print('0');
        }
        if(Long.numberOfLeadingZeros(bb) != 64) {
            System.out.println(Long.toBinaryString((long) bb));
        } else {
            System.out.println(' ');
        }
    }
 
    private List<Move> knight_move(long knight){
        long pieces = board.global_occupancy();
        List<Move> knight_moves = new ArrayList<Move>();
        
        // POUR CHACUNE DES PIECES ON APPLIQUE LE MASQUE CALCULER A L'INITIALISATION
        for(int i= Long.numberOfTrailingZeros(knight); i< 64; i++){
            if(((knight>>i) & 1) != 0) {
                long new_move = knigth_mask[i] & ~pieces;
                for(int j=Long.numberOfTrailingZeros(new_move); j<64; j++){
                    if(((new_move>>j) & 1) != 0) {
                        knight_moves.add(new Move(i + (j<<6)));
                    }
                }
            }
        }
        return knight_moves;
    }

    private List<Move> knight_attack(long knight, boolean white){
        List<Move> knight_attacks = new ArrayList<Move>();
        long enemy_pieces = (white) ? board.black_occupancy() : board.white_occupancy();
        long my_pieces = (white) ? board.white_occupancy() : board.black_occupancy();

        
        // POUR CHACUNE DES PIECES ON APPLIQUE LE MASQUE CALCULER A L'INITIALISATION
        for(int i= Long.numberOfTrailingZeros(knight); i< 64; i++){
            if(((knight>>i) & 1) != 0) {
                print_all_bits(knight);
                long new_move = knigth_mask[i] & ~my_pieces & enemy_pieces;
                print_all_bits(new_move);
                for(int j=Long.numberOfTrailingZeros(new_move); j<64; j++){
                    if(((new_move>>j) & 1) != 0) {
                        knight_attacks.add(new Move(i + (j<<6)));
                    }
                }
            }
        }
        return knight_attacks;
    }


    private void init_knight_mask(){
        knigth_mask = new long[64];
        long pos;
        long test;
        for(int i=0; i<64; i++) {
            pos = (1L<<i);

            test= pos & ~COLUMN_A & ~COLUMN_B & ~ROW_1;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1L << (i-10));
            }
            test= pos & ~COLUMN_A & ~COLUMN_B & ~ROW_8;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1L << (i+6));
            }

            test= pos & ~COLUMN_G & ~COLUMN_H & ~ROW_1;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1L << (i-6));
            }
            test= pos & ~COLUMN_G & ~COLUMN_H & ~ROW_8;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1L << (i+10));
            }

            test= pos & ~ROW_1 & ~ROW_2 & ~COLUMN_A;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1L << (i-17));
            }
            test= pos & ~ROW_1 & ~ROW_2 & ~COLUMN_H;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1L << (i-15));
            }

            test= pos & ~ROW_7 & ~ROW_8 & ~COLUMN_A;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1L << (i+15));
            }
            test= pos & ~ROW_7 & ~ROW_8 & ~COLUMN_H;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1L << (i+17));
            }
        
        }
    }

    private List<Move> generate_white_pawn_moves(long pawn_movable){
        List<Move> pawn_moves = new ArrayList<Move>();

        long my_pawn = pawn_movable;
        long enemy_pieces = board.black_occupancy();
        long double_row = ROW_4;
        long last_row = ROW_8;
        long empty = board.get_empty();


        // GENERATION MOUVEMENT SIMPLE

        long new_pawn = (my_pawn << NORTH) & empty & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-9) + (i<<6)));
            }
        }

        // GENERATION MOUVEMENT DOUBLE

        new_pawn = (my_pawn << 2 * NORTH) & empty & (empty << NORTH) & double_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-16) + (i<<6)));
            }
        }

        // PROMOTIONS

        new_pawn = (my_pawn << NORTH) & empty & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-8) + (i<<6) + (1<<14)));
                pawn_moves.add(new Move((i-8) + (i<<6) + (1<<12) + (1<<14)));
                pawn_moves.add(new Move((i-8) + (i<<6) + (2<<12) + (1<<14)));
                pawn_moves.add(new Move((i-8) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

        return pawn_moves;

    }

    private List<Move> generate_white_pawn_attack(){

        List<Move> pawn_attack = new ArrayList<Move>();
        long my_pawn = board.bb_wp;
        long enemy_pieces = board.black_occupancy();
        long last_row = ROW_8;
        long empty = board.get_empty();

        // GENERATION ATTAQUE GAUCHE

        long new_pawn = (my_pawn << NORTH_WEST) & enemy_pieces & ~COLUMN_A & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_attack.add(new Move((i-7) + (i<<6)));
            }
        }
        // GENERATION ATTAQUE DROITE

        new_pawn = (my_pawn << NORTH_EAST) & enemy_pieces & ~COLUMN_H & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_attack.add(new Move((i-9) + (i<<6)));
            }
        }

        // PROMOTIONS & ATTAQUE
        new_pawn = (my_pawn << NORTH_EAST) & enemy_pieces & ~COLUMN_H & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_attack.add(new Move((i-9) + (i<<6) + (1<<14)));
                pawn_attack.add(new Move((i-9) + (i<<6) + (1<<12) + (1<<14)));
                pawn_attack.add(new Move((i-9) + (i<<6) + (2<<12) + (1<<14)));
                pawn_attack.add(new Move((i-9) + (i<<6) + (3<<12) + (1<<14)));
            }
        }


        new_pawn = (my_pawn << NORTH_WEST) & enemy_pieces & ~COLUMN_A & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_attack.add(new Move((i-7) + (i<<6) + (1<<14)));
                pawn_attack.add(new Move((i-7) + (i<<6) + (1<<12) + (1<<14)));
                pawn_attack.add(new Move((i-7) + (i<<6) + (2<<12) + (1<<14)));
                pawn_attack.add(new Move((i-7) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

        return pawn_attack;

    }

    private List<Move> generate_black_pawn_moves(long pawn_movable){
         List<Move> pawn_moves = new ArrayList<Move>();

        long my_pawn = pawn_movable;
        long enemy_pieces = board.white_occupancy();
        long double_row = ROW_5;
        long last_row = ROW_1;
        long empty = board.get_empty();


        // GENERATION MOUVEMENT SIMPLE
        long new_pawn = (my_pawn >> NORTH) & empty & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+8) + (i<<6)));
            }
        }

        // GENERATION MOUVEMENT DOUBLE

        new_pawn = (my_pawn >> 2 * NORTH) & empty & (empty >> NORTH) & double_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+16) + (i<<6)));
            }
        }


        // PROMOTIONS

        new_pawn = (my_pawn >> NORTH) & empty & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+8) + (i<<6) + (1<<14)));
                pawn_moves.add(new Move((i+8) + (i<<6) + (1<<12) + (1<<14)));
                pawn_moves.add(new Move((i+8) + (i<<6) + (2<<12) + (1<<14)));
                pawn_moves.add(new Move((i+8) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

        return pawn_moves;

    }


    private List<Move> generate_black_pawn_attack(){
        List<Move> pawn_attack = new ArrayList<Move>();
        long my_pawn = board.bb_bp;
        long enemy_pieces = board.white_occupancy();
        long last_row = ROW_1;
        long empty = board.get_empty();


         // GENERATION ATTAQUE GAUCHE

        long new_pawn = (my_pawn >> NORTH_WEST) & enemy_pieces & ~COLUMN_H & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_attack.add(new Move((i+7) + (i<<6)));
            }
        }
        // GENERATION ATTAQUE DROITE

        new_pawn = (my_pawn >> NORTH_EAST) & enemy_pieces & ~COLUMN_A & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_attack.add(new Move((i+9) + (i<<6)));
            }
        }

        // PROMOTION

        new_pawn = (my_pawn >> NORTH_WEST) & enemy_pieces & ~COLUMN_H & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_attack.add(new Move((i+7) + (i<<6) + (1<<14)));
                pawn_attack.add(new Move((i+7) + (i<<6) + (1<<12) + (1<<14)));
                pawn_attack.add(new Move((i+7) + (i<<6) + (2<<12) + (1<<14)));
                pawn_attack.add(new Move((i+7) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

        new_pawn = (my_pawn >> NORTH_EAST) & enemy_pieces & ~COLUMN_A & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_attack.add(new Move((i+9) + (i<<6) + (1<<14)));
                pawn_attack.add(new Move((i+9) + (i<<6) + (1<<12) + (1<<14)));
                pawn_attack.add(new Move((i+9) + (i<<6) + (2<<12) + (1<<14)));
                pawn_attack.add(new Move((i+9) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

        return pawn_attack;

    }



}