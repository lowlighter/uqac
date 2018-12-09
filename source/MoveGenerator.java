import java.util.*;


/** TODO: Pawn: TraillingZero promotion in black pawn case
  * Special moves (En-passant, Roque)
 */
public class MoveGenerator extends Constants {

    private Board board;
    private long[] knight_mask;
    private long[] king_mask;
    private long[] bishop_bmask;
    private long[] rook_bmask;
    private long[][] rook_blkb;
    private long[][] bishop_blkb;
    private long[][] rook_moveb= new long[64][];
    private long[][] bishop_moveb = new long[64][];

    MoveGenerator(Board board) {
        this.board = board;
        init();
    }

    private void init(){

        init_king_mask();
        init_knight_mask();
        init_magic();
    }


    private void init_magic(){

        init_bishop_blocker_masks();
        init_rook_blocker_masks();

        init_shift();

        // BISHOP
        
        bishop_blkb = init_blocker_board(bishop_bmask);
        init_bishop_move_board();

        // ROOK
        
        rook_blkb = init_blocker_board(rook_bmask);
        init_rook_move_board();

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


    // Calculs tout les blocker boards possibles

    private long[][] init_blocker_board(long[] bmask) {

		long[][] blocker_board = new long[64][];
		for (int i = 0; i < 64; i++) {
			int bits_count = (int) (1L << Long.bitCount(bmask[i]));
			blocker_board[i] = new long[bits_count];

			for (int j = 1; j < bits_count; j++) {
				long mask = bmask[i];

				for (int k = Integer.numberOfTrailingZeros(j); k < 32; k++) {
					if (((1L << k) & j) != 0) {
						blocker_board[i][j] |= Long.lowestOneBit(mask);
					}
					mask &= mask - 1;
				}
			}
		}

		return blocker_board;
    }

    private void init_shift() {
		for (int i = 0; i < 64; i++) {
            B_BISHOP[i] = 64 - Long.bitCount(bishop_bmask[i]);
			B_ROOK[i] = 64 - Long.bitCount(rook_bmask[i]);
		}
	}
    

    // Initialisation du blocker mask pour les bishops

    private void init_bishop_blocker_masks(){
        bishop_bmask = new long[64];

        for(int i=0; i<64; i++){
            
            for(int j= i + NORTH_EAST; j < 64 - NORTH_EAST && j % 8 != 7 && j % 8 != 0; j+= NORTH_EAST){
                bishop_bmask[i] |=  (1L << j);
            }

            for(int j= i + NORTH_WEST; j < 64 - NORTH_WEST && j % 8 != 7 && j % 8 != 0; j+= NORTH_WEST){
                bishop_bmask[i] |=  (1L << j);
            }

            for(int j= i + SOUTH_EAST; j >= 0 - SOUTH_EAST && j % 8 != 7 && j % 8 != 0; j+= SOUTH_EAST){
                bishop_bmask[i] |=  (1L << j);
            }
            
            for(int j= i + SOUTH_WEST; j >= 0 - SOUTH_WEST && j % 8 != 7 && j % 8 != 0; j+= SOUTH_WEST){
                bishop_bmask[i] |=  (1L << j);
            }
        }
    }

    // Initialisation du blocker mask pour les rooks

    private void init_rook_blocker_masks(){
        rook_bmask = new long[64];

        for(int i=0; i<64; i++){
            
            for(int j= i + NORTH; j < 64 - NORTH; j+= NORTH){
                rook_bmask[i] |=  (1L << j);
            }

            for(int j= i + SOUTH; j >= 8; j+= SOUTH){
                rook_bmask[i] |=  (1L << j);
            }

            for(int j= i + EAST; j % 8 != 0 && j % 8 != 7; j+= EAST){
                rook_bmask[i] |=  (1L << j);
            }
            
            for(int j= i + WEST; j % 8 != 0 && j % 8 != 7 && j > 0; j+= WEST){
                rook_bmask[i] |=  (1L << j);
            }
        }
    }


    private void init_rook_move_board() {
		for (int i = 0; i < 64; i++) {
			rook_moveb[i] = new long[rook_blkb[i].length];
			for (int j = 0; j < rook_blkb[i].length; j++) {
				long new_move = 0;
                int magic = (int) ((rook_blkb[i][j] * MAGIC_R[i]) >>> B_ROOK[i]);
                
                // Une fois un blocker atteint, on ne peut plus continuer le mouvement, alors on break

				for (int k = i + NORTH; k < 64; k += NORTH) {
					new_move |= (1L << k);
					if ((rook_blkb[i][j] & (1L << k)) != 0) {
						break;
					}
				}
				for (int k = i + SOUTH; k >= 0; k += SOUTH) {
					new_move |= (1L << k);
					if ((rook_blkb[i][j] & (1L << k)) != 0) {
						break;
					}
				}
				for (int k = i + EAST; k % 8 != 0; k++) {
					new_move |= (1L << k);
					if ((rook_blkb[i][j] & (1L << k)) != 0) {
						break;
					}
				}
				for (int k = i + WEST; k % 8 != 7 && k >= 0; k+= WEST) {
					new_move |= (1L << k);
					if ((rook_blkb[i][j] & (1L << k)) != 0) {
						break;
					}
				}

				rook_moveb[i][magic] = new_move;
			}
		}
	}


    private void init_bishop_move_board() {
		for (int i = 0; i < 64; i++) {

			bishop_moveb[i] = new long[bishop_blkb[i].length];
			for (int j = 0; j < bishop_blkb[i].length; j++) {

				long new_move = 0;
                int magic = (int) ((bishop_blkb[i][j] * MAGIC_B[i]) >>> B_BISHOP[i]);
                
                // Une fois un blocker atteint, on ne peut plus continuer le mouvement, alors on break

				for (int k = i + NORTH_WEST; k % 8 != 7 && k < 64; k += NORTH_WEST) {
					new_move |= (1L << k);
					if ((bishop_blkb[i][j] & (1L << k)) != 0) {
						break;
					}
				}

				for (int k = i + NORTH_EAST; k % 8 != 0 && k < 64; k += NORTH_EAST) {
					new_move |= (1L << k);
					if ((bishop_blkb[i][j] & (1L << k)) != 0) {
						break;
					}
				}

				for (int k = i + SOUTH_WEST; k % 8 != 7 && k >= 0; k += SOUTH_WEST) {
					new_move |= (1L << k);
					if ((bishop_blkb[i][j] & (1L << k)) != 0) {
						break;
					}
				}

				for (int k = i + SOUTH_EAST; k % 8 != 0 && k >= 0; k -= SOUTH_EAST) {
					new_move |= (1L << k);
					if ((bishop_blkb[i][j] & (1L << k)) != 0) {
						break;
					}
				}

				bishop_moveb[i][magic] = new_move;
			}
		}
	}
 
    private List<Move> knight_move(long knight){
        long pieces = board.global_occupancy();
        List<Move> knight_moves = new ArrayList<Move>();
        
        // POUR CHACUNE DES PIECES ON APPLIQUE LE MASQUE CALCULER A L'INITIALISATION
        for(int i= Long.numberOfTrailingZeros(knight); i< 64; i++){
            if(((knight>>i) & 1) != 0) {
                long new_move = knight_mask[i] & ~pieces;
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
                long new_move = knight_mask[i] & ~my_pieces & enemy_pieces;
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
        knight_mask = new long[64];
        long pos;
        long test;
        for(int i=0; i<64; i++) {
            pos = (1L<<i);

            test= pos & ~COLUMN_A & ~COLUMN_B & ~ROW_1;
            if(test != 0 && (test & (test-1)) == 0){
                    knight_mask[i] |= (1L << (i-10));
            }
            test= pos & ~COLUMN_A & ~COLUMN_B & ~ROW_8;
            if(test != 0 && (test & (test-1)) == 0){
                    knight_mask[i] |= (1L << (i+6));
            }

            test= pos & ~COLUMN_G & ~COLUMN_H & ~ROW_1;
            if(test != 0 && (test & (test-1)) == 0){
                    knight_mask[i] |= (1L << (i-6));
            }
            test= pos & ~COLUMN_G & ~COLUMN_H & ~ROW_8;
            if(test != 0 && (test & (test-1)) == 0){
                    knight_mask[i] |= (1L << (i+10));
            }

            test= pos & ~ROW_1 & ~ROW_2 & ~COLUMN_A;
            if(test != 0 && (test & (test-1)) == 0){
                    knight_mask[i] |= (1L << (i-17));
            }
            test= pos & ~ROW_1 & ~ROW_2 & ~COLUMN_H;
            if(test != 0 && (test & (test-1)) == 0){
                    knight_mask[i] |= (1L << (i-15));
            }

            test= pos & ~ROW_7 & ~ROW_8 & ~COLUMN_A;
            if(test != 0 && (test & (test-1)) == 0){
                    knight_mask[i] |= (1L << (i+15));
            }
            test= pos & ~ROW_7 & ~ROW_8 & ~COLUMN_H;
            if(test != 0 && (test & (test-1)) == 0){
                    knight_mask[i] |= (1L << (i+17));
            }
        
        }
    }


    private void init_king_mask(){
        king_mask = new long[64];
        long pos;
        long test;
        for(int i=0; i<64; i++) {
            pos = (1L<<i);

            test= pos &  ~COLUMN_A;
            if(test != 0 && (test & (test-1)) == 0){
                    king_mask[i] |= (1L << (i + WEST));
            }

            test= pos & ~COLUMN_A & ~ROW_1;
            if(test != 0 && (test & (test-1)) == 0){
                    king_mask[i] |= (1L << (i + SOUTH_WEST));
            }
            test= pos & ~COLUMN_A & ~ROW_8;
            if(test != 0 && (test & (test-1)) == 0){
                    king_mask[i] |= (1L << (i + NORTH_WEST));
            }

            test= pos & ~COLUMN_H;
            if(test != 0 && (test & (test-1)) == 0){
                    king_mask[i] |= (1L << (i + EAST));
            }

            test= pos & ~COLUMN_H & ~ROW_1;
            if(test != 0 && (test & (test-1)) == 0){
                    king_mask[i] |= (1L << (i + SOUTH_EAST));
            }
            test= pos & ~COLUMN_H & ~ROW_8;
            if(test != 0 && (test & (test-1)) == 0){
                    king_mask[i] |= (1L << (i + NORTH_EAST));
            }

            test= pos & ~ROW_1;
            if(test != 0 && (test & (test-1)) == 0){
                    king_mask[i] |= (1L << (i + SOUTH));
            }
           
            test= pos & ~ROW_8;
            if(test != 0 && (test & (test-1)) == 0){
                    king_mask[i] |= (1L << (i + NORTH));
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
                pawn_moves.add(new Move((i-8) + (i<<6)));
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