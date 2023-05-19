package lphy.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Default methods to guide how the LPhy parser acts.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public interface LPhyParserAction {

    /**
     * Implement this to provide a user-defined visitor,
     * and pass the required information to {@link #parse(String, AbstractParseTreeVisitor)}}.
     * @param CASentence   lphy code
     * @return   a user-defined result of the operation.
     */
    Object parse(String CASentence);

    /**
     * Sanitise the line, create the error listener,
     * then get tokens, and determine which parser to apply to tokens,
     * finally visit the parse tree returned from the parser.
     * @param CASentence                lphy code
     * @param visitor      the visitor for a parse tree
     * @return  a user-defined result of the operation.
     */
    static Object parse(String CASentence, AbstractParseTreeVisitor<Object> visitor) {
        // if no data{}, CASentence is empty, e.g. GraphicalModelInterpreter line 235
        if (!CASentence.endsWith(";") && !CASentence.trim().isEmpty())
            CASentence = CASentence + ";";

        // Custom parse/lexer error listener
        BaseErrorListener errorListener = new LPhyBaseErrorListener();

        // Get our lexer
        Lexer lexer = new LPhyLexer(CharStreams.fromString(CASentence));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        LPhyParser parser = new LPhyParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParseTree parseTree = parser.input();

//	    // Specify our entry point
//	    CasentenceContext CASentenceContext = parser.casentence();
//
//	    // Walk it and attach our listener
//	    ParseTreeWalker walker = new ParseTreeWalker();
//	    AntlrCompactAnalysisListener listener = new AntlrCompactAnalysisListener();
//	    walker.walk(listener, CASentenceContext);

        // TODO return null given DataModelParser
        // Traverse parse tree, constructing tree along the way
        return visitor.visit(parseTree);
    }
}
