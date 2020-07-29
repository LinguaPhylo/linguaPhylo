package lphy.beast;

import beast.core.BEASTInterface;
import lphy.graphicalModel.Value;

public interface ValueToBEAST<T> {

    /**
     * @param value the value to be converted
     * @param context all beast objects already converted by the value-inorder generator-postorder traversal.
     * @return
     */
    BEASTInterface valueToBEAST(Value<T> value, BEASTContext context);

    /**
     * The class of value that can be converted to BEAST.
     * @return
     */
    Class getValueClass();
}
