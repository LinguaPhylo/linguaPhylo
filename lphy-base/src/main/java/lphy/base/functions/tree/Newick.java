package lphy.base.functions.tree;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.base.parser.newick.NewickASTVisitor;
import lphy.base.parser.newick.NewickLexer;
import lphy.base.parser.newick.NewickParser;
import lphy.base.parser.newick.TreeParsingException;
import lphy.core.model.components.*;
import lphy.core.util.LoggerUtils;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class Newick extends DeterministicFunction<TimeTree> {

    public static final String treeParamName = "tree";

    public Newick(@ParameterInfo(name = treeParamName, description = "the tree in Newick format.") Value<String> x) {
        setParam(treeParamName, x);
    }

    @GeneratorInfo(name="newick",
            category = GeneratorCategory.TREE, examples = {"errorModel1.lphy"},
            description = "A function that parses a tree from a newick formatted string.")
    public Value<TimeTree> apply() {
        Value<String> newickValue = (Value<String>)getParams().get(treeParamName);

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

        TimeTree tree = new TimeTree(Taxa.createTaxa(root));
        tree.setRoot(root);

        LoggerUtils.log.info("Parsed tree: " + tree);

        return tree;
    }
}
