package lphy.phylospec.convert;

import lphy.core.codebuilder.CanonicalCodeBuilder;
import lphy.core.io.FileConfig;
import lphy.core.logger.FileLoggerListener;
import lphy.core.model.Value;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModelUtils;
import lphy.core.simulator.NamedRandomValueSimulator;
import lphy.core.simulator.RandomUtils;
import lphy.core.simulator.Sampler;
import lphy.core.simulator.SimulatorListener;
import org.phylospec.ast.AstNode;
import org.phylospec.ast.Stmt;
import org.phylospec.ast.transformers.EvaluateLiterals;
import org.phylospec.ast.transformers.EvaluateScalarFunctions;
import org.phylospec.ast.transformers.RemoveGroupings;
import org.phylospec.components.ComponentResolver;
import org.phylospec.errors.Error;
import org.phylospec.lexer.Lexer;
import org.phylospec.lexer.Range;
import org.phylospec.lexer.Token;
import org.phylospec.parser.Parser;
import org.phylospec.tiling.errors.TileApplicationError;
import org.phylospec.typeresolver.TypeError;
import org.phylospec.typeresolver.TypeResolver;
import org.phylospec.typeresolver.VariableResolver;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

/**
 * Runs a PhyloSpec script end to end: parses and type-checks it with phylospec-core, converts it
 * into an LPhy model ({@link PhyloSpecToLPhy}), simulates it and logs the simulated values with
 * LPhy's {@link FileLoggerListener}, the way {@code slphy} does. See CLASS_DESIGN3.md §4.
 * <p>
 * Usage: {@code PhyloSpecToLPhyRunner <file.phylospec> [-r n] [-seed s] [-No id;id] [-o dir]}
 * @author Walter Xie
 */
@CommandLine.Command(name = "phylospec2lphy", mixinStandardHelpOptions = true,
        description = "Simulate a PhyloSpec model with LPhy and log the simulated values.")
public class PhyloSpecToLPhyRunner implements Callable<Integer> {

    public static final String PHYLOSPEC_EXTENSION = ".phylospec";

    @CommandLine.Parameters(paramLabel = "PhyloSpec_script",
            description = "The file path of the PhyloSpec model, with the extension '" + PHYLOSPEC_EXTENSION + "'.")
    Path infile;

    @CommandLine.Option(names = {"-r", "--replicates"}, defaultValue = "1", showDefaultValue = ALWAYS,
            description = "the number of simulations to run.")
    int numReplicates = 1;

    @CommandLine.Option(names = {"-seed", "--seed"}, description = "the seed.")
    Long seed;

    @CommandLine.Option(names = {"-No", "--notlog"}, split = ";",
            description = "random variables not to log (id or canonical id), e.g. -No \"D;psi\".")
    String[] varNotLog;

    @CommandLine.Option(names = {"-o", "--outdir"},
            description = "the output directory, default to the folder of the PhyloSpec script.")
    Path outDir;

    public static void main(String[] args) {
        System.exit(new CommandLine(new PhyloSpecToLPhyRunner()).execute(args));
    }

    @Override
    public Integer call() throws IOException {
        File scriptFile = infile.toFile().getAbsoluteFile();
        String fileName = scriptFile.getName();
        if (!fileName.endsWith(PHYLOSPEC_EXTENSION))
            throw new CommandLine.ParameterException(new CommandLine(this),
                    "The script must have the extension '" + PHYLOSPEC_EXTENSION + "' : " + infile);
        String filePrefix = fileName.substring(0, fileName.length() - PHYLOSPEC_EXTENSION.length());
        File dir = (outDir != null ? outDir.toFile() : scriptFile.getParentFile()).getAbsoluteFile();

        // the same as slphy: user.dir is the script's folder, so relative paths in it resolve there
        FileConfig.Utils.validate(scriptFile, dir);
        if (seed != null) RandomUtils.setSeed(seed);

        String source = Files.readString(scriptFile.toPath());
        REPL dict;
        try {
            dict = convert(source, filePrefix);
        } catch (ConversionException e) {
            System.err.println(e.toStdOutString(source));
            return 65;
        }

        simulateAndLog(dict, numReplicates, varNotLog, dir, filePrefix);

        File lphyFile = new File(dir, filePrefix + ".lphy");
        Files.writeString(lphyFile.toPath(), new CanonicalCodeBuilder().getCode(dict));
        System.out.println("Wrote the converted LPhy model to " + lphyFile);
        return 0;
    }

    /**
     * Parses, type-checks and converts a PhyloSpec script (§4 steps 1 and 3).
     * @param source the PhyloSpec script
     * @param name   the name of the LPhy model
     * @return the LPhy model, with values drawn for every random variable
     * @throws ConversionException if the script has syntax or type errors, or cannot be converted
     */
    public static REPL convert(String source, String name) {
        List<Error> errors = new ArrayList<>();

        Lexer lexer = new Lexer(source);
        lexer.registerEventListener(errors::add);
        List<Token> tokens = lexer.scanTokens();
        Parser parser = new Parser(tokens);
        parser.registerEventListener(errors::add);
        List<Stmt> statements = parser.parse();
        if (!errors.isEmpty()) throw new ConversionException(errors);

        // AttachComponentNamespaces must not run: the converter looks generators up by bare name
        statements = new RemoveGroupings().transform(statements);
        statements = new EvaluateLiterals().transform(statements);
        statements = new EvaluateScalarFunctions().transform(statements);

        VariableResolver variableResolver = new VariableResolver(statements);
        ComponentResolver componentResolver;
        try {
            componentResolver = new ComponentResolver(ComponentResolver.loadCoreComponentLibraries());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load the PhyloSpec core component library", e);
        }
        TypeResolver typeResolver = new TypeResolver(componentResolver);
        typeResolver.registerEventListener(errors::add);
        try {
            typeResolver.visitStatements(statements);
        } catch (TypeError e) {
            errors.add(e.toError(range(parser, e.getAstNode())));
        }
        if (!errors.isEmpty()) throw new ConversionException(errors);

        REPL dict = new REPL();
        dict.setName(name);
        try {
            new PhyloSpecToLPhy(dict, variableResolver, componentResolver, PhyloSpecNames.load()).convert(statements);
        } catch (TileApplicationError e) {
            throw new ConversionException(List.of(e.toError(range(parser, e.getAstNode()))));
        }
        return dict;
    }

    private static Range range(Parser parser, AstNode node) {
        return node == null ? null : parser.getRangeForAstNode(node);
    }

    /**
     * Simulates the model and logs it, the way slphy does (§4 step 5). Replicate 0 is the model as
     * converted; each later replicate resamples it in place.
     * @param dict          the converted model
     * @param numReplicates the number of simulations
     * @param varNotLog     random variables not to log, can be null
     * @param outDir        the output directory, or null for LPhy's current one
     * @param filePrefix    the prefix of the output files
     * @return the logged random values of each replicate, keyed by replicate index
     */
    public static Map<Integer, List<Value>> simulateAndLog(REPL dict, int numReplicates, String[] varNotLog,
                                                           File outDir, String filePrefix) {
        if (numReplicates < 1)
            throw new IllegalArgumentException("The replicate must be at least 1 time ! But numReplicates = " + numReplicates);

        // Sampler reads this JVM-global flag when constructed: resample the converted model in place,
        // rather than printing it as .lphy and parsing it again for every sample
        boolean sampleUsingParser = LPhyParserDictionary.Utils.isSampleValuesUsingParser();
        LPhyParserDictionary.Utils.setSampleValuesUsingParser(false);
        try {
            Sampler sampler = new Sampler(dict);

            // Sampler.sampleAll starts a logger with numReplicates only, which FileLoggerListener rejects
            FileLoggerListener logger = new FileLoggerListener();
            if (outDir != null) logger.setOutputDir(outDir.getAbsolutePath());
            logger.start(numReplicates, filePrefix);

            Map<Integer, List<Value>> allReplicates = new TreeMap<>();
            for (int i = SimulatorListener.REPLICATES_START_INDEX; i < numReplicates; i++) {
                // sample(seed) would re-seed every replicate, making them identical: pass null
                List<Value> values = i == SimulatorListener.REPLICATES_START_INDEX
                        ? GraphicalModelUtils.getAllValuesFromSinks(dict) : sampler.sample(null);
                List<Value> namedRandomValues = NamedRandomValueSimulator.getNamedRandomValues(values, varNotLog);
                allReplicates.put(i, namedRandomValues);
                logger.replicate(i, namedRandomValues);
            }
            logger.complete();
            return allReplicates;
        } finally {
            LPhyParserDictionary.Utils.setSampleValuesUsingParser(sampleUsingParser);
        }
    }

    /** Syntax, type or conversion errors, each with its source range when known. */
    public static class ConversionException extends RuntimeException {
        private final List<Error> errors;

        public ConversionException(List<Error> errors) {
            super(String.join("\n", errors.stream().map(ConversionException::describe).toList()));
            this.errors = List.copyOf(errors);
        }

        public List<Error> getErrors() {
            return errors;
        }

        private static String describe(Error error) {
            return (error.range() == null ? "" : error.range() + " ") + error;
        }

        /** The errors with the problematic code highlighted, for printing to a terminal. */
        public String toStdOutString(String source) {
            return String.join("\n", errors.stream()
                    .map(e -> e.range() == null ? describe(e) : e.toStdOutString(source)).toList());
        }
    }
}
