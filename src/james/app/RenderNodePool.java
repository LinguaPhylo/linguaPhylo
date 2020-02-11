package james.app;

import james.graphicalModel.Parameterized;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import java.awt.geom.Point2D;
import java.util.*;

public class RenderNodePool {

    private Map<Object, RenderNode> pool = new HashMap<>();
    private List<List<RenderNode>> nodesByLevel = new ArrayList<>();

    public RenderNodePool() {
    }

    public void addRoot(Value v) {
        createRenderNode(v, null);
    }

    private void addNode(RenderNode node) {
        pool.put(node.value(), node);
        addNodeByLevel(node);
    }

    private void addNodeByLevel(RenderNode node) {
        while (nodesByLevel.size() <= node.level) {
            nodesByLevel.add(new ArrayList<>());
        }
        nodesByLevel.get(node.level).add(node);
    }

    public List<RenderNode> getRenderNodes() {
        List<RenderNode> renderNodes = new ArrayList<>(pool.values());
        return renderNodes;
    }
    
    public void clearPool() {
        pool.clear();
    }

    public void locateAll(int width, int height) {

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
        double y = (maxLevel - level + 1) / (maxLevel + 2.0) * height;


        double lastX = 0;
        for (int i = 0; i < nodes.size(); i++) {
            RenderNode node = nodes.get(i);

            double x = 0;
            if (node.value() instanceof Parameterized) {
                x = ((RenderNode) node.outputs.get(0)).point.getX();
            } else {
                double preferredX = 0;
                if (level == 0) {
                    preferredX = (i + 1.0) / (nodes.size() + 1.0) * width;
                } else {
                    preferredX = node.getPreferredX(preferredSpacing);
                }

                if (preferredX >= (lastX + preferredSpacing) && (width - preferredX) >= ((nodes.size() - i - 1) * preferredSpacing)) {
                    x = preferredX;
                } else {
                    x = lastX + preferredSpacing;
                }

                lastX = x;
            }

            node.locate(new Point2D.Double(x, y));
        }
    }

    public RenderNode<Value> createRenderNode(Value value, RenderNode<Parameterized> parentNode) {

        RenderNode<Value> node = pool.get(value);
        boolean newNode = node == null;

        if (newNode) {
            node = new RenderNode<>(value);
        }

        if (parentNode != null && !node.outputs.contains(parentNode)) {
            node.outputs.add(parentNode);
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
            node = new RenderNode<>(g);
            node.outputs.add(parentNode);
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
