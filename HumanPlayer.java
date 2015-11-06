package jump61;

/** A Player that gets its moves from manual input.
 *  @author Austin Gandy
 */
class HumanPlayer extends Player {

    /** A new player initially playing COLOR taking manual input of
     *  moves from GAME's input source. */
    HumanPlayer(Game game, Color color) {
        super(game, color);
        _game = game;
        _color = color;
    }

    /** Ask my game to make my next move.   Assumes that I am of the
     *  proper color and that the game is not yet won. */
    @Override
    void makeMove() {
        int[] move = new int[2];
        if (_game.getMove(move)) {
            _game.setNoMove(false);
            _game.makeMove(move[0], move[1]);
        } else {
            _game.setNoMove(true);
        }
        Game game = getGame();
        Board board = getBoard();
    }

    /** The game this player is playing. */
    private Game _game;
    /** The color of the player. */
    private Color _color;
}
