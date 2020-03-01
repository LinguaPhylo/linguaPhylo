package james.app.graphicalmodelcomponent;

import james.graphicalModel.GraphicalModelParser;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class BrandesKopfHorizontalCoordinateAssignment {

    Map<LayeredNode, List<LayeredNode>> marks = new HashMap<>();

    LayeredGraph g;

    // minimum x-distance between two nodes.
    int delta = 2;

    public BrandesKopfHorizontalCoordinateAssignment(LayeredGraph g) {

        if (!proper(g)) {
            throw new IllegalArgumentException();
        }

        this.g = g;

        horizontalCoordinateAssignment();
    }

    boolean proper(LayeredGraph g) {
        for (LayeredNode v : g.getNodes()) {
            int layer = v.getLayer();
            for (LayeredNode u : v.getPredecessors()) {
                if (u.getLayer() != layer - 1) return false;
            }
            for (LayeredNode w : v.getSuccessors()) {
                if (w.getLayer() != layer + 1) return false;
            }

            if (v.isDummy() && v.getPredecessors().size() == 0) {
                throw new IllegalArgumentException("dummy vertex has no predecessors!");
            }
        }
        return true;
    }

    int countDummyNodes() {
        int count = 0;
        for (LayeredNode v : g.getNodes()) {
            if (v.isDummy()) {
                count += 1;
            }
        }
        return count;
    }

    int countInnerEdges() {
        int count = 0;
        for (LayeredNode v : g.getNodes()) {
            if (v.isDummy()) {
                for (LayeredNode u : v.getPredecessors()) {
                    if (u.isDummy()) {
                        count += 1;
                    }
                }
            }
        }
        return count;
    }

    void preprocessing() {

        int h = g.layers.size();

        for (int i = 0; i < h - 2; i++) {

            int k0 = 0;
            int l = 0;

            List<LayeredNode> layer0 = g.layers.get(i);
            List<LayeredNode> layer1 = g.layers.get(i + 1);
            int layer1Size = layer1.size();

            for (int l1 = 0; l1 < layer1Size; l1++) { // traverses layer left to right
                LayeredNode vl1iplus1 = layer1.get(l1);

                if (l1 == layer1Size - 1 || isInnerSegmentAbove(vl1iplus1)) {

                    int k1 = layer0.size() - 1;
                    if (isInnerSegmentAbove(vl1iplus1)) {
                        k1 = vl1iplus1.getPredecessors().get(0).getIndex(); // position of upper neighbour in it's own level
                    }
                    while (l <= l1) {
                        LayeredNode vliplus1 = layer1.get(l);
                        for (LayeredNode vki : vliplus1.getPredecessors()) {
                            int k = vki.getIndex();
                            if (k < k0 || k > k1) {
                                mark(vki, vliplus1);
                            }
                        }
                        l += 1;
                    }
                    k0 = k1;
                }
            }
        }
    }

    private void mark(LayeredNode upperNeighbour, LayeredNode node) {
        List<LayeredNode> edges = marks.get(node);
        if (edges == null) {
            edges = new ArrayList<>();
            marks.put(node, edges);
        }
        edges.add(upperNeighbour);
    }

    private boolean marked(LayeredNode upper, LayeredNode node) {
        List<LayeredNode> edges = marks.get(node);
        if (edges == null) return false;
        return edges.contains(upper);
    }

    void verticalAlignment(Alignment a) {

        for (LayeredNode v : g.getNodes()) {
            a.root.put(v, v);
            a.align.put(v, v);
        }

        for (List<LayeredNode> layer : g.layers) {
            int r = -1;
            for (int k = 0; k < layer.size(); k++) {
                LayeredNode v_ki = layer.get(k);
                if (hasOrderedPredecessors(v_ki)) {
                    int d = v_ki.getPredecessors().size();

                    for (int m = (int) Math.floor((d + 1.0) / 2.0) - 1; m <= (int) Math.ceil((d + 1.0) / 2.0) - 1; m++) {
                        LayeredNode um = upper(v_ki, m);

                        if (a.align(v_ki) == v_ki) {
                            if (!marked(um, v_ki) && r < um.getIndex()) {
                                a.align.put(um, v_ki);
                                a.root.put(v_ki, a.root(um));
                                a.align.put(v_ki, a.root(um));
                                r = um.getIndex();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean hasOrderedPredecessors(LayeredNode node) {
        List<LayeredNode> predecessors = node.getPredecessors();
        if (predecessors.size() == 0) return false;
        for (int i = 1; i < predecessors.size(); i++) {
            if (predecessors.get(i).getIndex() < predecessors.get(i - 1).getIndex()) return false;
        }
        return true;
    }

    LayeredNode pred(LayeredNode v) {
        return g.layers.get(v.getLayer()).get(v.getIndex() - 1);
    }

    void placeBlock(LayeredNode v, Alignment a) {

        if (a.x.get(v) == null) {
            a.x.put(v, 0);
            LayeredNode w = v;
            do {
                if (w.getIndex() > 0) {
                    LayeredNode u = a.root.get(pred(w));
                    placeBlock(u, a);
                    if (a.sink(v) == v) {
                        a.sink.put(v, a.sink(u));
                    }
                    if (a.sink(v) != a.sink(u)) {
                        a.shift.put(a.sink(u), Math.min(a.shift.get(a.sink(u)), a.x(v) - a.x(u) - delta));
                    } else {
                        a.x.put(v, Math.max(a.x(v), a.x(u) + delta));
                    }
                }
                w = a.align.get(w);
            } while (w != v);
        }
    }

    void horizontalCompaction(Alignment a) {
        for (LayeredNode v : g.getNodes()) {
            a.sink.put(v,v);
            a.shift.put(v, Integer.MAX_VALUE);
        }
        a.x.clear();

        for (LayeredNode v : g.getNodes()) {
            if (a.root(v) == v) {
                placeBlock(v, a);
            }
        }

        for (LayeredNode v : g.getNodes()) {
            a.x.put(v, a.x(a.root(v)));
        }
    }

    void horizontalCoordinateAssignment() {

        preprocessing();

        // do left
        Alignment l = new Alignment();

        verticalAlignment(l);
        horizontalCompaction(l);

        int maxXLeft = l.computeFinalX();

        // do right
        Alignment r = new Alignment();
        reverseLayers();

        verticalAlignment(r);
        horizontalCompaction(r);

        int maxXRight = r.computeFinalX();

        for (LayeredNode v : g.getNodes()) {
            v.setX((l.finalX(v) + (maxXRight - r.finalX(v))) / 2.0);
        }

        // return layers to original order
        reverseLayers();
    }

    private void reverseLayers() {
        for (List<LayeredNode> layer : g.layers) {
            Collections.reverse(layer);
        }
        g.updateIndex();
    }

    private LayeredNode upper(LayeredNode node, int i) {
        return node.getPredecessors().get(i);
    }

    private boolean isInnerSegmentAbove(LayeredNode layeredNode) {
        return layeredNode.isDummy() && layeredNode.getPredecessors().get(0).isDummy();
    }

    class Alignment {
        Map<LayeredNode, LayeredNode> root = new HashMap<>();
        Map<LayeredNode, LayeredNode> align = new HashMap<>();
        Map<LayeredNode, LayeredNode> sink = new HashMap<>();
        Map<LayeredNode, Integer> x = new HashMap<>();
        Map<LayeredNode, Integer> finalX = new HashMap<>();
        Map<LayeredNode, Integer> shift = new HashMap<>();

        /**
         * @return maximum x value
         */
        int computeFinalX() {
            int maxX = 0;
            for (LayeredNode v : g.getNodes()) {

                int x = x(v);
                int s = shift(sink(root(v)));
                if (s < Integer.MAX_VALUE) {
                    x += s;
                }

                if (x > maxX) maxX = x;
                finalX.put(v, x);
            }
            return maxX;
        }

        int x(LayeredNode v) { return x.get(v); }

        int finalX(LayeredNode v) { return finalX.get(v); }

        int shift(LayeredNode v) { return shift.get(v); }
        LayeredNode align(LayeredNode v) { return align.get(v); }
        LayeredNode sink(LayeredNode v) { return sink.get(v); }
        LayeredNode root(LayeredNode v) { return root.get(v); }
    }

    public static void main(String[] args) throws IOException {

        GraphicalModelParser parser = new GraphicalModelParser();

        String fileName = System.getProperty("user.dir") + File.separator + "examples" + File.separator + "bdRelaxed.lphy";

        File file = new File(fileName);

        parser.source(file);

        LayeredGraph layeredGraph = LayeredGraphFactory.createLayeredGraph(parser,true);

        //LayeredGraph layeredGraph = LayeredGraph.testGraph();

        BrandesKopfHorizontalCoordinateAssignment bkhca = new BrandesKopfHorizontalCoordinateAssignment(layeredGraph);

        JFrame frame = new JFrame("LayeredGraph");
        frame.setSize(1200, 800);
        frame.getContentPane().add(new LayeredGraphComponent(layeredGraph));
        frame.setVisible(true);


//        System.out.println(layeredGraph.layers.size() + " layers");
//
//        ProperLayeredGraph proper = new ProperLayeredGraph(layeredGraph, new Layering.FromSources());
//

    }
}
