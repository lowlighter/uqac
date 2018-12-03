import java.util.*;


/** TODO: Pawn: TraillingZero promotion in black pawn case
  * Rook move
  * Bishop Move
  * King Move
  * Queen Move
  * Special moves (En-passant, Roque)
 */
public class MoveGenerator {

    private Board board;
    private long[] knigth_mask;

    MoveGenerator(Board board) {
        this.board = board;
        init();
    }

    private void init(){
        init_knight_mask();
        print_all_bits(knigth_mask[0]);
        print_all_bits(knigth_mask[7]);
        print_all_bits(knigth_mask[56]);
        print_all_bits(knigth_mask[63]);
        print_all_bits(knigth_mask[35]);
    }

    private void print_all_bits(long bb){
        for(int i = 0; i < Long.numberOfLeadingZeros(bb); i++) {
            System.out.print('0');
        }
        if(Long.numberOfLeadingZeros(bb) != 64) {
            System.out.println(Long.toBinaryString(bb));
        } else {
            System.out.println(' ');
        }
    }
 

// TODO : A DEBUG, mauvais selon la position
    private void init_knight_mask(){
        knigth_mask = new long[64];
        long pos;
        long test;
        for(int i=0; i<64; i++) {
            pos = (1<<i);
            
            test= pos & ~board.COLUMN_A & ~board.COLUMN_B & ~board.ROW_1;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1 << (i-10));
            }
            test= pos & ~board.COLUMN_A & ~board.COLUMN_B & ~board.ROW_8;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1 << (i+6));
            }

            test= pos & ~board.COLUMN_G & ~board.COLUMN_H & ~board.ROW_1;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1 << (i-6));
            }
            test= pos & ~board.COLUMN_G & ~board.COLUMN_H & ~board.ROW_8;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1 << (i+10));
            }

            test= pos & ~board.ROW_1 & ~board.ROW_2 & ~board.COLUMN_A;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1 << (i-17));
            }
            test= pos & ~board.ROW_1 & ~board.ROW_2 & ~board.COLUMN_H;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1 << (i-15));
            }

            test= pos & ~board.ROW_7 & ~board.ROW_8 & ~board.COLUMN_A;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1 << (i+15));
            }
            test= pos & ~board.ROW_7 & ~board.ROW_8 & ~board.COLUMN_H;
            if(test != 0 && (test & (test-1)) == 0){
                    knigth_mask[i] |= (1 << (i+17));
            }
        
        }
    }

    private List<Move> generate_white_pawn_moves(){
        List<Move> pawn_moves = new ArrayList<Move>();

        long my_pawn = board.bb_wp;
        long enemy_pieces = board.black_occupancy();
        long double_row = board.ROW_4;
        long last_row = board.ROW_8;
        long empty = board.get_empty();


        // GENERATION MOUVEMENT SIMPLE

        long new_pawn = (my_pawn << board.NORTH) & empty & ~last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-9) + (i<<6)));
            }
        }

        // GENERATION MOUVEMENT DOUBLE

        new_pawn = (my_pawn << 2 * board.NORTH) & empty & (empty << board.NORTH) & double_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-16) + (i<<6)));
            }
        }

        // GENERATION ATTAQUE GAUCHE

        new_pawn = (my_pawn << board.NORTH_WEST) & enemy_pieces & ~board.COLUMN_A & ~last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-7) + (i<<6)));
            }
        }
        // GENERATION ATTAQUE DROITE

        new_pawn = (my_pawn << board.NORTH_EAST) & enemy_pieces & ~board.COLUMN_H & ~last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-9) + (i<<6)));
            }
        }

        // PROMOTIONS

        new_pawn = (my_pawn << board.NORTH) & empty & last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-8) + (i<<6) + (1<<14)));
                pawn_moves.add(new Move((i-8) + (i<<6) + (1<<12) + (1<<14)));
                pawn_moves.add(new Move((i-8) + (i<<6) + (2<<12) + (1<<14)));
                pawn_moves.add(new Move((i-8) + (i<<6) + (3<<12) + (1<<14)));
            }
        }


        new_pawn = (my_pawn << board.NORTH_EAST) & enemy_pieces & ~board.COLUMN_H & last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-9) + (i<<6) + (1<<14)));
                pawn_moves.add(new Move((i-9) + (i<<6) + (1<<12) + (1<<14)));
                pawn_moves.add(new Move((i-9) + (i<<6) + (2<<12) + (1<<14)));
                pawn_moves.add(new Move((i-9) + (i<<6) + (3<<12) + (1<<14)));
            }
        }


         new_pawn = (my_pawn << board.NORTH_WEST) & enemy_pieces & ~board.COLUMN_A & last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i-7) + (i<<6) + (1<<14)));
                pawn_moves.add(new Move((i-7) + (i<<6) + (1<<12) + (1<<14)));
                pawn_moves.add(new Move((i-7) + (i<<6) + (2<<12) + (1<<14)));
                pawn_moves.add(new Move((i-7) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

        return pawn_moves;

    }


    private List<Move> generate_black_pawn_moves(){
         List<Move> pawn_moves = new ArrayList<Move>();

        long my_pawn = board.bb_bp;
        long enemy_pieces = board.white_occupancy();
        long double_row = board.ROW_5;
        long last_row = board.ROW_1;
        long empty = board.get_empty();


        // GENERATION MOUVEMENT SIMPLE
        long new_pawn = (my_pawn >> board.NORTH) & empty & ~last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+8) + (i<<6)));
            }
        }

        // GENERATION MOUVEMENT DOUBLE

        new_pawn = (my_pawn >> 2 * board.NORTH) & empty & (empty >> board.NORTH) & double_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+16) + (i<<6)));
            }
        }

        // GENERATION ATTAQUE GAUCHE

        new_pawn = (my_pawn >> board.NORTH_WEST) & enemy_pieces & ~board.COLUMN_H & ~last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+7) + (i<<6)));
            }
        }
        // GENERATION ATTAQUE DROITE

        new_pawn = (my_pawn >> board.NORTH_EAST) & enemy_pieces & ~board.COLUMN_A & ~last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+9) + (i<<6)));
            }
        }


        // PROMOTIONS

        new_pawn = (my_pawn >> board.NORTH) & empty & last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+8) + (i<<6) + (1<<14)));
                pawn_moves.add(new Move((i+8) + (i<<6) + (1<<12) + (1<<14)));
                pawn_moves.add(new Move((i+8) + (i<<6) + (2<<12) + (1<<14)));
                pawn_moves.add(new Move((i+8) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

        new_pawn = (my_pawn >> board.NORTH_WEST) & enemy_pieces & ~board.COLUMN_H & last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+7) + (i<<6) + (1<<14)));
                pawn_moves.add(new Move((i+7) + (i<<6) + (1<<12) + (1<<14)));
                pawn_moves.add(new Move((i+7) + (i<<6) + (2<<12) + (1<<14)));
                pawn_moves.add(new Move((i+7) + (i<<6) + (3<<12) + (1<<14)));
            }
        }
        // GENERATION ATTAQUE DROITE

        new_pawn = (my_pawn >> board.NORTH_EAST) & enemy_pieces & ~board.COLUMN_A & last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((i+9) + (i<<6) + (1<<14)));
                pawn_moves.add(new Move((i+9) + (i<<6) + (1<<12) + (1<<14)));
                pawn_moves.add(new Move((i+9) + (i<<6) + (2<<12) + (1<<14)));
                pawn_moves.add(new Move((i+9) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

        return pawn_moves;

    }


}