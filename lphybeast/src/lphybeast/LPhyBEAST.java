package lphybeast;

import lphy.core.LPhyParser;
import lphy.parser.REPL;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "lphybeast", version = "LPhyBEAST " + LPhyBEAST.VERSION, footer = "Copyright(c) 2020",
        description = "LPhyBEAST takes an LPhy model specification, and some data and produces a BEAST 2 XML file.")
public class LPhyBEAST implements Callable<Integer> {

    public static final String VERSION = "0.0.1 alpha";

    @Parameters(paramLabel = "LPhy", description = "File of the LPhy model specification")
    File infile;

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Option(names = {"-o", "--out"},     description = "BEAST 2 XML")  File outfile;
    @Option(names = {"-d", "--data"},    description = "File containing alignment or traits")
    File datafile;
    @Option(names = {"-m", "--mapping"}, description = "mapping file") File mapfile;


    public static void main(String[] args) throws IOException {

        int exitCode = new CommandLine(new LPhyBEAST()).execute(args);
        System.exit(exitCode);


//        String dir = System.getProperty("user.home") + "/WorkSpace/linguaPhylo/lphybeast/examples/";
//
//        String infile = "simpleStructuredCoalescent.lphy";
//        if (args.length > 0) {
//            infile = args[0];
//        }
//
//        String outfile = infile.substring(0, infile.lastIndexOf('.')) + ".xml";
//
//        LPhyParser parser = new REPL();
//
//        // add path to file
//        File file = Paths.get(dir, infile).toFile();
//        BufferedReader reader = new BufferedReader(new FileReader(file));
//        source(reader, parser);
//
//        BEASTContext context = new BEASTContext(parser);
//
//        String fileNameStem = outfile.substring(0, outfile.indexOf("."));
//
//        String xml = context.toBEASTXML(fileNameStem);
//
//        PrintWriter writer = new PrintWriter(new FileWriter(Paths.get(dir, outfile).toFile()));
//
//        writer.println(xml);
//        writer.flush();
//        writer.close();
    }


    @Override
    public Integer call() throws Exception { // business logic goes here...

        BufferedReader reader = new BufferedReader(new FileReader(infile));

        LPhyParser parser = new REPL();
        source(reader, parser);

        BEASTContext context = new BEASTContext(parser);

        //TODO
        String outfile = infile.getName().substring(0, infile.getName().lastIndexOf('.')) + ".xml";
        String fileNameStem = outfile.substring(0, outfile.indexOf("."));

        String xml = context.toBEASTXML(fileNameStem);

        PrintWriter writer = new PrintWriter(new FileWriter(outfile));

        writer.println(xml);
        writer.flush();
        writer.close();

        return 0;
    }

    private static void source(BufferedReader reader, LPhyParser parser) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            parser.parse(line);
            line = reader.readLine();
        }
        reader.close();
    }

}
