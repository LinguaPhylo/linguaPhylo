package lphy.core.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ParserTest {

    @Test
    public void testAssignment() {
        parse("a=3;");
        parse("a=2;b=a;");
        parse("a=3.0;b=3.0*a;");
        parse("a=3;b=3.0*a;");
        parse("a=3.0;b=3*a;");
    }

    @Test
    public void testSemicolon() {
        parse("a=3");
        parse("a=2;b=a");
    }

    /**
     * Helper method
     * @param cmd
     * @return
     */
    public static Object parse(String cmd) {
        Object o = null;
        try {
            o = ParserSingleton.parse(cmd);
        } catch (Exception e) {
            fail("CMD " + cmd + " failed to parse, Exception :\n" + e.getMessage());
        }
        assertNotNull(o);
        return o;
    }

}
