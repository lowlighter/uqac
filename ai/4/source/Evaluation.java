

public class Evaluation extends Constants {

    private static final int kingval      = 300000;
    private static final int queenval     = 900;
    private static final int rookval      = 500;
    private static final int bishopval    = 300;
    private static final int knightval    = 300;
    private static final int pawnval      = 100;

    private static final int CASTLE_BONUS = 10;

    /*
     * Piece value tables modify the value of each piece according to where it
     * is on the board.
     *
     * To orient these tables, each row of 8 represents one row (rank) of the
     * chessboard.
     *
     * !!! The first row is where white's pieces start !!!
     *
     * So, for example
     * having a pawn at d2 is worth -5 for white. Having it at d7 is worth
     * 20. Note that these have to be flipped over to evaluate black's pawns
     * since pawn values are not symmetric.
     */

    private static int pawnposWhite[][] =
            {
                    {0,  0,  0,  0,  0,  0,  0,  0},
                    {0,  0,  0, -5, -5,  0,  0,  0},
                    {0,  2,  3,  4,  4,  3,  2,  0},
                    {0,  4,  6, 10, 10,  6,  4,  0},
                    {0,  6,  9, 10, 10,  9,  6,  0},
                    {4,  8, 12, 16, 16, 12,  8,  4},
                    {5, 10, 15, 20, 20, 15, 10,  5},
                    {0,  0,  0,  0,  0,  0,  0,  0}
            };

    private static int pawnposBlack[][] =
            {
                    {0,  0,  0,  0,  0,  0,  0,  0},
                    {5, 10, 15, 20, 20, 15, 10,  5},
                    {4,  8, 12, 16, 16, 12,  8,  4},
                    {0,  6,  9, 10, 10,  9,  6,  0},
                    {0,  4,  6, 10, 10,  6,  4,  0},
                    {0,  2,  3,  4,  4,  3,  2,  0},
                    {0,  0,  0, -5, -5,  0,  0,  0},
                    {0,  0,  0,  0,  0,  0,  0,  0}
            };

    private static final int knightPosWhite[][] =
            {
                    {-50, -40, -30, -30, -30, -30, -40, -50},
                    {-40, -20,   0,   5,   5,   0, -20, -40},
                    {-30,   5,  10,  15,  15,  10,   5, -30},
                    {-30,   0,  15,  20,  20,  15,   0, -30},
                    {-30,   5,  15,  20,  20,  15,   5, -30},
                    {-30,   0,  10,  15,  15,  10,   0, -30},
                    {-40, -20,   0,   0,   0,   0, -20, -40},
                    {-50, -40, -30, -30, -30, -30, -40, -50}
            };

    private static final int knightPosBlack[][] =
            {
                    {-50, -40, -30, -30, -30, -30, -40, -50},
                    {-40, -20,   0,   0,   0,   0, -20, -40},
                    {-30,   0,  10,  15,  15,  10,   0, -30},
                    {-30,   5,  15,  20,  20,  15,   5, -30},
                    {-30,   0,  15,  20,  20,  15,   0, -30},
                    {-30,   5,  10,  15,  15,  10,   5, -30},
                    {-40, -20,   0,   5,   5,   0, -20, -40},
                    {-50, -40, -30, -30, -30, -30, -40, -50}
            };

    private static final int bishopPosWhite[][] =
            {
                    {-20, -10, -10, -10, -10, -10, -10, -20},
                    {-10,   5,   0,   0,   0,   0,   5, -10},
                    {-10,  10,  10,  10,  10,  10,  10, -10},
                    {-10,   0,  10,  10,  10,  10,   0, -10},
                    {-10,   5,   5,  10,  10,   5,   5, -10},
                    {-10,   0,   5,  10,  10,   5,   0, -10},
                    {-10,   0,   0,   0,   0,   0,   0, -10},
                    {-20, -10, -10, -10, -10, -10, -10, -20}
            };

    private static final int bishopPosBlack[][] =
            {
                    {-20, -10, -10, -10, -10, -10, -10, -20},
                    {-10,   0,   0,   0,   0,   0,   0, -10},
                    {-10,   0,   5,  10,  10,   5,   0, -10},
                    {-10,   5,   5,  10,  10,   5,   5, -10},
                    {-10,   0,  10,  10,  10,  10,   0, -10},
                    {-10,  10,  10,  10,  10,  10,  10, -10},
                    {-10,   5,   0,   0,   0,   0,   5, -10},
                    {-20, -10, -10, -10, -10, -10, -10, -20}
            };

    private static final int rookPosWhite[][] =
            {
                    {0,  0,  0,  5,  5,  0,  0,  0},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {5, 10, 10, 10, 10, 10, 10,  5},
                    {0,  5,  5,  5,  5,  5,  5,  0}
            };

    private static final int rookPosBlack[][] =
            {
                    {0,  5,  5,  5,  5,  5,  5,  0},
                    {5, 10, 10, 10, 10, 10, 10,  5},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {-5, 0,  0,  0,  0,  0,  0, -5},
                    {0,  0,  0,  5,  5,  0,  0,  0}
            };

    private static final int queenPosWhite[][] =
            {
                    {-20, -10, -10, -5, -5, -10, -10, -20},
                    {-10,   0,   5,  0,  0,   0,   0, -10},
                    {-10,   5,   5,  5,  5,   5,   0, -10},
                    {0,     0,   5,  5,  5,   5,   0, -5},
                    {-5,    0,   5,  5,  5,   5,   0, -5},
                    {-10,   0,   5,  5,  5,   5,   0, -10},
                    {-10,   0,   0,  0,  0,   0,   0, -10},
                    {-20, -10, -10, -5, -5, -10, -10, -20}
            };

    private static final int queenPosBlack[][] =
            {
                    {-20, -10, -10, -5, -5, -10, -10, -20},
                    {-10,   0,   0,  0,  0,   0,   0, -10},
                    {-10,   0,   5,  5,  5,   5,   0, -10},
                    {-5,    0,   5,  5,  5,   5,   0, -5},
                    {0,     0,   5,  5,  5,   5,   0, -5},
                    {-10,   5,   5,  5,  5,   5,   0, -10},
                    {-10,   0,   5,  0,  0,   0,   0, -10},
                    {-20, -10, -10, -5, -5, -10, -10, -20}
            };

    private static final int kingPosWhite[][] =
            {
                    {20,   30,  10,   0,   0,  10,  30,  20},
                    {20,   20,   0,   0,   0,   0,  20,  20},
                    {-10, -20, -20, -20, -20, -20, -20, -10},
                    {-20, -30, -30, -40, -40, -30, -30, -20},
                    {-30, -40, -40, -50, -50, -40, -40, -30},
                    {-30, -40, -40, -50, -50, -40, -40, -30},
                    {-30, -40, -40, -50, -50, -40, -40, -30},
                    {-30, -40, -40, -50, -50, -40, -40, -30}
            };

    private static final int kingPosBlack[][] =
            {
                    {-30, -40, -40, -50, -50, -40, -40, -30},
                    {-30, -40, -40, -50, -50, -40, -40, -30},
                    {-30, -40, -40, -50, -50, -40, -40, -30},
                    {-30, -40, -40, -50, -50, -40, -40, -30},
                    {-20, -30, -30, -40, -40, -30, -30, -20},
                    {-10, -20, -20, -20, -20, -20, -20, -10},
                    {20,   20,   0,   0,   0,   0,  20,  20},
                    {20,   30,  10,   0,   0,  10,  30,  20}
            };

    public int eval(Board state) {
        // Get current player
        boolean player = state.player_turn(); // si vrai = blanc
        int playerValue = 0;

        // Opponent is opposite of player
        boolean opponent = !player;
        int opponentValue = 0;

        long playerPieces = (player ? state.white_occupancy() : state.black_occupancy());
        playerValue = getValueOfPieces(playerPieces, player, state);

        long opponentPieces = (player ? state.black_occupancy() : state.white_occupancy());
        opponentValue = getValueOfPieces(opponentPieces, opponent, state);

        // Updates score based on castling
        if(!state.flagged((player ? WHITE_KING : BLACK_KING), playerPieces)) {
            playerValue += CASTLE_BONUS;
        }
        if(!state.flagged((opponent ? WHITE_KING : BLACK_KING), playerPieces)) {
            opponentValue += CASTLE_BONUS;
        }

        // Return the difference between our current score and opponents
        return playerValue - opponentValue;
    }


    private int getValueOfPieces(long pieces, boolean player, Board state) {



        int value = 0;
        for(int c = 0 ; c < 64 ; ++c) {
            char piece = state.at(1L << c);
            if (piece == EMPTY) continue;
            int color = Character.isLowerCase(piece) ? -1 : +1;
            char p = Character.toLowerCase(piece);

            int ligne = c/8;
            int col = c%8;

            switch (piece) {
                case ANY_PAWN:
                    int[][] pawnpos = (player) ? pawnposWhite : pawnposBlack;
                    value += pawnval + pawnpos[ligne][col];
                    break;
                case ANY_BISHOP:
                    int[][] bishoppos = (player) ? bishopPosWhite : bishopPosBlack;
                    value += bishopval + bishoppos[ligne][col];
                    break;
                case ANY_KNIGHT:
                    int[][] knightpos = (player) ? knightPosWhite : knightPosBlack;
                    value += knightval + knightpos[ligne][col];
                    break;
                case ANY_ROOK:
                    int[][] rookpos = (player) ? rookPosWhite : rookPosBlack;
                    value += rookval + rookpos[ligne][col];
                    break;
                case ANY_QUEEN:
                    int[][] queenpos = (player) ? queenPosWhite : queenPosBlack;
                    value += queenval + queenpos[ligne][col];
                    break;
                case ANY_KING:
                    int[][] kingpos = (player) ? kingPosWhite : kingPosBlack;
                    value += kingval + kingpos[ligne][col];
                    break;
            }
        }

        return value;
    }


}
