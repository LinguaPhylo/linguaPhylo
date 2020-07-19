package lphy.beast2;

import beast.evolution.likelihood.TreeLikelihood;
import beast.util.XMLProducer;
import lphy.core.Alignment;
import lphy.core.LPhyParser;
import lphy.graphicalModel.RandomVariable;
import lphy.parser.REPL;

import java.io.*;

public class BEASTAnalysis {

    private static void source(BufferedReader reader, LPhyParser parser) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            parser.parse(line);
            line = reader.readLine();
        }
        reader.close();
    }


    public static void main(String[] args) throws IOException {

        String infile = "examples/simpleCoalescent.lphy";
        String outfile = "examples/simpleCoalescent.xml";


        LPhyParser parser = new REPL();

        File file = new File(infile);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        source(reader, parser);

        RandomVariable<Alignment> alignment = (RandomVariable<Alignment>)parser.getDictionary().get("D");

        BEAST2Context context = new BEAST2Context();
        String xml = context.toBEASTXML(alignment);

        PrintWriter writer = new PrintWriter(new FileWriter(outfile));

        writer.println(xml);
        writer.flush();
        writer.close();
    }
}
