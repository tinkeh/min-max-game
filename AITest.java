package jump61;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.junit.Test;

public class AITest {

    private void setUp() throws FileNotFoundException {
        board = new MutableBoard(2);
        reader = new StringReader("stub");
        writer = new PrintWriter("stub");
    }
    @Test
    public void test() {
        try {
            setUp();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        board.addSpot(Color.RED, 1, 1);
        Game game = new Game(reader, writer, board);
        AI testee = new AI(game, Color.BLUE, 4, board);
    }

    /** a mutable board. */
    private MutableBoard board;
    /** a reader. */
    private Reader reader;
    /** a writer. */
    private Writer writer;
    /** a game. */
    private Game game;
}
