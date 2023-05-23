package lphy.parser;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Value;

import java.util.Arrays;

public class ParserSingleton {
    private static lphy.core.LPhyParser PARSER = getInstance();
    public synchronized static lphy.core.LPhyParser getInstance() {
        if (PARSER == null) {
            PARSER = new REPL();
        }
        return PARSER;
    }

    public static Object parse(String cmd) {
        return parseModelBlock(cmd);
    }

    public static Object parseModelBlock(String cmd) {
        PARSER.clear();
        LPhyListenerImpl parser = new LPhyListenerImpl(PARSER, LPhyParser.Context.model);
        return parser.parse(cmd);
    }

    public static Object parseDataBlock(String cmd) {
        PARSER.clear();
        LPhyListenerImpl parser = new LPhyListenerImpl(PARSER, LPhyParser.Context.data);
        return parser.parse(cmd);
    }

    public static void main(String[] args) {

        Object res = parse("prod = [[1,2,3],[3,2,1]] * [[1,2,3],[3,2,1]];");

        Integer[][]  rV = ((Value<Integer[][]>) res).value();

        for (Integer[] row : rV) {
            System.out.println(Arrays.toString(row));
        }
    }

}
