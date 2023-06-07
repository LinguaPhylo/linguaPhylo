package lphy.core.model;

import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.CitationUtils;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

import java.util.List;
import java.util.Map;

import static lphy.core.model.GeneratorUtils.getGeneratorInfo;

public interface GeneratorNarrative {

    default String getInferenceStatement(Generator generator, Value value, Narrative narrative) {

        return NarrativeUtils.getGeneratorInferenceStatement(generator, value, narrative);
    }

    default String getInferenceNarrative(Generator generator, Value value, boolean unique, Narrative narrative) {

        String narrativeName = generator.getNarrativeName();

        GeneratorInfo info = getGeneratorInfo(this.getClass());
        Citation cite = CitationUtils.getCitation(this.getClass());
        String citationString = narrative.cite(cite);

        String verbClause = info != null ? info.verbClause() : "comes from";
        StringBuilder builder = new StringBuilder();
        builder.append(NarrativeUtils.getValueClause(value, unique, narrative));
        builder.append(" ");
        builder.append(verbClause);
        builder.append(" ");
        if (!(this instanceof ExpressionNode)) {
            if (this instanceof DeterministicFunction) {
                builder.append(NarrativeUtils.getDefiniteArticle(narrativeName, true));
            } else {
                builder.append(NarrativeUtils.getIndefiniteArticle(narrativeName, true));
            }
        }
        builder.append(" ");
        builder.append(narrativeName);
        if (citationString != null && citationString != "") {
            builder.append(" ");
            builder.append(citationString);
        }

        Map<String, Value> params = generator.getParams();
        String currentVerb = "";
        List<ParameterInfo> parameterInfos = generator.getParameterInfo(0);
        int count = 0;
        for (ParameterInfo parameterInfo : parameterInfos) {
            Value v = params.get(parameterInfo.name());
            if (v != null) {
                if (count == 0) builder.append(" ");
                if (count > 0) {
                    if (count == params.size() - 1) {
                        builder.append(" and ");
                    } else {
                        builder.append(", ");
                    }
                }
                if (!parameterInfo.verb().equals(currentVerb)) {
                    currentVerb = parameterInfo.verb();
                    builder.append(currentVerb);
                    builder.append(" ");
                }
                builder.append(NarrativeUtils.getValueClause(v, false, true, false, generator, narrative));
                count += 1;
            }
        }
        builder.append(".");
        return builder.toString();
    }

}
