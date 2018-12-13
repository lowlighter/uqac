import java.util.*;



 /**
  * Générateur de mouvements legaux
  * TODO:
  * Refactor pour optimiser 
  * Special moves (En-passant, Roque)
  */

public class MoveGenerator extends Constants {

    /***********************************************************************************************************************/
    /******************************************************** VAR **********************************************************/
    /***********************************************************************************************************************/

    // Bitboards de ligne/diagonales de cases entre deux index
    private static final long[][] INTERCASE = new long[64][64];

    // Bitboards de ligne/diagonales de cases de la direction roi-bout de la ligne/diagonales
    private static final long[][] XRAY = new long[64][64];

    // Bit shift for magic bitboard
    private static final int[] B_BISHOP = new int[64];
    private static final int[] B_ROOK = new int[64];

    // Plateau de jeu
    private Board board;

    // Masques des mouvements possibles
    private long[] knight_mask;
    private long[] king_mask;

    // Masques des blocker possibles
    private long[] bishop_bmask;
    private long[] rook_bmask;

    // Combinaison des bloqueur possibles
    private long[][] rook_blkb = new long[64][];
    private long[][] bishop_blkb = new long[64][];

    // Combinaison des mouvements possibles
    private long[][] rook_moveb= new long[64][];
    private long[][] bishop_moveb = new long[64][];

    // Liste des mouvements possible
    private List<Move> moves;



    /***********************************************************************************************************************/
    /******************************************************** INIT *********************************************************/
    /***********************************************************************************************************************/

    
    /**
     * Constructor
     * @param board Plateau de jeu
     */
    MoveGenerator(Board board) {
        this.board = board;
        init();
        // print_all_bits(XRAY[60][53]);
        // print_all_bits(XRAY[26][19]);
        // print_all_bits(XRAY[29][36]);
        // print_all_bits(XRAY[27][34]);
        // print_all_bits(XRAY[19][26]);
        // print_all_bits(XRAY[2][9]);

        // System.out.println("-------------------");

        // print_all_bits(XRAY[0][9]);
        // print_all_bits(XRAY[9][18]);
        // print_all_bits(XRAY[27][18]);
        // print_all_bits(XRAY[33][42]);
        // print_all_bits(XRAY[59][50]);
        // print_all_bits(XRAY[13][22]);
        // print_all_bits(XRAY[23][14]);
    }

    
    /** Initialisation des masques et matrices utilitaires */
    private void init(){

        init_intercase_matrix();
        init_xray_matrix();
        init_king_mask();
        init_knight_mask();
        init_magic();

    }

    /** Initialisation des masques/blockerboards/magicmoveboard (Sliding pieces) */
    private void init_magic(){

        init_bishop_blocker_masks();
        init_rook_blocker_masks();

        init_shift();

        // BISHOP
        
        init_blocker_board(bishop_bmask, bishop_blkb);
        init_bishop_move_board();

        // ROOK
        
        init_blocker_board(rook_bmask, rook_blkb);
        init_rook_move_board();

    }

    /** init de la matrices des bits shifts, sert pour la restitution du mouvement magique */
    private void init_shift() {
		for (int i = 0; i < 64; i++) {
            B_BISHOP[i] = 64 - Long.bitCount(bishop_bmask[i]);
			B_ROOK[i] = 64 - Long.bitCount(rook_bmask[i]);
		}
    }

    
    /** Calculs tous les blocker boards possibles */
    private void init_blocker_board(long[] bmask, long[][] blocker_board) {
   
		for (int i = 0; i < 64; i++) {
			int bits_count = (int) (1L << Long.bitCount(bmask[i]));
			blocker_board[i] = new long[bits_count];
			for (int j = 1; j < bits_count; j++) {
                long mask = bmask[i];
                int k=0;
                while(k < 32){
                    if (((1 << k) & j) != 0) {
						blocker_board[i][j] |= Long.lowestOneBit(mask);
                    }
                    mask &= mask - 1;
                    k++;
                }
			}
		}
    }

    /**  Matrice des Bitboards de ligne/diagonales entre deux pieces */
    private void init_intercase_matrix(){

        for(int i=0; i<64; i++){
            for(int j=i+1; j<64; j++){
                
                if(i / NORTH == j / NORTH){
                    for(int k= i + EAST; k <= j - EAST; k += EAST){
                        INTERCASE[i][j] |= (1L << k);
                    }
                }

                if(i % NORTH == j % NORTH){
                    for(int k= i + NORTH; k <= j - NORTH; k += NORTH){
                        INTERCASE[i][j] |= (1L << k);
                    }
                }

                if((i / NORTH < j / NORTH) && ((j -i) % NORTH_EAST == 0)){
                    for(int k= i + NORTH_EAST; k <= j - NORTH_EAST; k += NORTH_EAST){
                        INTERCASE[i][j] |= (1L << k);
                    }
                }

                
                if((i / NORTH < j / NORTH) && ((j -i) % NORTH_WEST == 0)){
                    for(int k= i + NORTH_WEST; k <= j - NORTH_WEST; k += NORTH_WEST){
                        INTERCASE[i][j] |= (1L << k);
                    }
                    
                }
            }
        }


        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < i; j++) {
                INTERCASE[i][j] = INTERCASE[j][i];
            }
        }
    }


    private void init_xray_matrix(){

        for(int i=0; i<64; i++){
            for(int j=0; j<64; j++){
                if(i == j) { continue;}
                int borne, pas;
                int substract = j - i;  
                int side;
                int king_row = i / 8;
                int king_col = i % 8;
                int piece_row = j / 8;
                int piece_col = j % 8;

                if(king_col == piece_col){
                    pas = (substract >0) ? 8 : -8;
                    borne = (substract > 0) ? i + ((8 - king_row) * 8) : i - (king_row + 1) * 8;
                    for(int k=i; k!= borne; k+=pas){
                        XRAY[i][j] |= (1L << k);
                    }
                }

                if (king_row == piece_row) {
                    pas = (substract >0) ? 1 : -1;
                    borne = (substract > 0) ? i + (8 - king_col) : i - (king_col + 1);
                    for(int k=i; k!= borne; k+=pas){
                        XRAY[i][j] |= (1L << k);
                    }
                }

                if(substract % 7 == 0 && king_col != piece_col){
                    pas = (substract >0) ? 7 : -7;
                    side = (king_col + king_row >= 8) ?  (substract < 0) ? 8  - king_col : 8 - king_row : (substract < 0) ? 8 - (king_row+1) : king_col +1;
                    borne = (substract > 0) ? i + (side * 7) : i - (side * 7);
                    for(int k=i; k!= borne; k+=pas){
                        XRAY[i][j] |= (1L << k);
                    }
                }


                if(substract % 9 == 0 && king_col != piece_col){
                    pas = (substract >0) ? 9 : -9;
                    side = (king_col - king_row >= 0) ?  ((substract < 0) ? king_row+1 : 8 - king_col ): ((substract < 0) ?  king_col +1 : 8 - (king_row));
                    borne = (substract > 0) ? i + (side * 9) : i - (side * 9);
                    for(int k=i; k!= borne; k+=pas){
                        XRAY[i][j] |= (1L << k);
                    }
                }


            }
        }
    }
    
    
    /***********************************************************************************************************************/
    /*************************************************** MOVE SELECTION ****************************************************/
    /***********************************************************************************************************************/

    /** Renvois la liste des mouvements possibles pour un tour
    * @param white couleur du joueurs qui doit jouer
    */
    public List<Move> get_legal_moves(boolean white){
        moves = new ArrayList<Move>();
        long checking_pieces = compute_checking_pieces(white);

        // Combien d'echec sur le roi
        switch (Long.bitCount(checking_pieces)) {
            // Generation des mouvements pinned & non pinned
            case 0:
                all_moves(white);
                break;
            // S'enlever de l'echec
            case 1:
                secure_king(white, checking_pieces);
                break;
            // Uniquement le roi peut bouger/attaquer pour s'enlever de l'echec
            default:
                king_moves(white);
                break;
        }

        return moves;
    }

    
    /** Aucune restriction sur le roi concernant les mouvements à renvoyer
     * @param white couleur du joueurs qui doit jouer
    */
    private void all_moves(boolean white){
        long pinned = compute_pinned_pieces(white);
        long pawn, rook, bishop, queen, king, knight, enemy_king;
        long empty = board.get_empty();
        long enemy = (white) ? board.black_occupancy() : board.white_occupancy();
        int king_pos = board.get_king_position(white);

        //  PIECES NON PINNED
        if(white) {
            pawn = board.bb_wp;
            rook = board.bb_wr;
            bishop = board.bb_wb;
            queen = board.bb_wq;
            king = board.bb_wk;
            knight = board.bb_wn;
            enemy_king = board.bb_bk;
            generate_white_pawn_moves(pawn & ~pinned, empty);
            generate_white_pawn_attack(pawn & ~pinned, enemy & ~enemy_king);
        } else {
            pawn = board.bb_bp;
            rook = board.bb_br;
            bishop = board.bb_bb;
            queen = board.bb_bq;
            king = board.bb_bk;
            knight = board.bb_bn;
            enemy_king = board.bb_wk;
            generate_black_pawn_moves(pawn & ~pinned, empty);
            generate_black_pawn_attack(pawn & ~pinned, enemy & ~enemy_king);
        }

        generate_bishop_move_or_attack(bishop & ~pinned, empty);
        generate_bishop_move_or_attack(bishop & ~pinned, enemy & ~enemy_king);
        generate_rook_move_or_attack(rook & ~pinned, empty);
        generate_rook_move_or_attack(rook & ~pinned, enemy & ~enemy_king);
        generate_queen_move_or_attack(queen & ~pinned, empty);
        generate_queen_move_or_attack(queen & ~pinned, enemy & ~enemy_king);
        generate_knight_move(knight & ~pinned, empty);
        generate_knight_attack(knight & ~pinned, enemy & ~enemy_king);

        king_moves(white);

        // PIECES PINNED

        while(pinned != 0){
            int pinned_pos= Long.numberOfTrailingZeros(pinned);
            long pinned_piece = (1L << pinned_pos);

            if((pinned_piece & queen) != 0) {
                generate_queen_move_or_attack(queen & pinned_piece, empty & XRAY[king_pos][pinned_pos]);
                generate_queen_move_or_attack(queen & pinned_piece, enemy & ~enemy_king & XRAY[king_pos][pinned_pos]);
            }
            
            if((pinned_piece & rook) != 0) {
                generate_rook_move_or_attack(rook & pinned_piece, empty & XRAY[king_pos][pinned_pos]);
                generate_rook_move_or_attack(rook & pinned_piece, enemy & ~enemy_king & XRAY[king_pos][pinned_pos]);
            }

            if((pinned_piece & bishop) != 0) {
                generate_bishop_move_or_attack(bishop & pinned_piece, empty & XRAY[king_pos][pinned_pos]);
                generate_bishop_move_or_attack(bishop & pinned_piece, enemy & ~enemy_king & XRAY[king_pos][pinned_pos]);
            }


            if((pinned_piece & knight) != 0) {
                generate_knight_move(knight & pinned_piece, empty & XRAY[king_pos][pinned_pos]);
                generate_knight_attack(knight & pinned_piece, enemy & ~enemy_king & XRAY[king_pos][pinned_pos]);
            }

            if((pinned_piece & pawn) != 0) {
                if(white) {
                    generate_white_pawn_moves(pawn & pinned_piece, empty & XRAY[king_pos][pinned_pos]);
                    generate_white_pawn_attack(pawn & pinned_piece, enemy & ~enemy_king & XRAY[king_pos][pinned_pos]);
                } else {
                    generate_black_pawn_moves(pawn & pinned_piece, empty & XRAY[king_pos][pinned_pos]);
                    generate_black_pawn_attack(pawn & pinned_piece, enemy & ~enemy_king & XRAY[king_pos][pinned_pos]);
                }
            }
        
            pinned &= pinned -1;
        }

    }

    /** Restriction des mouvements possibles pour s'enlever d'un echec
     * @param white couleur du joueurs qui doit jouer
    */
    private void secure_king(boolean white, long checking_piece){
        long pinned = compute_pinned_pieces(white);
        long pawn, rook, bishop, queen, king, knight, enemy_knight, enemy_king;
        long empty = board.get_empty();
        int king_pos = board.get_king_position(white);

        if(white){
            pawn = board.bb_wp;
            rook = board.bb_wr;
            bishop = board.bb_wb;
            queen = board.bb_wq;
            king = board.bb_wk;
            knight = board.bb_wn;
            enemy_knight = board.bb_bn;
            enemy_king = board.bb_bk;
        } else {
            pawn = board.bb_bp;
            rook = board.bb_br;
            bishop = board.bb_bb;
            queen = board.bb_bq;
            king = board.bb_bk;
            knight = board.bb_bn;
            enemy_knight = board.bb_wn;
            enemy_king = board.bb_wk;
        }

        // Mouvement & attaque du roi pour qu'ils se mettent lui même à l'abris
        king_moves(white);

        // Si ce n'est pas un knight et qu'il y a une place pour s'interposer on calcul les mouvements pour s'interposer, sinon on ne le fait pas
        long spaces = INTERCASE[Long.numberOfTrailingZeros(checking_piece)][board.get_king_position(white)];
       
        

        if((checking_piece & enemy_knight) == 0 && (spaces != 0)) {
            generate_bishop_move_or_attack(bishop & ~pinned, empty & spaces);
            generate_rook_move_or_attack(rook & ~pinned, empty & spaces);
            generate_queen_move_or_attack(queen & ~pinned, empty & spaces);
            generate_knight_move(knight & ~pinned, empty & spaces);
            if(white){generate_white_pawn_moves(pawn & ~pinned, empty & spaces);}
            else {generate_black_pawn_moves(pawn & ~pinned, empty & spaces);}
        }

        // PINNED ATTACK
            generate_bishop_move_or_attack(bishop & ~pinned, checking_piece);
            generate_rook_move_or_attack(rook & ~pinned, checking_piece);
            generate_queen_move_or_attack(queen & ~pinned, checking_piece);
            generate_knight_attack(knight & ~pinned, checking_piece);
        
        //  PINNED ATTACK

        while(pinned != 0){
            int pinned_pos= Long.numberOfTrailingZeros(pinned);
            long pinned_piece = (1L << pinned_pos);

            if((pinned_piece & queen) != 0) {
                generate_queen_move_or_attack(queen & pinned_piece, checking_piece & XRAY[king_pos][pinned_pos]);
            }
            
            if((pinned_piece & rook) != 0) {
                generate_rook_move_or_attack(rook & pinned_piece, checking_piece & XRAY[king_pos][pinned_pos]);
            }

            if((pinned_piece & bishop) != 0) {
                generate_bishop_move_or_attack(bishop & pinned_piece, checking_piece & XRAY[king_pos][pinned_pos]);
            }


            if((pinned_piece & knight) != 0) {
                generate_knight_attack(knight & pinned_piece, checking_piece & XRAY[king_pos][pinned_pos]);
            }

            if((pinned_piece & pawn) != 0) {
                if(white) {
                    generate_white_pawn_attack(pawn & pinned_piece, checking_piece & XRAY[king_pos][pinned_pos]);
                } else {
                    generate_black_pawn_attack(pawn & pinned_piece, checking_piece & XRAY[king_pos][pinned_pos]);
                }
            }
        
            pinned &= pinned -1;
        }
        
    }


    /** Bitboard des pieces qui attaque le roi
     * @param white couleur du joueurs qui doit jouer
    */

    private long compute_checking_pieces(boolean white){
        int king_pos = board.get_king_position(white);
        long enemy_pieces = (white) ? board.black_occupancy() : board.white_occupancy();
        long all_pieces = board.global_occupancy();
        long king_pos_bb = (1L << king_pos);
        long test;
        long checked = 0L;
        
        /* Checking pawns */
        long pawns = (white) ? board.bb_bp : board.bb_wp;

        if(white) {
            test = (pawns >> NORTH_WEST) & ~COLUMN_H & king_pos_bb;
            if(test != 0) {
                checked |= (1L << (king_pos + NORTH_WEST));
            }

            test = (pawns >> NORTH_EAST) & ~COLUMN_A & king_pos_bb;
            if(test != 0) {
                checked |= (1L << (king_pos + NORTH_EAST));
            }
        } else {
            test = (pawns << NORTH_WEST) & ~COLUMN_A & king_pos_bb;
            if(test != 0) {
                checked |= (1L << (king_pos - NORTH_WEST));
            }

            test = (pawns << NORTH_EAST) & ~COLUMN_H & king_pos_bb;
            if(test != 0) {
                checked |= (1L << (king_pos - NORTH_EAST));
            }
        }
        
        /** Checking Knights */
        long knights = (white) ? board.bb_bn : board.bb_wn;

        while(knights != 0) {
            int knight_pos = Long.numberOfTrailingZeros(knights);
            test = knight_mask[knight_pos] & king_pos_bb;
            if(test != 0) {
                checked |= (1L << knight_pos);
            }

            knights &= knights - 1;
        }

        /** Checking Sliding pieces */
        long line_slide = (white) ? board.bb_br | board.bb_bq : board.bb_wr | board.bb_wq;
        long diagonal_slide = (white) ? board.bb_bb | board.bb_bq : board.bb_wb | board.bb_wq;
        int piece_pos;

        while(line_slide != 0) {
            piece_pos = Long.numberOfTrailingZeros(line_slide);
            test = rook_movebb(piece_pos, all_pieces) & king_pos_bb;
            if(test != 0) {
                checked |= (1L << piece_pos);
            }
            line_slide &= line_slide - 1;
        }


        while(diagonal_slide != 0) {
            piece_pos = Long.numberOfTrailingZeros(diagonal_slide);
            test = bishop_movebb(piece_pos, all_pieces) & king_pos_bb;
            if(test != 0) {
                checked |= (1L << piece_pos);
            }

            diagonal_slide &= diagonal_slide - 1;
        }


        // Not checking the enemy king because it can not put the king in check
        return checked;

    }


    /** Renvois les pieces bloquées par les slidings pièces
     * @param white couleur du joueurs qui doit jouer
     */
    private long compute_pinned_pieces(boolean white) {
        int king_pos = board.get_king_position(white);
        long my_pieces = (white) ? board.white_occupancy() : board.black_occupancy();
        long enemy_pieces = (white) ? board.black_occupancy() : board.white_occupancy();
        long pinned = 0L;
        long potential_pinners = (white) ? ((board.bb_bb | board.bb_bq) & bishop_moveb[king_pos][0]) | ((board.bb_br | board.bb_bq) & rook_moveb[king_pos][0])
                                : ((board.bb_wb | board.bb_wq) & bishop_moveb[king_pos][0]) | ((board.bb_wr | board.bb_wq) & rook_moveb[king_pos][0]);

        while(potential_pinners != 0) {
            long chunk_enemy_pieces = INTERCASE[king_pos][Long.numberOfTrailingZeros(potential_pinners)] & enemy_pieces;
            long chunk_ally_pieces = INTERCASE[king_pos][Long.numberOfTrailingZeros(potential_pinners)] & my_pieces;

            if(Long.bitCount(chunk_enemy_pieces) >=1) {
                // The enemy is blocking himself

            } else {
                // If we have only one friend piece on the path, we have mark it as pinned
                if(Long.bitCount(chunk_ally_pieces) == 1) {
                    pinned |= chunk_ally_pieces;
                }
            }
            potential_pinners &= potential_pinners - 1;
        }
        
        return pinned;
    }

    /***********************************************************************************************************************/
    /******************************************************** KING *********************************************************/
    /***********************************************************************************************************************/

    /** Initialise les masques des mouvements possible du roi */
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

    /** Calcul la liste des mouvements possibles pour le roi
    * @param white couleur du joueur qui doit jouer
    */
    private void king_moves(boolean white){

        int king_pos = board.get_king_position(white);
        long enemy_pawns = (white) ? board.bb_bp : board.bb_wp;
        long enemy_knights = (white) ? board.bb_bn : board.bb_wn;
        long enemy_rooks = (white) ? board.bb_br : board.bb_wr;
        long enemy_bishops = (white) ? board.bb_bb : board.bb_wb;
        long enemy_queens = (white) ? board.bb_bq : board.bb_wq;
        long enemy_pieces = enemy_pawns | enemy_knights | enemy_rooks | enemy_queens | enemy_bishops;
        long empty = board.get_empty();
        long enemy_attack = 0L;
        long board_no_king = board.global_occupancy() & ~(1L<<king_pos);
        
        // On determine les cases potentiellement attaque par l'ennemi pour ne pas s'y deplacer
        enemy_attack |= pawn_right_attack_bb(!white, enemy_pawns);
        enemy_attack |= pawn_left_attack_bb(!white, enemy_pawns);
        enemy_attack |= knight_attack_bb(enemy_knights);

        // Pour les pieces sliding on enleve le roi, afin d'eviter que le roi soit en echec après le mouvement à cause de la même sliding piece
        enemy_attack |= slide_attack_bb(enemy_rooks, enemy_bishops, enemy_queens, board_no_king);

        // MOUVEMENTS EN CASE NON ATTAQUEE & VIDE
        long king_move = king_mask[king_pos];
        long options = king_move & ~enemy_attack & empty;
        for(int i=Long.numberOfTrailingZeros(options); i<64; i++){
            if(((options>>i) & 1) != 0){
                moves.add(new Move(king_pos + (i<<6)));
            }
        }

        // ATTAQUE CASE ENNEMIE NON ATTAQUEE
        options = king_move & ~enemy_attack & enemy_pieces;
        for(int i=Long.numberOfTrailingZeros(options); i<64; i++){
            if(((options>>i) & 1) != 0){
                moves.add(new Move(king_pos + (i<<6)));
            }
        }
    }
    
    /***********************************************************************************************************************/
    /******************************************************** QUEEN ********************************************************/
    /***********************************************************************************************************************/

    /** Renvois le bitboard des mouvements pour une reine à une case donnée
     * @param index indice de la case de la reine
     * @param pieces liste des pièces sur le board
     */

    public long queen_movebb(int index, long pieces) {
		return rook_movebb(index, pieces) | bishop_movebb(index, pieces);
    }


    /** Ajoute les mouvements pour un ensemble de queen
     * @param pieces liste de l'ensemble de queen
     * @param restriction restriction concernant les mouvements
     */
    private void generate_queen_move_or_attack(long pieces, long restriction) {
        generate_rook_move_or_attack(pieces, restriction);
        generate_bishop_move_or_attack(pieces, restriction);
    }

    /***********************************************************************************************************************/
    /******************************************************** ROOK *********************************************************/
    /***********************************************************************************************************************/
    

    /** Initialisation du blocker mask pour les rooks */

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
    
    /** Initialisation du move board pour les rooks */

    private void init_rook_move_board() {
		for (int i = 0; i < 64; i++) {
			rook_moveb[i] = new long[rook_blkb[i].length];
			for (int j = 0; j < rook_blkb[i].length; j++) {
				long new_move = 0L;
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
				for (int k = i + EAST; k % 8 != 0; k += EAST) {
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
    
    /** Renvois le bitboard des mouvements pour un rook à une case donnée
     * @param index indice de la case du rook
     * @param pieces liste des pièces sur le board
     */
    public long rook_movebb(int index, long pieces) {
		return rook_moveb[index][(int) ((pieces & rook_bmask[index]) * MAGIC_R[index] >>> B_ROOK[index])];
    }
    

    /** Ajoute les mouvements pour un ensemble de rook
     * @param pieces liste de l'ensemble de rook
     * @param restriction restriction concernant les mouvements
     */
    private void generate_rook_move_or_attack(long pieces, long restriction) {

        while(pieces != 0) {
            int start = Long.numberOfTrailingZeros(pieces);
            long board_occ = board.global_occupancy();
            long new_moves = rook_movebb(start, board_occ) & restriction;
            while(new_moves != 0){
                int target = Long.numberOfTrailingZeros(new_moves);
                moves.add(new Move(start + (target << 6)));
                new_moves &= new_moves - 1;
            }
            
            pieces &= pieces -1;
        }

    }

/***********************************************************************************************************************/
/******************************************************* BISHOP ********************************************************/
/***********************************************************************************************************************/


    /** Initialisation du blocker mask pour les bishops */

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


    /** Initialisation du move board pour les bishops */

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

				for (int k = i + SOUTH_EAST; k % 8 != 0 && k >= 0; k += SOUTH_EAST) {
					new_move |= (1L << k);
					if ((bishop_blkb[i][j] & (1L << k)) != 0) {
						break;
					}
				}

				bishop_moveb[i][magic] = new_move;
			}
		}
    }
    

    /** Renvois le bitboard des mouvements pour un bishop à une case donnée
     * @param index indice de la case du bishop
     * @param pieces liste des pièces sur le board
     */

	public long bishop_movebb(int index, long pieces) {
		return bishop_moveb[index][(int) ((pieces & bishop_bmask[index]) * MAGIC_B[index] >>> B_BISHOP[index])];
    }
    
    
    /** Ajoute les mouvements pour un ensemble de bishop
     * @param pieces liste de l'ensemble de bishop
     * @param restriction restriction concernant les mouvements
     */
    private void generate_bishop_move_or_attack(long pieces, long restriction) {

        while(pieces != 0) {
            int start = Long.numberOfTrailingZeros(pieces);
            long board_occ = board.global_occupancy();
            long new_moves = bishop_movebb(start, board_occ) & restriction;
            while(new_moves != 0){
                int target = Long.numberOfTrailingZeros(new_moves);
                moves.add(new Move(start + (target << 6)));
                new_moves &= new_moves - 1;
            }
            
            pieces &= pieces -1;
        }

    }

    /** Renvois sous forme d'un bitboard les attaques possibles des sliding pièces d'un joueur
     * @param rook liste des rook du joueur sur le board
     * @param bishop liste des bishop du joueur sur le board
     * @param queen liste des reines du joueur sur le board
     * @param all_pieces liste des pièces sur le board
     */

    private long slide_attack_bb(long rook, long bishop, long queen, long all_pieces){
        long attack = 0L;

        long line_slide = rook | queen;
        long diagonal_slide = bishop | queen;
        int piece_pos;

        while(line_slide != 0) {
            piece_pos = Long.numberOfTrailingZeros(line_slide);
            attack |= rook_movebb(piece_pos, all_pieces);

            line_slide &= line_slide - 1;
        }

        while(diagonal_slide != 0) {
            piece_pos = Long.numberOfTrailingZeros(diagonal_slide);
            attack |= bishop_movebb(piece_pos, all_pieces);

            diagonal_slide &= diagonal_slide - 1;
        }

        return attack;
    }

    /***********************************************************************************************************************/
    /******************************************************** KNIGHT *******************************************************/
    /***********************************************************************************************************************/

    /** Initialise tous les masques des coups possibles pour un knight */
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

    /** Renvois sous forme d'un bitboard les attaques possibles des knights d'un joueur
     * @param knight bitboard des knigths du joueur sur le board
     */

    private long knight_attack_bb(long knight){
        long attack = 0L;

        while(knight != 0){
            int i = Long.numberOfTrailingZeros(knight);
            attack |= knight_mask[i];
            knight &= knight -1;
        }

        return attack;
    }
    
    /** Ajoute les mouvements possibles des knights d'un joueur
     * @param knight bitboard des knights du joueur sur le board
     */
    private void generate_knight_move(long knight, long allowed_space){
        long empty = board.get_empty();
        
        // POUR CHACUNE DES PIECES ON APPLIQUE LE MASQUE CALCULER A L'INITIALISATION
        for(int i= Long.numberOfTrailingZeros(knight); i< 64; i++){
            if(((knight>>i) & 1) != 0) {
                long new_move = knight_mask[i] & empty & allowed_space;
                for(int j=Long.numberOfTrailingZeros(new_move); j<64; j++){
                    if(((new_move>>j) & 1) != 0) {
                        moves.add(new Move(i + (j<<6)));
                    }
                }
            }
        }

    }

    /** Ajoute les attaques possibles des knights d'un joueur
     * @param knight bitboard des knights du joueur sur le board
     */

    private void generate_knight_attack(long knight, long enemy_target){
        long enemy_pieces = enemy_target;

        // POUR CHACUNE DES PIECES ON APPLIQUE LE MASQUE CALCULER A L'INITIALISATION
        for(int i= Long.numberOfTrailingZeros(knight); i< 64; i++){
            if(((knight>>i) & 1) != 0) {
                long new_move = knight_mask[i] & enemy_pieces;
                for(int j=Long.numberOfTrailingZeros(new_move); j<64; j++){
                    if(((new_move>>j) & 1) != 0) {
                        moves.add(new Move(i + (j<<6)));
                    }
                }
            }
        }

    }


    /***********************************************************************************************************************/
    /******************************************************** PAWNS ********************************************************/
    /***********************************************************************************************************************/


    /** Renvois sous forme d'un bitboard les attaques à droite possibles des pions d'un joueur
     * @param white couleur du joueur dont c'est le tour
     * @param pawns bitboard des pions du joueur sur le board
     */

    private long pawn_right_attack_bb(boolean white, long pawns){
        return (white) ? (pawns << NORTH_EAST) & ~COLUMN_H : (pawns >> NORTH_EAST) & ~COLUMN_H;
    }

    /** Renvois sous forme d'un bitboard les attaques à gauche possibles des pions d'un joueur
     * @param white couleur du joueur dont c'est le tour
     * @param pawns bitboard des pions du joueur sur le board
     */

    private long pawn_left_attack_bb(boolean white, long pawns){
        return (white) ?  (pawns << NORTH_WEST) & ~COLUMN_A : (pawns >> NORTH_WEST) & ~COLUMN_A;
    }                                                              
    



    private void generate_white_pawn_moves(long pawn_movable, long restriction){

        long my_pawn = pawn_movable;
        long enemy_pieces = board.black_occupancy();
        long double_row = ROW_4;
        long last_row = ROW_8;
        long empty = board.get_empty();


        // GENERATION MOUVEMENT SIMPLE

        long new_pawn = (my_pawn << NORTH) & empty & restriction & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i-8) + (i<<6)));
            }
        }

        // GENERATION MOUVEMENT DOUBLE

        new_pawn = (my_pawn << 2 * NORTH) & empty & restriction & (empty << NORTH) & double_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i-16) + (i<<6)));
            }
        }

        // PROMOTIONS MOUVEMENT SIMPLE

        new_pawn = (my_pawn << NORTH) & empty & restriction & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i-8) + (i<<6) + (1<<14)));
                moves.add(new Move((i-8) + (i<<6) + (1<<12) + (1<<14)));
                moves.add(new Move((i-8) + (i<<6) + (2<<12) + (1<<14)));
                moves.add(new Move((i-8) + (i<<6) + (3<<12) + (1<<14)));
            }
        }
    }

    private void generate_white_pawn_attack(long pawn_movable, long enemy_target){

        long my_pawn = pawn_movable;
        long enemy_pieces = enemy_target;
        long last_row = ROW_8;
        long empty = board.get_empty();

        // GENERATION ATTAQUE GAUCHE

        long new_pawn = (my_pawn << NORTH_WEST) & enemy_pieces & ~COLUMN_A & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i-7) + (i<<6)));
            }
        }
        // GENERATION ATTAQUE DROITE

        new_pawn = (my_pawn << NORTH_EAST) & enemy_pieces & ~COLUMN_H & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i-9) + (i<<6)));
            }
        }

        // PROMOTIONS & ATTAQUE
        new_pawn = (my_pawn << NORTH_EAST) & enemy_pieces & ~COLUMN_H & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i-9) + (i<<6) + (1<<14)));
                moves.add(new Move((i-9) + (i<<6) + (1<<12) + (1<<14)));
                moves.add(new Move((i-9) + (i<<6) + (2<<12) + (1<<14)));
                moves.add(new Move((i-9) + (i<<6) + (3<<12) + (1<<14)));
            }
        }


        new_pawn = (my_pawn << NORTH_WEST) & enemy_pieces & ~COLUMN_A & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i-7) + (i<<6) + (1<<14)));
                moves.add(new Move((i-7) + (i<<6) + (1<<12) + (1<<14)));
                moves.add(new Move((i-7) + (i<<6) + (2<<12) + (1<<14)));
                moves.add(new Move((i-7) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

    }

    private void generate_black_pawn_moves(long pawn_movable, long restriction){

        long my_pawn = pawn_movable;
        long enemy_pieces = board.white_occupancy();
        long double_row = ROW_5;
        long last_row = ROW_1;
        long empty = board.get_empty();


        // GENERATION MOUVEMENT SIMPLE
        long new_pawn = (my_pawn >> NORTH) & restriction & empty & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i+8) + (i<<6)));
            }
        }

        // GENERATION MOUVEMENT DOUBLE

        new_pawn = (my_pawn >> 2 * NORTH) & restriction & empty & (empty >> NORTH) & double_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i+16) + (i<<6)));
            }
        }


        // PROMOTIONS

        new_pawn = (my_pawn >> NORTH) & empty & restriction & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i+8) + (i<<6) + (1<<14)));
                moves.add(new Move((i+8) + (i<<6) + (1<<12) + (1<<14)));
                moves.add(new Move((i+8) + (i<<6) + (2<<12) + (1<<14)));
                moves.add(new Move((i+8) + (i<<6) + (3<<12) + (1<<14)));
            }
        }


    }


    private void generate_black_pawn_attack(long pawn_movable, long enemy_target){

        long my_pawn = pawn_movable;
        long enemy_pieces = enemy_target;
        long last_row = ROW_1;
        long empty = board.get_empty();


         // GENERATION ATTAQUE GAUCHE

        long new_pawn = (my_pawn >> NORTH_WEST) & enemy_pieces & ~COLUMN_A & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i+7) + (i<<6)));
            }
        }
        // GENERATION ATTAQUE DROITE

        new_pawn = (my_pawn >> NORTH_EAST) & enemy_pieces & ~COLUMN_H & ~last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i+9) + (i<<6)));
            }
        }

        // PROMOTION

        new_pawn = (my_pawn >> NORTH_WEST) & enemy_pieces & ~COLUMN_A & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i+7) + (i<<6) + (1<<14)));
                moves.add(new Move((i+7) + (i<<6) + (1<<12) + (1<<14)));
                moves.add(new Move((i+7) + (i<<6) + (2<<12) + (1<<14)));
                moves.add(new Move((i+7) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

        new_pawn = (my_pawn >> NORTH_EAST) & enemy_pieces & ~COLUMN_H & last_row;
        for(int i= Long.numberOfTrailingZeros(new_pawn); i< 64; i++){
            if(((new_pawn>>i) & 1) != 0) {
                moves.add(new Move((i+9) + (i<<6) + (1<<14)));
                moves.add(new Move((i+9) + (i<<6) + (1<<12) + (1<<14)));
                moves.add(new Move((i+9) + (i<<6) + (2<<12) + (1<<14)));
                moves.add(new Move((i+9) + (i<<6) + (3<<12) + (1<<14)));
            }
        }

    }



}