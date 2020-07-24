package lphy.beast;

import beast.core.BEASTInterface;
import lphy.evolution.alignment.Alignment;
import lphy.graphicalModel.GraphicalModelNode;
import lphy.graphicalModel.Value;

import java.util.Map;

public interface ValueToBEAST<T> {

    /**
     * @param value the value to be converted
     * @param beastObjects all beast objects already converted by the value-inorder generator-postorder traversal.
     * @return
     */
    BEASTInterface valueToBEAST(Value<T> value, Map<GraphicalModelNode, BEASTInterface> beastObjects);

    /**
     * The class of value that can be converted to BEAST.
     * @return
     */
    Class getValueClass();
}
