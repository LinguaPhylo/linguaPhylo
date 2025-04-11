package lphy.base.evolution.likelihood;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.CellPosition;
import lphy.base.evolution.Taxa;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.evolution.alignment.VariantStyleAlignment;
import lphy.base.evolution.substitutionmodel.JukesCantor;
import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.base.parser.newick.NewickASTVisitor;
import lphy.base.parser.newick.NewickLexer;
import lphy.base.parser.newick.NewickParser;
import lphy.core.model.Value;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SparsePhyloCTMCTest {
    TimeTree tree;

    // test alignment mapping
    @Test
    void test1() {
        // set the tree
        String trNewick = "((1:2.0, (2:1.0, 3:1.0):1.0):2.0, 4:4.0)";
        CharStream charStream = CharStreams.fromString(trNewick);
        NewickLexer lexer = new NewickLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NewickParser parser = new NewickParser(tokens);
        ParseTree parseTree = parser.tree();
        NewickASTVisitor visitor = new NewickASTVisitor();

        // lphy
        TimeTreeNode root = visitor.visit(parseTree);
        this.tree = new TimeTree();
        this.tree.setRoot(root);
        Value<TimeTree> treeValue = new Value<>("tree", tree);
        Alignment alignment = new SimpleAlignment(Taxa.createTaxa(1), 10000, SequenceType.NUCLEOTIDE);
        Value<Alignment> alignmentValue = new Value<>("alignment", alignment);

        // construct Q
        JukesCantor Q = new JukesCantor(new Value<>("",1));
        Value<Double[][]> QValue = Q.apply();

        SparsePhyloCTMC sparse = new SparsePhyloCTMC(treeValue,null, null, QValue,null,null, new Value<>("", alignment.nchar()), null, alignmentValue);
        sparse.sample();
        VariantStyleAlignment observe = sparse.getAlignment();

        Map<TimeTreeNode, Map<Integer,Integer>> diffs = sparse.getNodeDifferences();
        Map<CellPosition, Integer> variantStore = observe.getVariantStore();
        List<Integer> sites = new ArrayList<>();

        for (TimeTreeNode node : diffs.keySet()) {
            if (node.isLeaf()){
                for (Integer site: diffs.get(node).keySet()){
                    sites.add(site);
                    CellPosition cellPosition = new CellPosition(node.getId(),site);
                    assertEquals(diffs.get(node).get(site), variantStore.get(cellPosition));
                }
            }
        }

        for (TimeTreeNode node : diffs.keySet()) {
            if (!node.isLeaf() && node.getAllLeafNodes().size()==2){
                for (int site : diffs.get(node).keySet()){
                    if (!sites.contains(site)){
                        List<TimeTreeNode> leafs = node.getAllLeafNodes();
                        sites.add(site);
                        for (TimeTreeNode leaf : leafs){
                            CellPosition cellPosition = new CellPosition(leaf.getId(), site);
                            assertEquals(diffs.get(node).get(site), variantStore.get(cellPosition));
                        }
                    }
                }
            }
        }

        for (TimeTreeNode node : diffs.keySet()) {
            if (!node.isLeaf() && node.getAllLeafNodes().size()==3){
                for (int site : diffs.get(node).keySet()){
                    if (!sites.contains(site)){
                        List<TimeTreeNode> leafs = node.getAllLeafNodes();
                        sites.add(site);
                        for (TimeTreeNode leaf : leafs){
                            CellPosition cellPosition = new CellPosition(leaf.getId(), site);
                            assertEquals(diffs.get(node).get(site), variantStore.get(cellPosition));
                        }
                    }
                }
            }
        }
    }

    // test without root sequence
    @Test
    void test2() {
        // set the tree
        String trNewick = "((1:1.0, 2:1.0)3:1.0))";
        CharStream charStream = CharStreams.fromString(trNewick);
        NewickLexer lexer = new NewickLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NewickParser parser = new NewickParser(tokens);
        ParseTree parseTree = parser.tree();
        NewickASTVisitor visitor = new NewickASTVisitor();

        // lphy
        TimeTreeNode root = visitor.visit(parseTree);
        this.tree = new TimeTree();
        this.tree.setRoot(root);

        Value<TimeTree> treeValue = new Value<>("tree", tree);

        // construct Q
        JukesCantor Q = new JukesCantor(new Value<>("", 1));
        Value<Double[][]> QValue = Q.apply();

        Double[] rootFreqs = new Double[]{0.25, 0.25, 0.25, 0.25};
        Value<Double[]> rootFreqsValue = new Value<>("", rootFreqs);

        int L = 100000;

        // construct phyloctmcs
        SparsePhyloCTMC sparse = new SparsePhyloCTMC(treeValue, new Value<>("",1), null, QValue, null, null, new Value<>("", L), null, null);
        Alignment observe = sparse.sample().value();
        Alignment rootSeq = sparse.sampledRootSeq;

        // root sequence should be the same
//        for (int i = 0; i<rootSeq.nchar(); i++){
//            assertEquals(rootSeq.getState(0,i), observe.getState(0,i));
//        }

        // for sparse
        double transProbAA = 0.25 + 0.75 * Math.exp(-4*2/3);
        double transProbAX = 0.25 - 0.25 * Math.exp(-4*2/3);

        double[] sparseProbs = new double[4];

        // get all indices for A in root
        List<Integer> indicesA = new ArrayList<>();
        for (int i = 0; i < rootSeq.nchar(); i++){
            if (rootSeq.getState(0,i) == 0){
                indicesA.add(i);
            }
        }

        int[] counter = new int[4];
        int total = 0;
        for (int i = 1; i < 3; i++){
            for (int j = 0; j < indicesA.size(); j++){
                int state = observe.getState(String.valueOf(i), indicesA.get(j));
                counter[state] ++;
                total ++;
            }
        }

        for (int state = 0; state < counter.length; state++){
            double prob = (double) counter[state] / (double) total;
            sparseProbs[state] = prob;
        }

        PhyloCTMC phylo = new PhyloCTMC(treeValue, new Value<>("", 1), null, QValue,null,null, new Value<>("", L), null, null);
        Alignment theory = phylo.sample().value();

        double[] phyloProbs = new double[4];
        // get all indices for A in root
        // index 0 is the root
        List<Integer> indicesAPhylo = new ArrayList<>();
        for (int i = 0; i < theory.nchar(); i++){
            if (theory.getState(0,i) == 0){
                indicesAPhylo.add(i);
            }
        }
        int[] counterPhylo = new int[4];
        int totalPhylo = 0;
        for (int i =1 ; i < 3; i++) {
            for (int j = 0; j < indicesAPhylo.size(); j++) {
                int state = theory.getState(String.valueOf(i), indicesAPhylo.get(j));
                counterPhylo[state]++;
                totalPhylo++;
            }
        }

        for (int state = 0; state < counterPhylo.length; state++) {
            double prob = (double) counterPhylo[state] / (double) totalPhylo;
            phyloProbs[state] = prob;
        }

        for (int i = 0; i < phyloProbs.length; i++){
            //assertEquals(phyloProbs[i], sparseProbs[i], 0.001);
//            // TODO: make it equal to theory
//            if (i == 0){
//                assertEquals(transProbAA, phyloProbs[i], 0.005);
//            } else {
//                assertEquals(transProbAX, phyloProbs[i], 0.005);
//            }
        }
    }

    // test with root sequence
    @Test
    void test3() {
        // set the tree
        String trNewick = "((1:1.0, 2:1.0):1.0)";
        CharStream charStream = CharStreams.fromString(trNewick);
        NewickLexer lexer = new NewickLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NewickParser parser = new NewickParser(tokens);
        ParseTree parseTree = parser.tree();
        NewickASTVisitor visitor = new NewickASTVisitor();

        // lphy
        TimeTreeNode root = visitor.visit(parseTree);
        this.tree = new TimeTree();
        this.tree.setRoot(root);

        Value<TimeTree> treeValue = new Value<>("tree", tree);

        // construct Q
        JukesCantor Q = new JukesCantor(new Value<>("",1));
        Value<Double[][]> QValue = Q.apply();

        int L = 1000000;
        Alignment rootSeq = new SimpleAlignment(Taxa.createTaxa(1), L, SequenceType.NUCLEOTIDE);

        SparsePhyloCTMC sparse = new SparsePhyloCTMC(treeValue,new Value<>("", 1), null, QValue,null,null, new Value<>("", L), null, new Value<>("",rootSeq));
        Alignment observe = sparse.sample().value();

        PhyloCTMC phylo = new PhyloCTMC(treeValue, new Value<>("", 1), null, QValue,null,null, new Value<>("", L), null, new Value<>("",rootSeq));
        Alignment theory = phylo.sample().value();

        // calculate probs
        // get all indices for A in root
        List<Integer> indicesA = new ArrayList<>();
        for (int i = 0; i<rootSeq.nchar(); i++){
            if (rootSeq.getState(0,i) == 0){
                indicesA.add(i);
            }
        }

        // for sparse
        int[] counter = new int[4];
        String[] leaves = tree.getTaxaNames();
        int total = 0;
        for (int i = 0; i < leaves.length; i++){
            for (int j = 0; j < indicesA.size(); j++){
                int state = observe.getState(leaves[i], indicesA.get(j));
                counter[state] ++;
                total ++;
            }
        }

        // for phyloCTMC
        int[] counterPhylo = new int[4];
        int phyloTotal = 0;
        for (int i = 0; i < leaves.length; i++){
            for (int j = 0; j < indicesA.size(); j++){
                int state = theory.getState(leaves[i], indicesA.get(j));
                counterPhylo[state] ++;
                phyloTotal++;
            }
        }

        double transProbAA = 0.25 + 0.75 * Math.exp(-4*2/3);
        double transProbAX = 0.25 - 0.25 * Math.exp(-4*2/3);

        for (int state = 0; state < counterPhylo.length; state++){
            double prob = (double) counter[state] / (double) total;
            double phyloProb = (double) counterPhylo[state] / (double) phyloTotal;
            if (state == 0){
                assertEquals(phyloProb, prob, 0.005);
                assertEquals(transProbAA, prob, 0.05);
            } else {
                assertEquals(phyloProb, prob, 0.005);
                assertEquals(transProbAX, prob, 0.05);
            }
        }
    }
}
