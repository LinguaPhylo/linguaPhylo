package james.graphicalModel;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public static DecimalFormat FORMAT = new DecimalFormat();

    static {
        FORMAT.setMaximumFractionDigits(6);
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
            // RRB: weirdly, the compiler complains that node.getInputs() returns objects, but 
            // the method signature says it returns a List<GraphicalModelNode>, so no cast
            // should be necessary
            for (GraphicalModelNode child : (List<GraphicalModelNode>) node.getInputs()) {
                builder.append(toGraphvizDot(child, done));
            }

            String name = graphvizName(node);
            builder.append(graphvizNodeString(node, name) );
            builder.append(";\n");

            for (GraphicalModelNode child : (List<GraphicalModelNode>) node.getInputs()) {
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
        String label = "";
        String edgestyle = " arrowhead=vee, ";

        if (child instanceof Parameterized) {
            label = "taillabel=\"" + ((Parameterized)child).getName() + "\", " ;
        } else if (child instanceof Value && ((Value) child).isAnonymous()) {
            label = "label=\"" + ((Value)child).getLabel() + "\", ";
        }

        if (node instanceof Parameterized) {
            edgestyle = "arrowhead=none, ";
        }

        return "[" + label + edgestyle + "tailport=s]";
    }

    private static String graphvizName(GraphicalModelNode node) {
        String name = null;

        if (node instanceof Value) {
            if (((Value) node).isAnonymous()) {

                name = ""+node.hashCode();
            } else {
                name = ((Value) node).getId();
            }
        } else if (node instanceof Parameterized) {
            name = ""+node.hashCode();
        }
        return name;
    }

    private static String graphvizLabel(GraphicalModelNode node) {
        String label = null;

        if (node instanceof Value) {
            if (((Value) node).isAnonymous()) {
                //label = ((Parameterized)((Value) node).getOutputs().get(0)).getParamName((Value) node) + " = " + node.toString();
                String slot = ((Value)node).getLabel();
                Object val = ((Value) node).value();
                if (val instanceof Double || val instanceof Integer) {
                    label = node.toString();
                }
            } else if (((Value)node).function == null  && !(node instanceof RandomVariable)) {
                label = node.toString();
            } else {
                label = ((Value) node).getId();
            }
        } else if (node instanceof Parameterized) {
            label = "";
        }
        return label;
    }

    private static String graphvizNodeString(GraphicalModelNode node, String name) {
        String labelString = "label=\"" + graphvizLabel(node) + "\", ";

        if (node instanceof Parameterized) {
            return name + "[" + labelString + "shape=box, fixedsize=true, width=0.2, height=0.2, label=\"\", fillcolor=gray, style=filled]";
            //, label=\"" + ((Parameterized)node).getName() + "\"]";
        } else if (node instanceof RandomVariable) {
            return name + "[" + labelString +"shape=circle, fixedsize=true, width=0.8, height=0.8, fillcolor=green, style=filled]";
        } else if (node instanceof Value) {
            if (((Value)node).function != null) {
                return name + "[" + labelString +"shape=diamond, fixedsize=true, width=0.8, height=0.8, fillcolor=red, style=filled]";
            } else return name + "[" + labelString +"shape=rect]";
        }
        return name;
    }

    public static boolean isInteger(String str) {
        try {
            int i = Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
