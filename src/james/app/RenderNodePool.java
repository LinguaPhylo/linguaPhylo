package james.app;

import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.Parameterized;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import java.awt.geom.Point2D;
import java.util.*;

public class RenderNodePool {

    private Map<Object, RenderNode> pool = new HashMap<>();
    private List<List<RenderNode>> nodesByLevel = new ArrayList<>();

    int lastWidth = 0;
    int lastHeight = 0;

    RenderNode rootNode = null;

    GraphicalModelParser parser;

    public RenderNodePool(GraphicalModelParser parser) {
        this.parser = parser;
        rootNode = new RenderNode("A dummy root to group actual root nodes! This node will node be rendered!", null);
        rootNode.level = 0;
        addNode(rootNode);
    }

    public void addRoot(Value v) {
        rootNode.inputs.add(createRenderNode(v, rootNode));
    }

    private void addNode(RenderNode node) {
        pool.put(node.value(), node);
        addNodeByLevel(node);
    }

    private void addNodeByLevel(RenderNode node) {
        while (nodesByLevel.size() <= node.level) {
            nodesByLevel.add(new ArrayList<>());
        }
        List<RenderNode> level =  nodesByLevel.get(node.level);
        if (!level.contains(node)) {
            level.add(node);
        }
    }

    private void relevel() {
        for (List<RenderNode> level : nodesByLevel) {
            level.clear();
        }
        nodesByLevel.clear();

        traverseToRelevel(rootNode);
    }

    private void traverseToRelevel(RenderNode node) {
        addNodeByLevel(node);
        for (RenderNode child : ((List<RenderNode>)node.inputs)) {
            traverseToRelevel(child);
        }
    }

    public List<RenderNode> getRenderNodes() {
        List<RenderNode> renderNodes = new ArrayList<>(pool.values());
        return renderNodes;
    }
    
    public void clearPool() {
        pool.clear();
    }

    public void locateAll(int width, int height) {

        lastWidth = width;
        lastHeight = height;

        Set<RenderNode> located = new HashSet<>();

        int maxLevel = 0;
        for (RenderNode node : pool.values()) {
            if (node.level > maxLevel) maxLevel = node.level;
        }
        int[] totalNodeWidth = new int[maxLevel + 1];

        for (RenderNode node : pool.values()) {
            totalNodeWidth[node.level] += 1;
        }

        int maxLevelWidth = 0;
        for (int levelWidth : totalNodeWidth) {
            if (levelWidth > maxLevelWidth) maxLevelWidth = levelWidth;
        }

        for (int i = 0; i <= maxLevel; i++) {
            locate(nodesByLevel.get(i), width, height, i, maxLevelWidth, maxLevel);
        }
    }

    private void locate(List<RenderNode> nodes, int width, int height, int level, int maxNodesAtAnyLevel, int maxLevel) {

        double preferredSpacing = width / (maxNodesAtAnyLevel + 1.0);
        double y = (maxLevel - level + 1) / (maxLevel + 1.0) * height;

        double lastX = 0;
        for (int i = 0; i < nodes.size(); i++) {
            RenderNode node = nodes.get(i);

            double x = 0;
            if (node.value() instanceof Parameterized) {
                x = ((RenderNode) node.outputs.get(0)).point.getX();
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

            node.locate(new Point2D.Double(x, y));
        }
    }

    private List<RenderNode> getInputsOfLeftmostOutput(RenderNode node) {

        //TODO find out how node can be null here!
        List<RenderNode> outputs = node.outputs;

        if (node.outputs.size() > 1) {
            // get the left-most output according to nodesByLevel
            RenderNode leftMost = outputs.get(0);
            int leftMostIndex = levelIndex(leftMost);
            for (int i = 1; i < outputs.size(); i++) {
                int index = levelIndex(outputs.get(i));
                if (index < leftMostIndex) {
                    leftMostIndex = index;
                    leftMost = outputs.get(i);
                }
            }
            return leftMost.inputs;
        } else if (outputs.size() == 1) {
            return outputs.get(0).inputs;
        } else {
            return null;
        }
    }

    public void shiftLeft(RenderNode node) {
//        int pos = nodesByLevel.get(node.level).indexOf(node);
//        if (pos > 0) {
//            RenderNode temp = nodesByLevel.get(node.level).get(pos-1);
//            nodesByLevel.get(node.level).set(pos-1, node);
//            nodesByLevel.get(node.level).set(pos, temp);
//            locateAll(lastWidth, lastHeight);
//        }

        if (node != null) {
            List<RenderNode> inputs = getInputsOfLeftmostOutput(node);

            int pos = inputs.indexOf(node);
            if (pos > 0) {
                RenderNode temp = inputs.get(pos - 1);
                inputs.set(pos - 1, node);
                inputs.set(pos, temp);
                relevel();
                locateAll(lastWidth, lastHeight);
            }
        }
    }

    private int levelIndex(RenderNode node) {
        return nodesByLevel.get(node.level).indexOf(node);
    }

    public void shiftRight(RenderNode node) {
//        int pos = nodesByLevel.get(node.level).indexOf(node);
//        if (pos < nodesByLevel.get(node.level).size() - 1) {
//            RenderNode temp = nodesByLevel.get(node.level).get(pos+1);
//            nodesByLevel.get(node.level).set(pos+1, node);
//            nodesByLevel.get(node.level).set(pos, temp);
//            locateAll(lastWidth, lastHeight);
//        }

        if (node != null) {
            List<RenderNode> inputs = getInputsOfLeftmostOutput(node);
            int pos = inputs.indexOf(node);
            if (pos < inputs.size() - 1) {
                RenderNode temp = inputs.get(pos+1);
                inputs.set(pos+1, node);
                inputs.set(pos, temp);
                relevel();
                locateAll(lastWidth, lastHeight);
            }
        }
    }

    public RenderNode<Value> createRenderNode(Value value, RenderNode<Parameterized> parentNode) {

        RenderNode<Value> node = pool.get(value);
        boolean newNode = node == null;

        if (newNode) {
            node = new RenderNode<>(value, parser);
        }

        if (parentNode != null && !node.outputs.contains(parentNode)) {
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
                RenderNode<Parameterized> childNode = createRenderNode(child, node);
                node.inputs.add(childNode);
            }
        }

        return node;
    }

    public RenderNode<Parameterized> createRenderNode(Parameterized g, RenderNode<Value> parentNode) {
        RenderNode<Parameterized> node = pool.get(g);
        if (node == null) {
            node = new RenderNode<>(g, parser);
            node.addOutput(parentNode);
            node.setLevel();
            addNode(node);

            Map<String, Value> params = g.getParams();
            for (String key : params.keySet()) {

                RenderNode<Value> child = createRenderNode(params.get(key), node);
                node.inputs.add(child);
            }
        }
        return node;
    }
}
