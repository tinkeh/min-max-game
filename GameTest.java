package jump61;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.junit.Test;

public class GameTest {

    private void setUpStuff() {
        board = new MutableBoard(6);
        reader = new StringReader("start \\n");
    }
    @Test
    public void test() {
        setUpStuff();
    }
    MutableBoard board;
    Reader reader;
    Writer writer;

}
