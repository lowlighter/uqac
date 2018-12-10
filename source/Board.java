/** 
 * Plateau de jeu.
 * Fonctionne avec des bitboards.
 */
public class Board extends Bitboards {

    /** Couleur */
    public static boolean white;

    /**
     * Crée un nouveau plateau de jeu.
     * A voir comment on fera pour éviter de créer trop d'instance de Board pour les noeuds et si c'est couteux.
     */
    public Board(boolean is_white) {
        init(is_white);
        
    }

    public MoveGenerator generator;

    public void bestmove() {
        System.out.println("bestmove "+BestMove.compute(this, 2));
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

        generator =  new MoveGenerator(this);
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
     * @param white - Couleur du camp du moteur
     * Retourne l'indice la position du roi
     */
    public int get_king_position(boolean white) {
        long king_pawns = (white) ? bb_wk : bb_bk;
        return Long.numberOfTrailingZeros(king_pawns);
    }

}