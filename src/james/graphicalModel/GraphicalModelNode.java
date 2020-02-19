package james.graphicalModel;

import java.util.List;

public interface GraphicalModelNode {

    /**
     * inputs are the arguments of a function or distribution or the function/distribution that produced this model node value/variable.
     * @return
     */
    List<GraphicalModelNode> getInputs();
    
    /**
     * @return current value of the Constant, DeterministicFunction or GenerativeDistribution
     */
    Value currentValue();
}
