package jump61;

import static jump61.Color.*;

import java.util.ArrayList;

/** A Jump61 board state.
 *  @author Austin Gandy
 */
class MutableBoard extends Board {

    /** An N x N board in initial configuration. */
    MutableBoard(int N) {
        _N = N;
        _numSquares = _N * _N;
        _squares = new String[_N][_N];
        for (int i = 0; i < _N; i += 1) {
            for (int j = 0; j < _N; j += 1) {
                _squares[i][j] = "--";
            }
        }
        _moves = new ArrayList<String[][]>();
        _currentPlayer = RED;
    }

    /** A board whose initial contents are copied from BOARD0. Clears the
     *  undo history. */
    MutableBoard(Board board0) {
        _squares = board0.getSquares();
        _N = board0.size();
    }

    /** sets _currentPlayer to PLAYER. */
    public void setCurrentPlayer(Color player) {
        _currentPlayer = player;
    }

    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to 0. */
    @Override
    void clear(int N) {
        _N = N;
        _numMoves = 0;
        _squares = new String[N][N];
        for (int i = 0; i < size(); i += 1) {
            for (int j = 0; j < size(); j += 1) {
                _squares[i][j] = "--";
            }
        }
    }

    /** Copy the contents of BOARD into me. */
    @Override
    void copy(Board board) {
        _numBlue = 0;
        _numRed = 0;
        _numMoves = 0;
        if (board.size() !=  _N) {
            _squares = new String[board.size()][board.size()];
        }
        String[][] squares = board.getSquares();
        for (int i = 0; i < _squares.length; i += 1) {
            for (int j = 0; j < _squares.length; j += 1) {
                _squares[i][j] = squares[i][j];
                if (board.color(i + 1, j + 1) == BLUE) {
                    _numBlue += 1;
                } else if (board.color(i + 1, j + 1) == RED) {
                    _numRed += 1;
                }
            }
        }
    }

    /** Clears the board (sets squares all back to original state) and changes
     *  the size of the board to S. */
    public void setSize(int s) {
        _squares = new String[s][s];
        for (int i = 0; i < _squares.length; i += 1) {
            for (int j = 0; j < _squares.length; j += 1) {
                _squares[i][j] = "--";
            }
        }
        _numRed = 0;
        _numBlue = 0;
        _N = s;
    }

    @Override
    int size() {
        return _N;
    }

    @Override
    int spots(int r, int c) {
        assert r <= _N && c <= _N;
        assert r >= 1 && c >= 1;
        String spots = getSquare(r, c).substring(1);
        if (!spots.equals("-")) {
            return Integer.parseInt(spots);
        } else {
            return 0;
        }
    }

    @Override
    int spots(int n) {
        return spots(row(n), col(n));
    }

    @Override
    Color color(int r, int c) {
        assert r <= _N && c <= _N;
        String color = getSquare(r, c).substring(0, 1);
        if (color.equals("-")) {
            return WHITE;
        } else if (color.equals("r")) {
            return RED;
        } else {
            return BLUE;
        }
    }

    @Override
    Color color(int n) {
        return color(row(n), col(n));
    }

    @Override
    int numMoves() {
        return _numMoves;
    }

    @Override
    int numOfColor(Color color) {
        if (color == RED) {
            return _numRed;
        } else if (color == BLUE) {
            return _numBlue;
        } else {
            return _numSquares - _numRed - _numBlue;
        }
    }

    /** adds one spot to the square at row R column C with the color of PLAYER.
     *  checks to be sure PLAYER can play this square and adds a move to _moves
     *  so this method must only be called when player makes a move. Otherwise
     *  adding a spot must be done using setSpots. This method also changes
     *  the color of the square to PLAYER if necessary. */
    @Override
    void addSpot(Color player, int r, int c) {
        assert player.playableSquare(color(r, c));
        addMove();
        String spot = getSquare(r, c).substring(1);
        int spots;
        if (!spot.equals("-")) {
            spots = Integer.parseInt(getSquare(r, c).substring(1));
        } else {
            spots = 0;
        }
        setSpots(r, c, spots + 1);
        setColor(r, c, player);
        if (isOverfull(r, c) && _numRed != size() * size()
                && _numBlue != size() * size()) {
            jump(r, c);
        }
        _numMoves += 1;
        _currentPlayer = _currentPlayer.opposite();
    }

    @Override
    void addSpot(Color player, int n) {
        addSpot(player, row(n), col(n));
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Clear the undo
     *  history. */
    @Override
    void set(int r, int c, int num, Color player) {
        assert num >= 0;
        if (num == 0) {
            player = WHITE;
        }
        setColor(r, c, player);
        setSpots(r, c, num);
        _moves.clear();
    }

    /** retrieves the square associated with the given row R and column C.
     *  inputs are from 1 - N
     *  @return _square */
    private String getSquare(int r, int c) {
        assert r >= 1 && r <= size();
        assert c >= 1 && c <= size();
        return _squares[r - 1][c - 1];
    }

    /** checks for any changes between COLOR and the color of the square
     * R, C. */
    private void checkColor(int r, int c, Color color) {
        if (color(r, c) == WHITE && color != WHITE) {
            if (color == BLUE) {
                _numBlue += 1;
            } else {
                _numRed += 1;
            }
        } else {
            if (color(r, c) == RED && color == BLUE) {
                _numBlue += 1;
                if (_numRed > 0) {
                    _numRed -= 1;
                }
            } else if (color(r, c) == BLUE && color == RED) {
                _numRed += 1;
                if (_numBlue > 0) {
                    _numBlue -= 1;
                }
            }
        }
    }

    /** sets the square at row R and column C's color to COLOR. */
    public void setColor(int r, int c, Color color) {
        checkColor(r, c, color);
        String spots = spots(r, c) != 0 ? Integer.toString(spots(r, c)) : "-";
        String col = color.toString().substring(0, 1);
        _squares[r - 1][c - 1] = col + spots;
    }

    /** sets the spots on square R, C to SPOTS. */
    public void setSpots(int r, int c, int spots) {
        String col = color(r, c).toString().substring(0, 1);
        if (col.equals("w")) {
            col = "-";
        }
        _squares[r - 1][c - 1] = col + Integer.toString(spots);
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white).  Clear the undo history. */
    @Override
    void set(int n, int num, Color player) {
        set(row(n), col(n), num, player);
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Clear the undo
     *  history. */
    @Override
    void setMoves(int num) {
        assert num > 0;
        _numMoves = num;
    }

    /** Undo the effects one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    @Override
    void undo() {
        _squares = _moves.get(_moves.size() - 1);
        _moves.remove(_moves.size() - 1);
        _numMoves -= 1;
        _numRed = 0;
        _numBlue = 0;
        _currentPlayer = _currentPlayer.opposite();
        countColors();
    }

    /** Counts the number of reds and blues on the current board. */
    private void countColors() {
        for (int i = 1; i <= size(); i += 1) {
            for (int j = 1; j <= size(); j += 1) {
                Color color = color(i, j);
                if (color == BLUE) {
                    _numBlue += 1;
                } else if (color == RED) {
                    _numRed += 1;
                }
            }
        }
    }
    /** True if the given square on R, C is full and thus must jump.
     *  @return whether the square is overfull. */
    private boolean isOverfull(int r, int c) {
        return (spots(r, c) > neighbors(r, c));
    }


    /** Do all jumping on this board, assuming that initially, R, C is the only
     *  square that might be over-full. */
    private void jump(int r, int c) {
        Color col = color(r, c);
        if (_numRed != size() * size() && _numBlue != size() * size()) {
            setSpots(r, c, 1);
            checkAndSet(r - 1, c, col);
            checkAndSet(r + 1, c, col);
            checkAndSet(r, c - 1, col);
            checkAndSet(r, c + 1, col);
        }
    }

    /** makes sure the input is on the board and then adds a spot to square R, C
     *   and changes its color to COL. Calls jump if adding a spot makes the
     *   square over-full. */
    private void checkAndSet(int r, int c, Color col) {
        if (exists(r, c)) {
            setColor(r, c, col);
            setSpots(r, c, spots(r, c) + 1);
            if (isOverfull(r, c)) {
                jump(r, c);
            }
        }
    }
    /** Total combined number of moves by both sides. */
    protected int _numMoves;

    /** takes in an int MOVES and sets _numMoves. */
    public void setNumMoves(int moves) {
        _numMoves = moves;
    }
    /** Returns _numMoves. */
    public int getNumMoves() {
        return _numMoves;
    }

    /** Convenience variable: size of board (squares along one edge). */
    private int _N;
    /** A 2-dimensional array of squares of size N (determined when
     *  instantiated or defaults to size N = 6). Squares are represented as
     *  Strings starting as "--" and once set, have a color (r or c) and
     *  a number of dots. */
    private String[][] _squares;

    /** the number of red squares currently on the board. Initially zero unless
     *  we are constructing this Board with the input of another Board. Gets
     *  incremented every time .jump(int S) changes a square from blue to red
     *  and decremented every time a square is changed from red to blue. */
    private int _numRed;

    /** Returns _numRed. */
    @Override
    public int getNumRed() {
        return _numRed;
    }

    /** same as above but with blue. */
    private int _numBlue;

    /** Returns _numBlue. */
    @Override
    public int getNumBlue() {
        return _numBlue;
    }

    /** stores all board positions to support undo method. */
    private ArrayList<String[][]> _moves;

    /** Returns _moves. */
    public ArrayList<String[][]> getMoves() {
        return _moves;
    }
    /** adds a board position BOARD to _moves. */
    public void addMove() {
        String[][]copy = new String[size()][size()];
        for (int i = 0; i < _squares.length; i += 1) {
            for (int j = 0; j < _squares.length; j += 1) {
                copy[i][j] = _squares[i][j];
            }
        }
        _moves.add(copy);
    }
    /** the number of squares on the board. */
    private int _numSquares;

}
