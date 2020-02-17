package james.graphicalModel;

import java.util.List;

public interface GraphicalModelNode {

    /**
     * inputs are the arguments of a function or distribution or the function/distribution that produced this model node value/variable.
     * @return
     */
    List<GraphicalModelNode> getInputs();

    /**
     * Outputs are the function or distribution of the arguments or the value/variable produced by the function/distribution.
     * @return
     */
    //List<GraphicalModelNode> getOutputs();


}
