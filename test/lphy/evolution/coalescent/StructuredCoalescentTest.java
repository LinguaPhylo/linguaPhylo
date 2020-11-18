package lphy.evolution.coalescent;

import junit.framework.TestCase;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphy.graphicalModel.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StructuredCoalescentTest extends TestCase {

    public List<List<TimeTreeNode>> setupNodes() {
        TimeTree tree = new TimeTree();

        List<List<TimeTreeNode>> nodes = new ArrayList<>();
        nodes.add(new ArrayList<>());
        nodes.add(new ArrayList<>());

        TimeTreeNode node0 = new TimeTreeNode("0", tree);
        node0.setMetaData(StructuredCoalescent.populationLabel, 0);
        TimeTreeNode node1 = new TimeTreeNode("1", tree);
        node1.setMetaData(StructuredCoalescent.populationLabel, 0);

        TimeTreeNode node2 = new TimeTreeNode("2", tree);
        node2.setMetaData(StructuredCoalescent.populationLabel, 1);
        TimeTreeNode node3 = new TimeTreeNode("3", tree);
        node3.setMetaData(StructuredCoalescent.populationLabel, 1);

        nodes.get(0).add(node0);
        nodes.get(0).add(node1);

        nodes.get(1).add(node2);
        nodes.get(1).add(node3);

        return nodes;
    }

    public void testPopulateRateMatrix() {

        List<List<TimeTreeNode>> nodes = setupNodes();

        Double[][] theta = {{1.0,0.1},{0.1, 1.0}};

        double[][] rates = new double[2][2];

        double totalRate = StructuredCoalescent.populateRateMatrix(nodes, theta, rates);

        double sum = 0.0;
        for (int i = 0; i < rates.length; i++) {
            System.out.println(Arrays.toString(rates[i]));
            for (int j = 0; j < rates[i].length; j++) {
                sum += rates[i][j];
            }
        }
        assertEquals(sum, totalRate, 1e-10);

    }

    public void testSelectRandomEvent() {

        List<List<TimeTreeNode>> nodes = setupNodes();

        Double[][] theta = {{1.0,0.1},{0.1, 1.0}};

        Integer[] k = {2,2};

        double[][] rates = new double[2][2];

        int[][] events = new int[2][2];

        StructuredCoalescent coalescent = new StructuredCoalescent(new Value<>("theta", theta),
                new Value<>("k", k), null, null);

        double totalRate = coalescent.populateRateMatrix(nodes, theta, rates);

        for (int i = 0; i < 5000; i++) {
            StructuredCoalescent.SCEvent event = coalescent.selectRandomEvent(rates, totalRate, 0);
            events[event.pop][event.toPop] += 1;
        }
    }
}
