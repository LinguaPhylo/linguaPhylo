package lphy.base.evolution.alignment;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.logger.LoggerUtils;

import java.util.List;
import java.util.Objects;

/**
 * Guide to create an alignment containing the internal node sequences.
 * To implement this, the class must contain the taxa and internal nodes objects,
 * which are used to map to the sequences in the alignment.
 */
public interface AugmentedAlignment<T> extends TaxaCharacterMatrix<T> {

    /**
     * @param sequenceId the index of sequences in this alignment, tips are from 0 to (ntaxa - 1),
     *                   internal nodes are the rest, and root index is the last.
     * @param position   the site position.
     * @return  the integer state at the given coordinate of this alignment.
     */
    T getState(int sequenceId, int position);


    /**
     * @param  nodeId  a tree node ID, either taxon name or internal node ID.
     * @param  internalNodes  internal nodes of the tree
     * @return the index of this id mapped to the sequence in this AugmentedAlignment,
     *         or -1 if this id does not exist.
     *         This index  is assumed to range from 0 to ntaxa-1 for tip nodes,
     *         and continues from ntaxa up to the total count of internal nodes in the list.
     */
    default int indexOfSequence(String nodeId, Taxa taxa, List<TimeTreeNode> internalNodes) {
        int sequenceId = taxa.indexOfTaxon(nodeId);
        if (sequenceId >= 0)
            return sequenceId; // tips
        else {
            // the indices of internal nodes
            for (int i = 0; i < internalNodes.size(); i++) {
                TimeTreeNode treeNode = internalNodes.get(i);
                String id = treeNode.getId();
                // ids[i] could be null, but nodeId cannot be null.
                if (Objects.requireNonNull(nodeId).equals(id)) {
                    sequenceId = taxa.ntaxa() + i;
                    if (sequenceId != treeNode.getIndex())
                        LoggerUtils.log.warning("The index " + sequenceId + " of sequence of internal node (" +
                                nodeId + ") does not match its internal node index = " + treeNode.getIndex() + " !");
                    return sequenceId;
                }
            }
            return -1;
        }
    }

    /**
     * @param  nodeId     a tree node ID, either taxon name or internal node ID.
     * @param  treeNodes  any nodes of the tree
     * @return the index of this node id, or -1 if this id is not in this AugmentedAlignment object.
     */
    default int indexOfNode(String nodeId, List<TimeTreeNode> treeNodes) {
        for (TimeTreeNode treeNode : treeNodes) {
            String id = treeNode.getId();
            // ids[i] could be null, but nodeId cannot be null.
            if (Objects.requireNonNull(nodeId).equals(id))
                return treeNode.getIndex();
        }
        return -1;
    }

}
