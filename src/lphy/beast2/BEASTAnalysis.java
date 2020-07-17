package lphy.beast2;

import beast.evolution.likelihood.TreeLikelihood;
import beast.util.XMLProducer;
import lphy.core.Alignment;
import lphy.core.LPhyParser;
import lphy.graphicalModel.RandomVariable;
import lphy.parser.REPL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BEASTAnalysis {

    public static String toBEASTXML(RandomVariable<Alignment> alignment) {

        TreeLikelihood treeLikelihood = BEASTFactory.createTreeLikelihood(alignment);

        String xml = new XMLProducer().toXML(treeLikelihood);

        return xml;
    }

    private static void source(BufferedReader reader, LPhyParser parser) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            parser.parse(line);
            line = reader.readLine();
        }
        reader.close();
    }


    public static void main(String[] args) throws IOException {

        LPhyParser parser = new REPL();

        File file = new File("examples/simpleCoalescent.lphy");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        source(reader, parser);

        RandomVariable<Alignment> alignment = (RandomVariable<Alignment>)parser.getDictionary().get("D");

        System.out.println(toBEASTXML(alignment));
    }
}
