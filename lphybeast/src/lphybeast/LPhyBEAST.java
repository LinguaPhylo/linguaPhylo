package lphybeast;

import lphy.core.LPhyParser;
import lphy.parser.REPL;
import lphybeast.tobeast.data.DataExchanger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "lphybeast", version = "LPhyBEAST " + LPhyBEAST.VERSION, footer = "Copyright(c) 2020",
        description = "LPhyBEAST takes an LPhy model specification, and some data and produces a BEAST 2 XML file.")
public class LPhyBEAST implements Callable<Integer> {

    public static final String VERSION = "0.0.1 alpha";

    @Parameters(paramLabel = "LPhy", description = "File of the LPhy model specification")
    Path infile;

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

//    @Option(names = {"-wd", "--workdir"}, description = "Working directory") Path wd;
    @Option(names = {"-o", "--out"},     description = "BEAST 2 XML")  Path outfile;
    @Option(names = {"-n", "--nex"},    description = "BEAST 2 Nexus file containing alignment or traits")
    Path nexfile;
//    @Option(names = {"-m", "--mapping"}, description = "mapping file") Path mapfile;
    // TODO Mutually Dependent to -n
    @Option(names = {"-m", "--varmap"}, split = "\\|", splitSynopsisLabel = "|",
            description = "LPhy var <=> Nexus keyword")
    Map<String, String> varmap;

    public static void main(String[] args) throws IOException {

        int exitCode = new CommandLine(new LPhyBEAST()).execute(args);
        System.exit(exitCode);

    }


    @Override
    public Integer call() throws Exception { // business logic goes here...

        BufferedReader reader = new BufferedReader(new FileReader(infile.toFile()));

        DataExchanger dataExchanger = null;
        if (nexfile != null) {
            assert nexfile.toString().endsWith("nex") || nexfile.toString().endsWith("nexus");
            // TODO LoggerUtils.log.info print twice?
            System.out.println("Load the alignment from " + nexfile.getFileName());

            dataExchanger = new DataExchanger(nexfile, varmap);
            dataExchanger.printVarMap(System.out);

        }

        //*** Parse LPhy file ***//
        LPhyParser parser = new REPL();
        source(reader, parser, dataExchanger);

        // If dataExchanger is null, then using simulated alignment
        BEASTContext context = new BEASTContext(parser, dataExchanger);

        //*** Write BEAST 2 XML ***//
//        String wkdir = infile.getParent().toString();
        String fileName = infile.getFileName().toString();
        String fileNameStem = fileName.substring(0, fileName.indexOf("."));
        // avoid to add dir into fileNameStem passed into XML logger
        String xml = context.toBEASTXML(fileNameStem);

        if (outfile == null) {
            String outPath = infile.toString().substring(0, infile.toString().indexOf(".")) + ".xml";
            // create outfile in the same dir of infile as default
            outfile = Paths.get(outPath);
        }

        PrintWriter writer = new PrintWriter(new FileWriter(outfile.toFile()));
        writer.println(xml);
        writer.flush();
        writer.close();

        System.out.println("\nCreate BEAST 2 XML : " + Paths.get(System.getProperty("user.dir"), outfile.toString()));
        return 0;
    }

    private static void source(BufferedReader reader, LPhyParser parser, DataExchanger dataExchanger)
            throws IOException {
        if (dataExchanger != null) dataExchanger.preloadArgs();

        String line = reader.readLine();
        while (line != null) {
            parser.parse(line);
            // replace real data in ArgI
//            if (dataExchanger != null) {
//
//                if (!dataExchanger.containsArg(line))
//                    dataExchanger.updateArgs(parser);
//
//            }

            line = reader.readLine();
        }
        reader.close();
    }

}
