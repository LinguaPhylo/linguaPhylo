package lphy.core.functions;

import lphy.TimeTree;
import lphy.TimeTreeNode;
import lphy.core.functions.newickParser.NewickASTVisitor;
import lphy.core.functions.newickParser.NewickLexer;
import lphy.core.functions.newickParser.NewickParser;
import lphy.core.functions.newickParser.TreeParsingException;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.FunctionInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.utils.LoggerUtils;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class Newick extends DeterministicFunction<TimeTree> {

    final String paramName;

    public Newick(@ParameterInfo(name = "tree", description = "the tree in Newick format.") Value<String> x) {
        paramName = getParamName(0);
        setParam(paramName, x);
    }

    @FunctionInfo(name="newick",description = "A function that parses a tree from a newick formatted string.")
    public Value<TimeTree> apply() {
        Value<String> newickValue = (Value<String>)getParams().get(paramName);

        TimeTree tree = parseNewick(newickValue.value());

        return new Value<>(tree, this);
    }

    private TimeTree parseNewick(String newick) {

        CharStream charStream = CharStreams.fromString(newick);

        // Custom parse/lexer error listener
        BaseErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol,
                                    int line, int charPositionInLine,
                                    String msg, RecognitionException e) {
                throw new TreeParsingException(msg, charPositionInLine, line);
            }
        };

        // Use lexer to produce token stream

        NewickLexer lexer = new NewickLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Parse token stream to produce parse tree

        NewickParser parser = new NewickParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ParseTree parseTree = parser.tree();

        // Traverse parse tree, constructing BEAST tree along the way

        NewickASTVisitor visitor = new NewickASTVisitor();

        TimeTreeNode root = visitor.visit(parseTree);

        TimeTree tree = new TimeTree();
        tree.setRoot(root);

        LoggerUtils.log.info("Parsed tree: " + tree);

        return tree;
    }
}
