package lphybeast;

import lphy.core.LPhyParser;
import lphy.parser.REPL;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    @Option(names = {"-n", "--nex"},    description = "BEAST 2 partitions defined in Nexus file")
    Path nexfile;
//    Map<String, String> partitionMap; // d1=primates.nex:noncoding|d2=primates.nex:coding   //
//    @Option(names = {"-m", "--mapping"}, description = "mapping file") Path mapfile;
//    @Option(names = {"-p", "--partition"}, split = "\\|", splitSynopsisLabel = "|",
//            description = "LPhy var <=> Nexus keyword")
//    Map<String, String> partitionMap;  // -m D1=firsthalf|D2=secondhalf

    public static void main(String[] args) throws IOException {

        int exitCode = new CommandLine(new LPhyBEAST()).execute(args);
        System.exit(exitCode);

    }


    @Override
    public Integer call() throws Exception { // business logic goes here...

        BufferedReader reader = new BufferedReader(new FileReader(infile.toFile()));

//        DataExchanger dataExchanger = null;
        if (nexfile != null) {
//            dataExchanger = new DataExchanger(nexfile, partitionMap);
            assert nexfile.toString().endsWith("nex") || nexfile.toString().endsWith("nexus");
            // TODO LoggerUtils.log.info print twice?
            System.out.println("Load the alignment from " + nexfile.getFileName());

//            dataExchanger.printPartMap(System.out);
            throw new UnsupportedOperationException("This feature moved to LPhy script.");

        }

        //*** Parse LPhy file ***//
        LPhyParser parser = new REPL();
        source(reader, parser);

        // If dataExchanger is null, then using simulated alignment
        BEASTContext context = new BEASTContext(parser);

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

    private static void source(BufferedReader reader, LPhyParser parser)
            throws IOException {
//        if (dataExchanger != null) dataExchanger.preloadArgs();

        String line = reader.readLine();
        while (line != null) {
            parser.parse(line);
            // replace dataframe to nexus ntax nchar
//            if (dataExchanger != null) {
//
//                if (dataExchanger.containsDF(line))
//                    dataExchanger.updateDF(parser);
//
//            }

            line = reader.readLine();
        }
        reader.close();
    }

}
