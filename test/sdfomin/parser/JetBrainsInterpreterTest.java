package sdfomin.parser;

import org.junit.jupiter.api.Test;

import javax.lang.model.type.ArrayType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JetBrainsInterpreterTest {

    @Test
    void execute() {
        String program1 = "@x = 10; @second = 20;\n if (second - 19) {x + 1;}" +
                "x*second + second/x*3;";
        JetBrainsParser parser = new JetBrainsParser(program1);
        try {
            ArrayList<Integer> output = JetBrainsInterpreter.execute(parser.parse());
            assertIterableEquals(output, Arrays.asList(11, 206));
        } catch (InterpretException e) {
            fail();
        }
    }

}