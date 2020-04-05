package sdfomin.parser;

import java.text.ParseException;

public class ParserBase {

    /**
     *  symbols between operands
     */
    public final String DEFAULT_WHITESPACES = " \n\r\t";

    /**
     * text to parse
     */
    private String source;

    /**
     * current position of caret
     */
    private int pos = 0;

    /**
     * constructor
     * @param source text to parse
     */
    public ParserBase(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public int getPos() {
        return pos;
    }

    public char getCurrent() {
        return pos >= source.length() ? 0 : source.charAt(pos);
    }

    public boolean isEnd() {
        return getCurrent() == 0;
    }

    public void next() {
        if (!isEnd())
            pos++;
    }

    /**
     * move caret to next non-whitespace symbol
     */
    public void skip() {
        while (DEFAULT_WHITESPACES.contains(String.valueOf(getCurrent())))
            next();
    }

    /**
     * match string from caret to args
     * @param args strings to match
     * @return arg which found or null if nothing was found
     */
    private String matchNoExcept(String... args) {
        int pos = getPos();
        for (String s : args) {
            boolean match = true;
            for (int j = 0; j < s.length(); ++j) {
                if (getCurrent() == s.charAt(j))
                    next();
                else {
                    this.pos = pos;
                    match = false;
                    break;
                }
            }
            if (match) {
                skip();
                return s;
            }
        }
        return null;
    }

    /**
     * is string matching to args
     * @param terms string try to match
     * @return true if matched, otherwise false
     */
    public boolean isMatch(String... terms) {
        int pos = getPos();
        String result = matchNoExcept(terms);
        this.pos = pos;
        return result != null;
    }

    /**
     * try to match string with one of the terms
     * @param terms strings try to match with
     * @return term that was found
     * @throws ParseException if nothing was found
     */
    public String match(String... terms) throws ParseException {
        int pos = getPos();
        String result = matchNoExcept(terms);
        if (result == null) {
            StringBuilder message = new StringBuilder("One of the strings were expected: ");
            boolean first = true;
            for (String term : terms) {
                if (!first)
                    message.append(", ");
                message.append(String.format("\"%s\"", term));
                first = false;
            }
            throw new ParseException(message.toString(), pos);
        }
        return result;
    }

}

