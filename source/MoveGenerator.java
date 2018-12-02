import java.util.*;

public class MoveGenerator {

    private Board board;

    MoveGenerator(Board board) {
        this.board = board;
        init();
    }

    private void init(){
        generate_black_pawn_moves();
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
                pawn_moves.add(new Move((short) (i-9) + ((i-1)<<6)));
            }
        }

        // GENERATION MOUVEMENT DOUBLE

        new_pawn = (my_pawn << 2 * board.NORTH) & empty & (empty << board.NORTH) & double_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((short) (i-17) + ((i-1)<<6)));
            }
        }

        // GENERATION ATTAQUE GAUCHE

        new_pawn = (my_pawn << board.NORTH_WEST) & enemy_pieces & ~board.COLUMN_A & ~last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((short) (i-8) + ((i-1)<<6)));
            }
        }
        // GENERATION ATTAQUE DROITE

        new_pawn = (my_pawn << board.NORTH_EAST) & enemy_pieces & ~board.COLUMN_H & ~last_row;
        for(int i= Long.numberOfLeadingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((short) (i-10) + ((i-1)<<6)));
            }
        }


        // PROMOTIONS

        
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
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((short) (i+7) + ((i-1)<<6)));
            }
        }

        // GENERATION MOUVEMENT DOUBLE

        new_pawn = (my_pawn >> 2 * board.NORTH) & empty & (empty >> board.NORTH) & double_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((short) (i+15) + ((i-1)<<6)));
            }
        }

        // GENERATION ATTAQUE GAUCHE

        new_pawn = (my_pawn >> board.NORTH_WEST) & enemy_pieces & ~board.COLUMN_H & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((short) (i+6) + ((i-1)<<6)));
            }
        }
        // GENERATION ATTAQUE DROITE

        new_pawn = (my_pawn >> board.NORTH_EAST) & enemy_pieces & ~board.COLUMN_A & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                pawn_moves.add(new Move((short) (i+8) + ((i-1)<<6)));
            }
        }


        // PROMOTIONS


        return pawn_moves;

    }


}