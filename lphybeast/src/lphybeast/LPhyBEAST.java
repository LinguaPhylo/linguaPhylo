package lphybeast;

import lphy.core.LPhyParser;
import lphy.parser.REPL;

import java.io.*;
import java.nio.file.Paths;

public class LPhyBEAST {

    private static void source(BufferedReader reader, LPhyParser parser) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            parser.parse(line);
            line = reader.readLine();
        }
        reader.close();
    }


    public static void main(String[] args) throws IOException {

        String dir = System.getProperty("user.home") + "/WorkSpace/linguaPhylo/lphybeast/examples/";

        String infile = "simpleStructuredCoalescent.lphy";
        if (args.length > 0) {
            infile = args[0];
        }

        String outfile = infile.substring(0, infile.lastIndexOf('.')) + ".xml";

        LPhyParser parser = new REPL();

        // add path to file
        File file = Paths.get(dir, infile).toFile();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        source(reader, parser);

        BEASTContext context = new BEASTContext(parser);

        String fileNameStem = outfile.substring(0, outfile.indexOf("."));

        String xml = context.toBEASTXML(fileNameStem);

        PrintWriter writer = new PrintWriter(new FileWriter(Paths.get(dir, outfile).toFile()));

        writer.println(xml);
        writer.flush();
        writer.close();
    }
}
