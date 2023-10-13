package lphystudio.app;

import lphy.core.model.*;
import lphy.core.parser.LPhyMetaData;
import lphystudio.core.theme.ThemeColours;

import java.awt.*;
import java.util.List;
import java.util.*;

import static lphystudio.core.theme.ThemeColours.*;

/**
 * @author Alexei Drummond
 */
public class GraphvizDotUtils {

    static boolean clusters = true;

    public static String toGraphvizDot(Collection<GraphicalModelNode> nodes, LPhyMetaData parser) {

        Set<GraphicalModelNode> done = new HashSet<>();
        List<String> dataNodes = new ArrayList<>();
        List<String> modelNodes = new ArrayList<>();
        List<String> edges = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        builder.append("digraph G {\n");
        builder.append("  ranksep=0.02;\n");
        for (GraphicalModelNode node : nodes) {
            toGraphvizDot(node, done, dataNodes, modelNodes, edges, parser, isDataNode(node, parser));
        }

        appendCluster("data", builder, dataNodes);
        appendCluster("model", builder, modelNodes);

        for (String edge : edges) {
            builder.append(edge);
        }

        builder.append("}\n");
        return builder.toString();

    }

    private static void appendCluster(String name, StringBuilder builder, List<String> nodes) {
        if (nodes.size() > 0) {
            if (clusters) builder.append("subgraph cluster_").append(name).append(" {\n");
            for (String node : nodes) {
                builder.append("  ").append(node);
            }
            if (clusters) {
                builder.append("  label = \"");
                builder.append(name);
                builder.append("\";\n");
                //builder.append("  style=filled;\n");
                builder.append("  color=blue;\n");
                builder.append("  labeljust=\"l\";\n");
                builder.append("  fontcolor=blue;\n");
                builder.append("}\n");
            }
        }
        builder.append("\n");
    }

    private static void toGraphvizDot(GraphicalModelNode node, Set<GraphicalModelNode> done, List<String> dataNodes, List<String> modelNodes, List<String> edges, LPhyMetaData parser, boolean isData) {
        if (done.contains(node)) {
            // DO NOTHING
        } else {
            isData = isData || isDataNode(node, parser);


            for (GraphicalModelNode child : (List<GraphicalModelNode>) node.getInputs()) {
                toGraphvizDot(child, done, dataNodes, modelNodes, edges, parser, isData);
                done.add(child);
            }

            String name = getUniqueId(node, parser);
            boolean isClamped = parser.isClamped(node.getUniqueId());
            String nodeString = graphvizNodeString(node, name, isData, isClamped) + ";\n";
            if (isData) {
                dataNodes.add(nodeString);
            } else {
                modelNodes.add(nodeString);
            }

            for (GraphicalModelNode child : (List<GraphicalModelNode>) node.getInputs()) {
                StringBuilder builder = new StringBuilder();
                builder.append(getUniqueId(child, parser));
                builder.append(" -> ");

                builder.append(name);
                builder.append(graphvizEdgeLabel(node, child));
                builder.append(";\n");
                edges.add(builder.toString());
            }
        }
    }

    private static String graphvizEdgeLabel(GraphicalModelNode node, GraphicalModelNode child) {
        String label = "";
        String edgestyle = " arrowhead=vee, ";

        if (child instanceof Generator) {
            label = "taillabel=\"" + ((Generator)child).getName() + "\", " ;
        } else if (child instanceof Value && ((Value) child).isAnonymous()) {
            label = "label=\"" + ((Value)child).getLabel() + "\", ";
        }

        if (node instanceof Generator) {
            edgestyle = "arrowhead=none, ";
        }

        return "[" + label + edgestyle + "tailport=s]";
    }

    private static String graphvizLabel(GraphicalModelNode node) {
        String label = null;

        if (node instanceof Value) {
            if (((Value) node).isAnonymous()) {
                //label = ((Generator)((Value) node).getOutputs().get(0)).getParamName((Value) node) + " = " + node.toString();
                String slot = ((Value)node).getLabel();
                Object val = ((Value) node).value();
                if (val instanceof Double || val instanceof Integer) {
                    label = node.toString();
                } else if (val instanceof Double[] && ((Double[])val).length < 7) {
                    label = Arrays.toString((Double[])val);
                } else if (val instanceof String) {
                    label = "'" + (String)val + "'";
                } else {
                    label = slot;
                }
            } else if (((Value)node).getGenerator() == null  && !(node instanceof RandomVariable)) {
                label = node.toString();
            } else {
                label = ((Value) node).getId();
            }
        } else if (node instanceof Generator) {
            label = "";
        }

        return label;
    }

    private static String graphvizNodeString(GraphicalModelNode node, String name, boolean isData, boolean isClamped) {
        String labelString = "label=\"" + graphvizLabel(node) + "\", ";

        if (node instanceof GenerativeDistribution) { // use gray
            Color color = ThemeColours.getArgumentNameColor();
            String hex = getHexString(color);
            return name + "[" + labelString + "shape=box, fixedsize=true, width=0.2, height=0.2, label=\"\", " +
                    "fillcolor=\"" + hex + "\", style=filled]";
            //, label=\"" + ((Generator)node).getName() + "\"]";
        } if (node instanceof DeterministicFunction) { // use gray
            Color color = ThemeColours.getArgumentNameColor();
            String hex = getHexString(color);
            return name + "[" + labelString + "shape=diamond, fixedsize=true, width=0.2, height=0.2, label=\"\", " +
                    "fillcolor=\"" + hex + "\", style=filled]";
            //, label=\"" + ((Generator)node).getName() + "\"]";
        }  else if (node instanceof RandomVariable) {
            Color color;
            if (isClamped)
                color = getClampedVarColor();
            else
                color = getRandomVarColor();
            String hex = getHexString(color);
            return name + "[" + labelString +"shape=circle, fixedsize=true, width=0.8, height=0.8, " +
                    "fillcolor=\"" + hex + "\"\t, style=filled]";
        } else if (node instanceof Value) {
            if (((Value)node).getGenerator() != null) {
                Color color;
                if (isData)
                    color = getDataButtonColor();
                else
                    color = getFunctionColor();
                String hex = getHexString(color);
                return name + "[" + labelString +"shape=diamond, fixedsize=true, width=0.8, height=0.8, " +
                        "fillcolor=\"" + hex + "\", style=filled]";
            } else return name + "[" + labelString +"shape=rect]";
        }
        return name;
    }

    private static boolean isDataNode(GraphicalModelNode node, LPhyMetaData parser) {
        if (node instanceof Value && !(node instanceof RandomVariable)) {
            Value value = (Value)node;
            if (!value.isAnonymous()) {
                return (parser.hasValue(value.getId(), LPhyMetaData.Context.data));
            }
        }
        return false;
    }

    private static String getUniqueId(GraphicalModelNode node, LPhyMetaData parser) {
        String name = node.getUniqueId();
        if (node instanceof Value && !((Value)node).isAnonymous() && parser.isClamped(((Value) node).getId())) {
            name = node.hashCode()+"";
        }
        return name;
    }

}
