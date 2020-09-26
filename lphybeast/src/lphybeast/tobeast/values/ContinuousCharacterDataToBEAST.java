package lphybeast.tobeast.values;

import beast.core.parameter.RealParameter;
import beast.evolution.alignment.Sequence;
import jebl.evolution.sequences.SequenceType;
import lphy.evolution.alignment.ContinuousCharacterData;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;
import lphybeast.tobeast.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContinuousCharacterDataToBEAST implements ValueToBEAST<ContinuousCharacterData, RealParameter> {

    @Override
    public RealParameter valueToBEAST(Value<ContinuousCharacterData> continuousCharacterDataValue, BEASTContext context) {

        ContinuousCharacterData continuousCharacterData = continuousCharacterDataValue.value();
        String[] taxaNames = continuousCharacterData.getTaxa().getTaxaNames();

        StringBuilder builder = new StringBuilder();
        builder.append(taxaNames[0]);
        for (int i = 1; i < taxaNames.length; i++) {
            builder.append(" ");
            builder.append(taxaNames[i]);
        }

        List<Double> allDataRowByRow = new ArrayList<>();
        for (int i = 0; i < taxaNames.length; i++) {
            allDataRowByRow.addAll(Arrays.asList(continuousCharacterData.getCharacterSequence(taxaNames[i])));
        }

        RealParameter beastParameter = new RealParameter();
        beastParameter.setInputValue("keys", builder.toString());
        beastParameter.setInputValue("values", allDataRowByRow);
        beastParameter.setInputValue("minordimension", continuousCharacterData.nchar());
        beastParameter.initAndValidate();

        // using LPhy var as ID allows multiple alignments
        if (!continuousCharacterDataValue.isAnonymous()) beastParameter.setID(continuousCharacterDataValue.getCanonicalId());
        return beastParameter;
    }

    @Override
    public Class getValueClass() {
        return ContinuousCharacterData.class;
    }

    @Override
    public Class<RealParameter> getBEASTClass() {
        return RealParameter.class;
    }
}
