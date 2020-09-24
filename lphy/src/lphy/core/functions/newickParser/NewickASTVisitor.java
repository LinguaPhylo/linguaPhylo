package lphy.core.functions.newickParser;

import lphy.evolution.tree.TimeTreeNode;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Visits each component of the AST built from the Newick string, constructing
 * a BEAST tree along the way.
 */
public class NewickASTVisitor extends NewickParserBaseVisitor<TimeTreeNode> {

    private int numberedNodeCount = 0;

    private double DEFAULT_LENGTH = 0.001;

    List<String> labels = new ArrayList<>();

    @Override
    public TimeTreeNode visitTree(NewickParser.TreeContext ctx) {
        TimeTreeNode root = visit(ctx.node());

        // Ensure tree is properly sorted in terms of node numbers.
        root.sort();

        // Replace lengths read from Newick with heights.
        convertLengthToHeight(root);

        // Make sure internal nodes are numbered correctly
        numberUnnumberedNodes(root);

        // Check for duplicate taxa
        BitSet nodeNrSeen = new BitSet();
        for (TimeTreeNode leaf : root.getAllLeafNodes()) {
            if (leaf.getIndex() < 0)
                continue;  // Skip unnumbered leaves

            if (nodeNrSeen.get(leaf.getIndex()))
                throw new TreeParsingException("Duplicate taxon found: " + labels.get(leaf.getIndex()));
            else
                nodeNrSeen.set(leaf.getIndex());
        }

        return root;
    }

    private void processMetadata(TimeTreeNode node, NewickParser.MetaContext metaContext, boolean isLengthMeta) {
        String metaDataString = "";
        for (int i = 0; i < metaContext.attrib().size(); i++) {
            if (i > 0)
                metaDataString += ",";
            metaDataString += metaContext.attrib().get(i).getText();
        }

        String key;
        Object value;
        for (NewickParser.AttribContext attribctx : metaContext.attrib()) {
            key = attribctx.attribKey.getText();

            if (attribctx.attribValue().attribNumber() != null) {
                value = Double.parseDouble(attribctx.attribValue().attribNumber().getText());
            } else if (attribctx.attribValue().ASTRING() != null) {
                String stringValue = attribctx.attribValue().ASTRING().getText();
                if (stringValue.startsWith("\"") || stringValue.startsWith("\'")) {
                    stringValue = stringValue.substring(1, stringValue.length() - 1);
                }
                value = stringValue;
            } else if (attribctx.attribValue().vector() != null) {
                try {

                    List<NewickParser.AttribValueContext> elementContexts = attribctx.attribValue().vector().attribValue();

                    Double[] arrayValues = new Double[elementContexts.size()];
                    for (int i = 0; i < elementContexts.size(); i++)
                        arrayValues[i] = Double.parseDouble(elementContexts.get(i).getText());

                    value = arrayValues;
                } catch (NumberFormatException ex) {
                    //throw new TreeParsingException("Encountered vector-valued metadata entry with " +
                    //              "one or more non-numeric elements.");

                    // it is a non-numerical vector -- store as String
                    List<NewickParser.AttribValueContext> elementContexts = attribctx.attribValue().vector().attribValue();

                    String[] arrayValues = new String[elementContexts.size()];
                    for (int i = 0; i < elementContexts.size(); i++)
                        arrayValues[i] = elementContexts.get(i).getText();

                    value = arrayValues;
                }

            } else
                throw new TreeParsingException("Encountered unknown metadata value.");

            if (isLengthMeta)
                node.setMetaData(key, value);
            else
                node.setMetaData(key, value);
        }
    }

    @Override
    public TimeTreeNode visitNode(NewickParser.NodeContext ctx) {
        TimeTreeNode node = new TimeTreeNode((String)null, null);

        for (NewickParser.NodeContext ctxChild : ctx.node()) {
            node.addChild(visit(ctxChild));
        }

        NewickParser.PostContext postCtx = ctx.post();

        // Process metadata

        if (postCtx.nodeMeta != null)
            processMetadata(node, postCtx.nodeMeta, false);

        if (postCtx.lengthMeta != null)
            processMetadata(node, postCtx.lengthMeta, true);

        // Process edge length

        if (postCtx.length != null)
            node.setAge(Double.parseDouble(postCtx.length.getText()));
        else
            node.setAge(DEFAULT_LENGTH);

        // Process label

        node.setIndex(-1);
        if (postCtx.label() != null) {
            node.setId(postCtx.label().getText());


            if (node.isLeaf()) {
                node.setIndex(getLabelIndex(postCtx.label().getText()));
                numberedNodeCount += 1;
            }
        }

        return node;
    }

    /**
     * Try to map str into an index.
     */
    private int getLabelIndex(final String str) {

        // look it up in list of taxa
        for (int index = 0; index < labels.size(); index++) {
            if (str.equals(labels.get(index))) {
                return index;
            }
        }

        labels.add(str);
        return labels.size() - 1;
    }

    /**
     * The node height field is initially populated with the length of the edge above due
     * to the way the tree is stored in Newick format.  This method converts these lengths
     * to actual ages before the most recent sample.
     *
     * @param root root of tree
     */
    private void convertLengthToHeight(final TimeTreeNode root) {
        final double totalHeight = convertLengthToHeight(root, 0);
        offset(root, -totalHeight);
    }

    /**
     * Recursive method used to convert lengths to heights.  Applied to the root,
     * results in heights from 0 to -total_height_of_tree.
     *
     * @param node   node of a clade to convert
     * @param height Parent height.
     * @return total height of clade
     */
    private double convertLengthToHeight(final TimeTreeNode node, final double height) {
        final double length = node.getAge();
        node.setAge((height - length));
        if (node.isLeaf()) {
            return node.getAge();
        } else {
            double minChildHeight = Double.POSITIVE_INFINITY;
            for (TimeTreeNode child : node.getChildren())
                minChildHeight = Math.min(minChildHeight, convertLengthToHeight(child, height - length));

            return minChildHeight;
        }
    }

    /**
     * Method used by convertLengthToHeight(node) to remove negative offset from
     * node heights that is produced by convertLengthToHeight(node, height).
     *
     * @param node  node of clade to offset
     * @param delta offset
     */
    private void offset(final TimeTreeNode node, final double delta) {
        node.setAge(node.getAge() + delta);
        if (node.isLeaf()) {
            if (node.getAge() < 0) {
                node.setAge(0);
            }
        }
        if (!node.isLeaf()) {
            for (TimeTreeNode child : node.getChildren()) {
                offset(child, delta);
            }
        }
    }

    /**
     * Number any nodes in a clade which were not explicitly numbered by
     * the parsed string.
     *
     * @param node clade parent
     */
    private void numberUnnumberedNodes(TimeTreeNode node) {
        if (node.isLeaf())
            return;

        for (TimeTreeNode child : node.getChildren()) {
            numberUnnumberedNodes(child);
        }

        if (node.getIndex() < 0)
            node.setIndex(numberedNodeCount);

        numberedNodeCount += 1;
    }
}
