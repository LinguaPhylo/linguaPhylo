package lphybeast;

import lphy.core.functions.*;
import lphy.evolution.Taxa;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.CharSetAlignment;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;

/**
 * Utils class to exclude {@link lphy.graphicalModel.Value}
 * or {@link lphy.graphicalModel.Generator} to skip the validation
 * so not to throw UnsupportedOperationException
 * in either <code>BEASTContext#valueToBEAST(Value)<code/> or
 * <code>BEASTContext#generatorToBEAST(Value, Generator)<code/>.
 * @author Walter Xie
 */
public class Exclusion {

    public static boolean isExcludedValue(Value<?> val) {
        Object ob = val.value();
        return ob instanceof String || ob instanceof String[] || // ignore all String: d = nexus(file="Dengue4.nex");
                ob instanceof CharSetAlignment || // CharSetAlignment itself has no sequences
                // exclude the value returned by taxa (and ages) functions
                ( ob instanceof Taxa && !(ob instanceof Alignment) ) ;
    }

    public static boolean isExcludedGenerator(Generator generator) {
        return ((generator instanceof NTaxaFunction) || (generator instanceof NCharFunction) ||
                (generator instanceof TaxaFunction) ||
                (generator instanceof Nexus) || (generator instanceof TaxaAgesFromFunction) ||
                (generator instanceof ARange) ||
                (generator instanceof Range) || (generator instanceof Partition) );
    }
}
