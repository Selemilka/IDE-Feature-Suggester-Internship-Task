package sdfomin.parser;

/**
 * When program can't be interpreted
 */
public class InterpretException extends Exception {
    public InterpretException(String text) {
        super(text);
    }
}
