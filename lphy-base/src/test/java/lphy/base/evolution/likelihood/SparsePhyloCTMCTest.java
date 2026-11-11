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

    // test without root sequence: the root is sampled from the root frequencies (equilibrium of JC)
    @Test
    void test2() {
        lphy.core.simulator.RandomUtils.setSeed(7);
        String trNewick = "((1:1.0, 2:1.0):1.0)";
        NewickParser parser = new NewickParser(new CommonTokenStream(new NewickLexer(CharStreams.fromString(trNewick))));
        TimeTreeNode root = new NewickASTVisitor().visit(parser.tree());
        this.tree = new TimeTree();
        this.tree.setRoot(root);
        Value<TimeTree> treeValue = new Value<>("tree", tree);

        Value<Double[][]> QValue = new JukesCantor(new Value<>("", 1)).apply();
        int L = 1000000;

        SparsePhyloCTMC sparse = new SparsePhyloCTMC(treeValue, new Value<>("", 1), null, QValue,
                null, null, new Value<>("", L), null, null);
        Alignment observe = sparse.sample().value();
        // the sampled root sequence
        Alignment rootSeq = sparse.getAlignment().getRoot();
        assertEquals(1, rootSeq.ntaxa());
        assertEquals(L, rootSeq.nchar());

        // root composition ~ equilibrium (0.25 each)
        int[] rootCounts = new int[4];
        for (int i = 0; i < L; i++) rootCounts[rootSeq.getState(0, i)]++;
        for (int k = 0; k < 4; k++)
            assertEquals(0.25, (double) rootCounts[k] / L, 0.002, "root state " + k);

        // JC, root-to-tip distance 2.0: P(A->A) and P(A->X), conditioned on root state A
        double transProbAA = 0.25 + 0.75 * Math.exp(-4.0 * 2.0 / 3.0);
        double transProbAX = 0.25 - 0.25 * Math.exp(-4.0 * 2.0 / 3.0);

        int[] counter = new int[4];
        int total = 0;
        for (String leaf : tree.getTaxaNames()) {
            for (int i = 0; i < L; i++) {
                if (rootSeq.getState(0, i) == 0) {
                    counter[observe.getState(leaf, i)]++;
                    total++;
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            double prob = (double) counter[k] / total;
            assertEquals(k == 0 ? transProbAA : transProbAX, prob, 0.005, "state " + k);
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

        // JC, root-to-tip distance 2.0
        double transProbAA = 0.25 + 0.75 * Math.exp(-4.0 * 2.0 / 3.0);
        double transProbAX = 0.25 - 0.25 * Math.exp(-4.0 * 2.0 / 3.0);

        for (int state = 0; state < counterPhylo.length; state++){
            double prob = (double) counter[state] / (double) total;
            double phyloProb = (double) counterPhylo[state] / (double) phyloTotal;
            if (state == 0){
                assertEquals(phyloProb, prob, 0.005);
                assertEquals(transProbAA, prob, 0.005);
            } else {
                assertEquals(phyloProb, prob, 0.005);
                assertEquals(transProbAX, prob, 0.005);
            }
        }
    }

    // sparse and PhyloCTMC simulate the same distribution: HKY (unequal exit rates), clock rate != 1,
    // site rates incl. zero, taxon names whose sort order differs from leaf order ("10" < "2")
    @Test
    void sameDistributionAsPhyloCTMC() {
        lphy.core.simulator.RandomUtils.setSeed(42);
        String trNewick = "((((1:0.3,2:0.3):0.4,(3:0.5,10:0.5):0.2):0.5,(4:0.9,5:0.9):0.3):0.6,((6:1.0,7:1.0):0.5,(8:0.2,9:0.2):1.3):0.3)";
        NewickParser parser = new NewickParser(new CommonTokenStream(new NewickLexer(CharStreams.fromString(trNewick))));
        TimeTree t = new TimeTree();
        t.setRoot(new NewickASTVisitor().visit(parser.tree()));
        Value<TimeTree> treeValue = new Value<>("tree", t);

        Double[] freq = {0.4, 0.1, 0.2, 0.3};
        Value<Double[][]> Q = new lphy.base.evolution.substitutionmodel.HKY(
                new Value<>("", 4.0), new Value<>("", freq), null).apply();
        int L = 200000;
        Double[] sr = new Double[L];
        for (int i = 0; i < L; i++) sr[i] = new double[]{0.0, 0.5, 1.0, 2.5}[i % 4];
        Value<Double[]> siteRates = new Value<>("siteRates", sr);
        Value<Number> mu = new Value<>("mu", 0.5);
        Value<Integer> Lv = new Value<>("L", L);

        // no root sequence: both sample it from the root frequencies
        Alignment s = new SparsePhyloCTMC(treeValue, mu, null, Q, siteRates, null, Lv, null, null).sample().value();
        Alignment p = new PhyloCTMC(treeValue, mu, null, Q, siteRates, null, Lv, null, null).sample().value();

        String[] names = t.getTaxaNames();
        for (int i = 0; i < names.length; i++) {
            int[] cs = new int[4], cp = new int[4];
            for (int site = 0; site < L; site++) {
                cs[s.getState(names[i], site)]++;
                cp[p.getState(names[i], site)]++;
            }
            for (int k = 0; k < 4; k++)
                assertEquals((double) cp[k] / L, (double) cs[k] / L, 0.006, "taxon " + names[i] + " state " + k);
            for (int j = i + 1; j < names.length; j++)
                assertEquals(pdist(p, names[i], names[j]), pdist(s, names[i], names[j]), 0.01,
                        "distance " + names[i] + "-" + names[j]);
        }
        // sites with rate 0 never change
        for (int site = 0; site < L; site += 4)
            for (String n : names)
                assertEquals(s.getState(names[0], site), s.getState(n, site));
    }

    // helper method calculate p distance
    private static double pdist(Alignment a, String x, String y) {
        int d = 0;
        for (int i = 0; i < a.nchar(); i++) if (!a.getState(x, i).equals(a.getState(y, i))) d++;
        return (double) d / a.nchar();
    }
}
