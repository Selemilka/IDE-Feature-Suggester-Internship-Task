package sdfomin.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JetBrainsParserLibraryTest {

    @Test
    void isBlockStatementUpdated() {
        String program1 = "if () 10 * 2; 13 + 7;";
        String program2 = "if () { 10 * 2; 13 + 7; }";
        JetBrainsParser parse1 = new JetBrainsParser(program1);
        JetBrainsParser parse2 = new JetBrainsParser(program2);
        assertTrue(JetBrainsParserLibrary.isBlockStatementUpdated(parse1.parse(), parse2.parse()));
    }
}
