package jump61;

import java.io.Reader;
import java.io.Writer;
import java.io.PrintWriter;

import java.util.Scanner;
import java.util.Random;

import static jump61.Color.*;
import static jump61.GameException.error;

/** Main logic for playing (a) game(s) of Jump61.
 *  @author P. N. Hilfinger
 */
class Game {

    /** Name of resource containing help message. */
    private static final String HELP = "jump61/Help.txt";

    /** A new Game that takes command/move input from INPUT, prints
     *  normal output on OUTPUT, prints prompts for input on PROMPTS,
     *  and prints error messages on ERROROUTPUT. The Game now "owns"
     *  INPUT, PROMPTS, OUTPUT, and ERROROUTPUT, and is responsible for
     *  closing them when its play method returns. */
    Game(Reader input, Writer prompts, Writer output, Writer errorOutput) {
        _board = new MutableBoard(Defaults.BOARD_SIZE);
        _readonlyBoard = new ConstantBoard(_board);
        _prompter = new PrintWriter(prompts, true);
        _inp = new Scanner(input);
        _inp.useDelimiter("(?m)\\p{Blank}*$|^\\p{Blank}*|\\p{Blank}+");
        _out = new PrintWriter(output, true);
        _err = new PrintWriter(errorOutput, true);
        _quit = false;
        _noMove = false;
        _humanRed = new HumanPlayer(this, RED);
        _humanBlue = new HumanPlayer(this, BLUE);
        _autoRed = new AI(this, RED, 4, _board);
        _autoBlue = new AI(this, BLUE, 4, _board);
        _blue = _autoBlue;
        _red = _humanRed;
    }

    /** Alternate constructor that makes testing this and AI significantly
     *  easier. Takes in INPUT, OUTPUT, and BOARD. */
    Game(Reader input, Writer output, MutableBoard board) {
        _board = board;
        _readonlyBoard = new ConstantBoard(_board);
        _prompter = new PrintWriter(output, true);
        _inp = new Scanner(input);
        _inp.useDelimiter("(?m)\\p{Blank}*$|^\\p{Blank}*|\\p{Blank}+");
        _out = new PrintWriter(output, true);
        _err = new PrintWriter(output, true);
        _quit = false;
        _noMove = false;
        _humanRed = new HumanPlayer(this, RED);
        _humanBlue = new HumanPlayer(this, BLUE);
        _autoRed = new AI(this, RED, 4, _board);
        _autoBlue = new AI(this, RED, 4, _board);
        _blue = _autoBlue;
        _red = _humanRed;
    }

    /** Returns a readonly view of the game board.  This board remains valid
     *  throughout the session. */
    Board getBoard() {
        return _readonlyBoard;
    }

    /** Play a session of Jump61.  This may include multiple games,
     *  and proceeds until the user exits.  Returns an exit code: 0 is
     *  normal; any positive quantity indicates an error.  */
    int play() {
        _out.println("Welcome to " + Defaults.VERSION);
        while (!_quit) {
            try {
                if (_playing && !_noMove) {
                    _red.makeMove();
                    checkForWin();
                    if (!_noMove && _playing) {
                        _blue.makeMove();
                        checkForWin();
                    }
                } else {
                    if (promptForNext()) {
                        readExecuteCommand();
                    } else {
                        _quit = true;
                    }
                }
            } catch (GameException e) {
                _out.println(e.getMessage());
            }
        }
        _out.flush();
        return 0;
    }

    /** Get a move from my input and place its row and column in
     *  MOVE.  Returns true if this is successful, false if game stops
     *  or ends first. */
    boolean getMove(int[] move) {
        while (_playing && _move[0] == 0 && promptForNext()) {
            readExecuteCommand();
        }
        if (_move[0] > 0) {
            move[0] = _move[0];
            move[1] = _move[1];
            _move[0] = 0;
            return true;
        } else {
            return false;
        }
    }

    /** Add a spot to R C, if legal to do so. */
    void makeMove(int r, int c) {
        try {
            if (_board.getCurrentPlayer().playableSquare(_board.color(r, c))) {
                _board.addSpot(_board.getCurrentPlayer(), r, c);
            } else {
                reportError("Square '%s', '%s' not a valid move"
                        + " for '%s'.", r, c, _board.getCurrentPlayer());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            reportError("Square '%s', '%s' not on the board.", r, c);
        }
    }

    /** Add a spot to square #N, if legal to do so. */
    void makeMove(int n) {
        makeMove(_board.row(n), _board.col(n));
    }

    /** Return a random integer in the range [0 .. N), uniformly
     *  distributed.  Requires N > 0. */
    int randInt(int n) {
        return _random.nextInt(n);
    }

    /** Send a message to the user as determined by FORMAT and ARGS, which
     *  are interpreted as for String.format or PrintWriter.printf. */
    void message(String format, Object... args) {
        _out.printf(format, args);
    }

    /** Check whether we are playing and there is an unannounced winner.
     *  If so, announce and stop play. */
    private void checkForWin() {
        int reds = _board.getNumRed();
        if (reds == _board.size() * _board.size() || (reds == 0
            && _board.numMoves() > 1)) {
            _playing = false;
            _winner = reds == 0 ? "Blue" : "Red";
            announceWinner();
            restartGame();
        }
    }

    /** Send announcement of winner to my user output. */
    private void announceWinner() {
        _out.printf("%s wins.\n", _winner);
    }

    /** Make player COLOR an AI for subsequent moves. */
    private void setAuto(String color) {
        _playing = false;
        if (color.equals("red")) {
            _red = _autoRed;
        } else if (color.equals("blue")) {
            _red = _autoRed;
        } else {
            throw error("Wrong arguments for command: auto '%s'",
                    color);
        }
    }

    /** Make color PLAYER take manual input from the user
     * for subsequent moves. */
    private void setManual(String player) {
        _playing = false;
        if (player.equals("red")) {
            _red = _humanRed;
        } else if (player.equals("blue")) {
            _blue = _humanBlue;
        } else {
            throw error("Wrong argument(s) for auto: '%s'",
                    player);
        }
    }

    /** Stop any current game and clear the board to its initial
     *  state. */
    private void clear() {
        _playing = false;
        _board.clear(_board.size());
    }

    /** Print the current board using standard board-dump format. */
    private void dump() {
        _out.println(_board);
    }

    /** Print a help message. */
    private void help() {
        Main.printHelpResource(HELP, _out);
    }

    /** Stop any current game and set the move number to N. */
    private void setMoveNumber(int n) {
        _board.setNumMoves(n);
    }

    /** Seed the random-number generator with SEED. */
    private void setSeed(String[] seed) {
        return;
    }

    /** Stop any current game and set the board to an empty N x N board
     *  with numMoves() == 0 and SIZE. */
    private void setSize(int size) {
        if (size < 1) {
            throw new NumberFormatException();
        }
        _playing = false;
        _board.clear(size);
    }

    /** Begin accepting moves for game.  If the game is won,
     *  immediately print a win message and end the game. */
    private void restartGame() {
        _board.setCurrentPlayer(RED);
        _playing = false;
        _board.clear(_board.size());
    }

    /** Read and execute one command.  Leave the input at the start of
     *  a line, if there is more input. */
    private void readExecuteCommand() {
        String command = _inp.nextLine();
        if (command.matches("[0-9]\\s+[0-9]")) {
            String[] stuff = command.split("\\s");
            _move[0] = Integer.parseInt(stuff[0]);
            _move[1] = Integer.parseInt(stuff[1]);
        } else {
            executeCommand(command);
        }
    }

    /** Gather arguments and execute command CMND.  Throws GameException
     *  on errors. */
    private void executeCommand(String cmnd) {
        cmnd = cmnd.toLowerCase();
        String[] commands = cmnd.split("\\s+");
        if (commands.length > 0) {
            cmnd = commands[0];
        }
        if (cmnd.matches("\\s*\n*") || cmnd.equals("\r\n")) {
            return;
        } else if (cmnd.equals("#")) {
            return;
        } else if (cmnd.equals("clear")) {
            clear();
        } else if (cmnd.equals("start")) {
            _playing = true;
        } else if (cmnd.equals("quit")) {
            System.exit(0);
        } else if (cmnd.equals("auto")) {
            try {
                setAuto(commands[1].toLowerCase());
            } catch (ArrayIndexOutOfBoundsException e) {
                throw error("Too few arguments for commnd: Auto");
            }
        } else if (cmnd.equals("help")) {
            help();
        } else if (cmnd.equals("seed")) {
            setSeed(commands);
        } else if (cmnd.equals("manual")) {
            setManual(commands[1].toLowerCase());
        } else if (cmnd.equals("size")) {
            try {
                int size = Integer.parseInt(commands[1]);
                setSize(size);
            } catch (NumberFormatException e) {
                throw error("Wrong arguments for size: '%s'", commands[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw error("Too few arguments for command: size");
            }
        } else if (cmnd.equals("move")) {
            try {
                int move = Integer.parseInt(commands[1]);
                setMoveNumber(move);
            } catch (NumberFormatException e) {
                throw error("Wrong arguments for size: '%s'", commands[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw error("Too few arguments for command: move");
            }
        } else if (cmnd.equals("set")) {
            try {
                set(commands[1], commands[2], commands[3], commands[4]);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw error("Too few arguments for command: 'set'");
            }
        } else if (cmnd.equals("dump")) {
            _out.println(_board);
        } else {
            throw error("bad command: '%s'", cmnd);
        }
    }

    /** sets square ROWSTR, COLSTR to SPOTS and COLOR. */
    private void set(String rowStr, String colStr, String spots, String color) {
        int row = -1;
        int numSpots = -1;
        int col = -1;
        try {
            row = Integer.parseInt(rowStr);
        } catch (NumberFormatException e) {
            throw error("Wrong argument for command set: '%s'", rowStr);
        }
        try {
            col = Integer.parseInt(colStr);
        } catch (NumberFormatException e) {
            throw error("Wrong argument for command set: '%s'", colStr);
        }
        try {
            numSpots = Integer.parseInt(spots);
        } catch (NumberFormatException e) {
            throw error("Wrong argument for command set: '%s'", spots);
        }
        _playing = false;
        Color player;
        if (color.equals("r")) {
            player = RED;
        } else if (color.equals("b")) {
            player = BLUE;
        } else {
            throw error("Wrong arguments for command: set");
        }
        _board.set(row, col, numSpots, player);
    }

    /** Print a prompt and wait for input. Returns true iff there is another
     *  token. */
    private boolean promptForNext() {
        if (_playing) {
            _out.print(_board.getCurrentPlayer() + "> ");
        } else {
            _out.print("> ");
        }
        _out.flush();
        return _inp.hasNext();
    }

    /** Send an error message to the user formed from arguments FORMAT
     *  and ARGS, whose meanings are as for printf. */
    void reportError(String format, Object... args) {
        _err.print("Error: ");
        _err.printf(format, args);
        _err.println();
        promptForNext();
        _out.println();
        _out.flush();
    }

    /** Writer on which to print prompts for input. */
    private final PrintWriter _prompter;
    /** Scanner from current game input.  Initialized to return
     *  newlines as tokens. */
    private final Scanner _inp;
    /** Outlet for responses to the user. */
    private final PrintWriter _out;
    /** Outlet for error responses to the user. */
    private final PrintWriter _err;

    /** The board on which I record all moves. */
    private final MutableBoard _board;
    /** A readonly view of _board. */
    private final Board _readonlyBoard;

    /** A pseudo-random number generator used by players as needed. */
    private final Random _random = new Random();

    /** True iff a game is currently in progress. */
    private boolean _playing;

    /** True iff the session is still going.  Once set to false, this should
     *  prompt the returning of a zero exit code in play(). */
    private boolean _inSession;

    /** Getter for _inSession.
     *  @return _inSession. */
    public boolean getInSession() {
        return _inSession;
    }

    /** sets _noMove to VAL. */
    public void setNoMove(boolean val) {
        _noMove = val;
    }

    /** returns _playing. */
    public boolean getPlaying() {
        return _playing;
    }
    /** Assigned when a winner is found. Used to announce which player won. */
    private String _winner;

    /** The color of player one. */
    private Color _playerOne;
    /** The color of player two. */
    private Color _playerTwo;

   /** Used to return a move entered from the console.  Allocated
     *  here to avoid allocations. */
    private final int[] _move = new int[2];
    /** true if we quit the program. */
    private boolean _quit;
    /** true iff player.getmove failed; means we should then try again. */
    private boolean _noMove;
    /** red pointer that is either a human or AI. */
    private Player _red;
    /** blue pointer that either a human or AI. */
    private Player _blue;
    /** red AI. */
    private AI _autoRed;
    /** blue AI. */
    private AI _autoBlue;
    /** red Human. */
    private HumanPlayer _humanRed;
    /** blue Human. */
    private HumanPlayer _humanBlue;
}
