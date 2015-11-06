package jump61;

import java.util.ArrayList;

/** An automated Player.
 *  @author Austin Gandy
 */
class AI extends Player {

    /** A new player of GAME initially playing COLOR that chooses
     *  moves automatically looking into MAXDEPTH operates on
     *  BOARD. */
    AI(Game game, Color color, int maxDepth, MutableBoard board) {
        super(game, color);
        _game = game;
        _board = board;
        _color = color;
        _maxDepth = maxDepth;
    }

    @Override
    void makeMove() {
        int[] bestMove = new int[2];
        bestMove[0] = 1;
        bestMove[1] = 1;
        minimax(_maxDepth, _board, _color,  -Integer.MAX_VALUE,
                Integer.MAX_VALUE, bestMove);
        String message = _color + " moves " + bestMove[0] + " " + bestMove[1]
                + "\n";
        _game.message(message);
        _game.makeMove(bestMove[0],  bestMove[1]);
    }

    /** Recurses through all possible moves DEPTH moves forward on board B
     *  determining what is best for player P on board B updating ALPHA and
     *  BETA as it goes to prune the tree accordingly. Updates BESTMOVE and
     *  returns the score of the best move. */
    private int minimax(int depth, MutableBoard b, Color p,
            int alpha, int beta, int[] bestMove) {
        int score;
        if (depth == 0) {
            return staticEval(p, b);
        }
        ArrayList<Integer> children = findValidMoves(p, b);
        if (p == _color) {
            for (int i = 0; i < children.size(); i += 2) {
                b.addSpot(p,  children.get(i), children.get(i + 1));
                score = minimax(depth - 1, b, p.opposite(), alpha, beta,
                        bestMove);
                b.undo();
                if (score > alpha) {
                    alpha = score;
                    if (depth == _maxDepth) {
                        bestMove[0] = children.get(i);
                        bestMove[1] = children.get(i + 1);
                    }
                } else if (alpha >= beta) {
                    break;
                }
            }
            return alpha;
        } else {
            for (int i = 0; i < children.size(); i += 2) {
                b.addSpot(p,  children.get(0),  children.get(1));
                score = minimax(depth - 1, b, p.opposite(), alpha, beta,
                        bestMove);
                b.undo();
                if (score < beta) {
                    beta = score;
                    if (depth == _maxDepth) {
                        bestMove[0] = children.get(i);
                        bestMove[1] = children.get(i + 1);
                    }
                } else if (alpha >= beta) {
                    break;
                }
            }
            return beta;
        }
    }

    /** Checks every position on Board B and adds it to the ArrayList<int[]>
     *  provided it is a valid move for Player P. Returns this ArrayList. */
    private ArrayList<Integer> findValidMoves(Color p, Board b) {
        ArrayList<Integer> moves = new ArrayList<Integer>();
        for (int i = 1; i <= b.size(); i += 1) {
            for (int j = 1; j <= b.size(); j += 1) {
                if (b.isLegal(p, i, j)) {
                    moves.add(i);
                    moves.add(j);
                }
            }
        }
        return moves;
    }

    /** Returns heuristic value of board B for player P.
     *  Higher is better for P. */
    private int staticEval(Color p, Board b) {
        if (p == Color.RED) {
            return b.getNumRed();
        } else {
            return b.getNumBlue();
        }
    }

    /** Board this is playing on. */
    private MutableBoard _board;

    /** The game associated with this. */
    private Game _game;

    /** The color of this. */
    private Color _color;

    /** How many moves this looks ahead. */
    private int _maxDepth;
}


