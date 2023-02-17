package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.StringValue;

import java.util.Objects;

/**
 * @author Walter Xie
 */
public class ConcatStr extends DeterministicFunction<String> {

    StringValue[] x;

    // TODO: SEVERE: Found no function for concatStr matching arguments [Llphy.graphicalModel.Value;@19e7a160
    // SimulatorListenerImpl line 848
    public ConcatStr(StringValue... x) {
        int length = x.length;
        this.x = x;

        for (int i = 0; i < length; i++) {
            setInput(i + "", x[i]);
        }
    }

    @Override
    @GeneratorInfo(name = "concatStr", description = "A function to concatenate substrings into one sting.")
    public Value<String> apply() {
        StringBuilder oneStr = new StringBuilder();
        // concatenate Value<String>[] into Value<String>
        for (StringValue xVal : x) {
//            if (xVal instanceof StringValue sv) {
                oneStr.append(Objects.requireNonNull(xVal).value());
//            }
//            else if (xVal instanceof StringArrayValue sav) {
//                for (String str : Objects.requireNonNull(sav).value()) {
//                    oneStr.append(str);
//                }
//            }
        }

        return new StringValue(null, oneStr.toString(), this);
    }

}
