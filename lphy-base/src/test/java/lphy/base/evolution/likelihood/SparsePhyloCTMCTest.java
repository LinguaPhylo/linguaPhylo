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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SparsePhyloCTMCTest {
    final int nTaxa = 16;
    TimeTree tree;

    @BeforeEach
    void setUp() {
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
    }

    /*
        check alignment mapping
         */
    @Test
    void test1() {
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

    /*
    check with original phyloCTMC
     */
    @Test
    void test2() {
        Value<TimeTree> treeValue = new Value<>("tree", tree);
        double branchLength = 0;
        for (TimeTreeNode node: tree.getNodes()){
            if (node != tree.getRoot()){
                double length = node.getParent().getAge() - node.getAge();
                branchLength += length;
            }
        }

        Alignment alignment = new SimpleAlignment(Taxa.createTaxa(1), 100, SequenceType.NUCLEOTIDE);
        Value<Alignment> alignmentValue = new Value<>("alignment", alignment);

        // construct Q
        JukesCantor Q = new JukesCantor(new Value<>("",1));
        Value<Double[][]> QValue = Q.apply();

        Double[] rootFreqs = new Double[4];

        double sum = 0;
        for (int i = 0; i< rootFreqs.length; i++){
            rootFreqs[i] = 0.25;
            sum += rootFreqs[i];
        }

        assertEquals(1, sum);

        SparsePhyloCTMC sparse = new SparsePhyloCTMC(treeValue,new Value<>("mu", 1), new Value<>("rootfreq", rootFreqs), QValue,null,null, new Value<>("", alignment.nchar()), null, alignmentValue);
        Alignment observe = sparse.sample().value();

        PhyloCTMC phylo = new PhyloCTMC(treeValue, new Value<>("mu", 1), new Value<>("rootfreq", rootFreqs), QValue,null,null, new Value<>("", alignment.nchar()), null, alignmentValue);
        Alignment theory = phylo.sample().value();

        // calculate probs
        // get all indices for A in root
        List<Integer> indicesA = new ArrayList<>();
        for (int i = 0; i<alignment.nchar(); i++){
            if (alignment.getState(0,i) == 0){
                indicesA.add(i);
            }
        }

        // for sparse
        // get states in taxon 1 at indices
        Map<Integer, Integer> APresent = new HashMap<>();
        String[] leaves = tree.getTaxaNames();
        for (int i = 0; i<indicesA.size(); i++){
            for (int j = 0; j < leaves.length; j++){
                int state = observe.getState(leaves[j], i);
                if (state == 0){
                    APresent.put(0, (APresent.get(0) == null) ? 1 : APresent.get(0) + 1);
                } else if (state == 1){
                    APresent.put(1, (APresent.get(1) == null) ? 1 : APresent.get(1) + 1);
                } else if (state == 2){
                    APresent.put(2, (APresent.get(2) == null) ? 1 : APresent.get(2) + 1);
                } else if (state == 3){
                    APresent.put(3, (APresent.get(3) == null) ? 1 : APresent.get(3) + 1);
                }
            }
        }

        // for phyloCTMC
        // get states in taxon 1 at indices
        Map<Integer, Integer> APresentPhylo = new HashMap<>();
        for (int i = 0; i<indicesA.size(); i++){
            for (int j = 0; j < leaves.length; j++){
                int state = theory.getState(leaves[j], i);
                if (state == 0){
                    APresentPhylo.put(0, (APresentPhylo.get(0) == null) ? 1 : APresentPhylo.get(0) + 1);
                } else if (state == 1){
                    APresentPhylo.put(1, (APresentPhylo.get(1) == null) ? 1 : APresentPhylo.get(1) + 1);
                } else if (state == 2){
                    APresentPhylo.put(2, (APresentPhylo.get(2) == null) ? 1 : APresentPhylo.get(2) + 1);
                } else if (state == 3){
                    APresentPhylo.put(3, (APresentPhylo.get(3) == null) ? 1 : APresentPhylo.get(3) + 1);
                }
            }
        }

        int totalSparse = APresent.values().stream().mapToInt(a -> a).sum();
        int totalPhylo = APresentPhylo.values().stream().mapToInt(a -> a).sum();

        assertEquals(APresent.get(0)/totalSparse, APresentPhylo.get(0)/totalPhylo);
        assertEquals(APresent.get(1)/totalSparse, APresentPhylo.get(1)/totalPhylo);
        assertEquals(APresent.get(2)/totalSparse, APresentPhylo.get(2)/totalPhylo);
        assertEquals(APresent.get(3)/totalSparse, APresentPhylo.get(3)/totalPhylo);
    }
}
