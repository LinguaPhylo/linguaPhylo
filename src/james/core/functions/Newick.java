package james.core.functions;

import james.TimeTree;
import james.TimeTreeNode;
import james.core.functions.newickParser.NewickASTVisitor;
import james.core.functions.newickParser.NewickLexer;
import james.core.functions.newickParser.NewickParser;
import james.core.functions.newickParser.TreeParsingException;
import james.graphicalModel.DeterministicFunction;
import james.graphicalModel.FunctionInfo;
import james.graphicalModel.ParameterInfo;
import james.graphicalModel.Value;
import james.graphicalModel.types.IntegerValue;
import james.utils.Message;
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

        Message.info("Parsed tree: " + tree, this);

        return tree;
    }
}
