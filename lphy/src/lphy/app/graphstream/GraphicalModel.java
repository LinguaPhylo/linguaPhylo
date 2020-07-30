package lphy.app.graphstream;

import lphy.graphicalModel.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GraphicalModel {

    Graph graph;
    boolean drawConstants;

    public GraphicalModel(String graphId, String styleSheetPath, boolean drawConstants) {
        graph = new SingleGraph(graphId);

        graph.addAttribute("ui.stylesheet", "url('file://" + styleSheetPath + "')");

        this.drawConstants = drawConstants;
    }

    public void addFromParser(GraphicalModelParser parser) {
        for (Value value : parser.getAllValuesFromRoots()) {
            addNode(value);
            if (value instanceof RandomVariable) {
                addNode(((RandomVariable)value).getGenerativeDistribution());
            } else if (value.getGenerator() != null) {
                addNode(value.getGenerator());
            }
        }
        for (Value value : parser.getAllValuesFromRoots()) {
            addEdges(value);
            if (value instanceof RandomVariable) {
                addEdges(((RandomVariable)value).getGenerativeDistribution());
            } else if (value.getGenerator() != null) {
                addEdges(value.getGenerator());
            }
        }
    }

    public void addNode(GraphicalModelNode node) {

        String nodeId = node.getUniqueId();

        if (node instanceof Value && ((Value)node).isConstant() && !drawConstants) {
            return;
        }

        graph.addNode(nodeId);
        Node gsnode = graph.getNode(nodeId);

        if (node instanceof DeterministicFunction) {
            gsnode.setAttribute("ui.class", "function");
        } else if (node instanceof GenerativeDistribution) {
            gsnode.setAttribute("ui.class", "generativeDistribution");
            gsnode.setAttribute("ui.label", ((GenerativeDistribution)node).getName());
        } else if (node instanceof RandomVariable) {
            gsnode.setAttribute("ui.class", "randomVariable");
        } else if (node instanceof Value && ((Value)node).getGenerator() != null) {
            gsnode.setAttribute("ui.class", "functionValue");
        } else {
            gsnode.setAttribute("ui.class", "constant");
        }

        if (node instanceof Value && !((Value)node).isAnonymous()) {
            gsnode.setAttribute("ui.label", ((Value)node).getId());
        }
    }

    public void addEdges(GraphicalModelNode node) {

        String nodeId = node.getUniqueId();

        for (GraphicalModelNode child : (List<GraphicalModelNode>)node.getInputs()) {
            String childId = child.getUniqueId();

            if (graph.getNode(childId) != null) {
                String edgeId = childId + "->" + nodeId;
                graph.addEdge(edgeId, childId, nodeId, true);
            }
        }
    }

    public static void main(String[] args) throws IOException {

        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        //System.setProperty("org.graphstream.ui.layout", "org.graphstream.ui.layout.HierarchicalLayout");

        GraphicalModelParser parser = new GraphicalModelParser();

        String fileName = System.getProperty("user.dir") + File.separator + "examples" + File.separator + "errorModel2.lphy";
        String styleSheetFileName = System.getProperty("user.dir") + File.separator + "css" + File.separator + "graphicalModel.css";

        File file = new File(fileName);
        
        parser.source(file);

        GraphicalModel model = new GraphicalModel(fileName, styleSheetFileName, false);
        model.addFromParser(parser);

        Viewer viewer = new Viewer(model.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.addDefaultView(false);
        

        JFrame frame = new JFrame("Graphical Model");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(viewer.getDefaultView());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(dim.width * 9 / 10, dim.height * 9 / 10);
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);

        frame.setVisible(true);
    }
}
