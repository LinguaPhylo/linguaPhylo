package james.graphicalModel;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public static DecimalFormat FORMAT = new DecimalFormat();

    static {
        FORMAT.setMaximumFractionDigits(8);
    }
    
    public static String toGraphvizDot(Collection<GraphicalModelNode> nodes) {

        Set<GraphicalModelNode> done = new HashSet<>();

        StringBuilder builder = new StringBuilder();
        builder.append("digraph G {\n");
        for (GraphicalModelNode node : nodes) {
            String str = toGraphvizDot(node, done);
            if (str != null) {
                builder.append(str);
                builder.append("\n");
                done.add(node);
            }
        }
        builder.append("}\n");
        return builder.toString();

    }

    private static String toGraphvizDot(GraphicalModelNode node, Set<GraphicalModelNode> done) {
        if (done.contains(node)) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder();
            for (GraphicalModelNode child : node.getInputs()) {
                builder.append(toGraphvizDot(child, done));
            }

            String name = graphvizName(node);
            builder.append(graphvizNodeString(node,name) );
            builder.append(";\n");

            for (GraphicalModelNode child : node.getInputs()) {
                builder.append(graphvizName(child));
                builder.append(" -> ");

                builder.append(name);
                builder.append(graphvizEdgeLabel(node, child));
                builder.append(";\n");
            }
            return builder.toString();
        }
    }

    private static String graphvizEdgeLabel(GraphicalModelNode node, GraphicalModelNode child) {
        String taillabel = "";
        String edgestyle = " arrowhead=vee, ";

        if (child instanceof Parameterized) {
            taillabel = "taillabel=\"" + ((Parameterized)child).getName() + "\", " ;
        }

        if (node instanceof Parameterized) {
            edgestyle = "arrowhead=none, ";
        }

        return "[" + taillabel + edgestyle + "tailport=s]";
    }

    private static String graphvizName(GraphicalModelNode node) {
        String name = null;

        if (node instanceof Value) {
            if (((Value)node).function == null  && !(node instanceof RandomVariable)) {
                name = "\"" + node.toString() + "\"";
            } else {
                name = "\"" + ((Value) node).getId() + "\"";
            }
        } else if (node instanceof Parameterized) {
            name = ((Parameterized)node).getName() + node.hashCode();
        }
        return name;
    }

    private static String graphvizNodeString(GraphicalModelNode node, String name) {
        if (node instanceof Parameterized) {
            return name + "[shape=box, fixedsize=true, width=0.2, height=0.2, label=\"\", fillcolor=gray, style=filled]";
            //, label=\"" + ((Parameterized)node).getName() + "\"]";
        } else if (node instanceof RandomVariable) {
            return name + "[shape=circle, fixedsize=true, width=0.8, height=0.8, fillcolor=green, style=filled]";
        } else if (node instanceof Value) {
            if (((Value)node).function != null) {
                return name + "[shape=diamond, fixedsize=true, width=0.8, height=0.8, fillcolor=red, style=filled]";
            } else return name + "[shape=rect]";
        }
        return name;
    }
}
