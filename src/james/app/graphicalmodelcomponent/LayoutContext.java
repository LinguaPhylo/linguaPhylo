package james.app.graphicalmodelcomponent;

import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.Parameterized;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import java.util.*;

public class LayoutContext {

    private Map<Object, NodeLayout> nodes = new HashMap<>();
    private List<List<NodeLayout>> nodesByLevel = new ArrayList<>();

    int lastWidth = 0;
    int lastHeight = 0;

    NodeLayout rootNode = null;

    GraphicalModelParser parser;
    private boolean showAllNodes = true;

    public LayoutContext(GraphicalModelParser parser, boolean showAllNodes) {
        this.parser = parser;
        this.showAllNodes = showAllNodes;
    }

    public void addRoot(Value v) {

        NodeLayout renderNode = createNode(v, null);

        //addNodeByLevel(renderNode);
    }

    private void addNode(NodeLayout node) {
        nodes.put(node.value(), node);
        addNodeByLevel(node);
    }

    private void addNodeByLevel(NodeLayout node) {
        while (nodesByLevel.size() <= node.level) {
            nodesByLevel.add(new ArrayList<>());
        }
        List<NodeLayout> level =  nodesByLevel.get(node.level);
        if (!level.contains(node)) {
            level.add(node);
        }
    }

    private void relevel() {
        for (List<NodeLayout> level : nodesByLevel) {
            level.clear();
        }
        nodesByLevel.clear();

        traverseToRelevel(rootNode);
    }

    private void traverseToRelevel(NodeLayout node) {
        addNodeByLevel(node);
        for (NodeLayout child : node.getPredecessingNodes()) {
            traverseToRelevel(child);
        }
    }

    public List<? extends NodeLayout> getNodes() {
        List<NodeLayout> renderNodes = new ArrayList<>(nodes.values());
        return renderNodes;
    }
    
    public void clearPool() {
        nodes.clear();
    }

    public void locateAll(int width, int height) {

        lastWidth = width;
        lastHeight = height;

        int maxLevel = 0;
        for (NodeLayout node : nodes.values()) {
            if (node.level > maxLevel) maxLevel = node.level;
        }
        int[] totalNodeWidth = new int[maxLevel + 1];

        for (NodeLayout node : nodes.values()) {
            totalNodeWidth[node.level] += 1;
        }

        int maxLevelWidth = 0;
        for (int levelWidth : totalNodeWidth) {
            if (levelWidth > maxLevelWidth) maxLevelWidth = levelWidth;
        }

        double[] minMaxX = {width, 0};
        for (int i = 0; i <= maxLevel; i++) {
            locate(nodesByLevel.get(i), width, height, i, maxLevelWidth, maxLevel, minMaxX);
        }

        // this is a hack to partially correct my bad layout algorithm :(
        double xAdjust = (minMaxX[0] + (width-minMaxX[1])) / 2 - minMaxX[0];

        for (int i = 0; i <= maxLevel; i++) {
            for (int j = 0; j < nodesByLevel.get(i).size(); j++) {
                shiftX(nodesByLevel.get(i).get(j), xAdjust);
            }
        }
    }

    private void shiftX(NodeLayout renderNode, double xAdjust) {
        renderNode.setLocation(renderNode.point.getX() + xAdjust, renderNode.point.getY());
    }

    private void locate(List<NodeLayout> nodes, int width, int height, int level, int maxNodesAtAnyLevel, int maxLevel, double[] minMaxX) {

        double preferredSpacing = width / (maxNodesAtAnyLevel + 1.0);

        double yDelta = height / (maxLevel + 1.0);
        double y = (maxLevel - level + 1) * yDelta;

        double lastX = 0;
        for (int i = 0; i < nodes.size(); i++) {
            NodeLayout node = nodes.get(i);

            double x = 0;
            if (node.value() instanceof Parameterized) {
                x = ((NodeLayout) node.getSuccessingNodes().get(0)).point.getX();
            } else {
                double preferredX = 0;
                if (level <= 1) {
                    preferredX = (i + 1.0) / (nodes.size() + 1.0) * width;
                } else {
                    preferredX = node.getPreferredX(preferredSpacing);
                }

                if (preferredX >= (lastX + preferredSpacing) && (width - preferredX) >= ((nodes.size() - i - 1) * preferredSpacing))  {
                    x = preferredX;
                } else {
                    x = lastX + preferredSpacing;
                }

                lastX = x;
            }

            if (x < minMaxX[0]) minMaxX[0] = x;
            if (x > minMaxX[1]) minMaxX[1] = x;
            node.setLocation(x,y);
        }
    }

    private List<NodeLayout> getInputsOfLeftmostOutput(NodeLayout node) {

        //TODO find out how node can be null here!
        List<NodeLayout> outputs = node.getSuccessingNodes();

        if (node.getSuccessingNodes().size() > 1) {
            // get the left-most output according to nodesByLevel
            NodeLayout leftMost = outputs.get(0);
            int leftMostIndex = levelIndex(leftMost);
            for (int i = 1; i < outputs.size(); i++) {
                int index = levelIndex(outputs.get(i));
                if (index < leftMostIndex) {
                    leftMostIndex = index;
                    leftMost = outputs.get(i);
                }
            }
            return leftMost.getPredecessingNodes();
        } else if (outputs.size() == 1) {
            return outputs.get(0).getPredecessingNodes();
        } else {
            return null;
        }
    }

    public void shiftLeft(NodeLayout node) {
//        int pos = nodesByLevel.get(node.level).indexOf(node);
//        if (pos > 0) {
//            RenderNode temp = nodesByLevel.get(node.level).get(pos-1);
//            nodesByLevel.get(node.level).set(pos-1, node);
//            nodesByLevel.get(node.level).set(pos, temp);
//            locateAll(lastWidth, lastHeight);
//        }

        if (node != null) {
            List<NodeLayout> inputs = getInputsOfLeftmostOutput(node);

            int pos = inputs.indexOf(node);
            if (pos > 0) {
                NodeLayout temp = inputs.get(pos - 1);
                inputs.set(pos - 1, node);
                inputs.set(pos, temp);
                relevel();
                locateAll(lastWidth, lastHeight);
            }
        }
    }

    private int levelIndex(NodeLayout node) {
        return nodesByLevel.get(node.level).indexOf(node);
    }

    public void shiftRight(NodeLayout node) {
//        int pos = nodesByLevel.get(node.level).indexOf(node);
//        if (pos < nodesByLevel.get(node.level).size() - 1) {
//            RenderNode temp = nodesByLevel.get(node.level).get(pos+1);
//            nodesByLevel.get(node.level).set(pos+1, node);
//            nodesByLevel.get(node.level).set(pos, temp);
//            locateAll(lastWidth, lastHeight);
//        }

        if (node != null) {
            List<NodeLayout> inputs = getInputsOfLeftmostOutput(node);
            int pos = inputs.indexOf(node);
            if (pos < inputs.size() - 1) {
                NodeLayout temp = inputs.get(pos+1);
                inputs.set(pos+1, node);
                inputs.set(pos, temp);
                relevel();
                locateAll(lastWidth, lastHeight);
            }
        }
    }

    public NodeLayout createNode(Value value, NodeLayout parentNode) {

        NodeLayout node = nodes.get(value);
        boolean newNode = (node == null);

        if (newNode && (value.isRandom() || showAllNodes)) {
            node = new NodeLayout(value, parser);
        }

        if (node != null) {
            if (parentNode != null && !node.getSuccessingNodes().contains(parentNode)) {
                node.addOutput(parentNode);
            }
            node.setLevel();

            if (newNode) {
                addNode(node);
                Parameterized child;
                if (value instanceof RandomVariable) {
                    child = ((RandomVariable) value).getGenerativeDistribution();
                } else {
                    child = value.getFunction();
                }

                if (child != null) {
                    NodeLayout childNode = createNode(child, node);
                    if (childNode != null) node.getPredecessingNodes().add(childNode);
                }
            }
        }

        return node;
    }

    public NodeLayout createNode(Parameterized g, NodeLayout parentNode) {
        NodeLayout node = nodes.get(g);
        if (node == null) {
            node = new NodeLayout(g, parser);
            node.addOutput(parentNode);
            node.setLevel();
            addNode(node);

            Map<String, Value> params = g.getParams();
            for (String key : params.keySet()) {

                NodeLayout child = createNode(params.get(key), node);
                if (child != null) node.getPredecessingNodes().add(child);
            }
        }
        return node;
    }
}
