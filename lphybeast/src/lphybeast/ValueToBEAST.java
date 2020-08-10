package lphybeast;

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

    /**
     * The BEAST class to be converted. It is only used for summarising at the moment.
     *
     * @return
     */
    default Class<? extends BEASTInterface> getBEASTClass() {
        return BEASTInterface.class;
    }
}
