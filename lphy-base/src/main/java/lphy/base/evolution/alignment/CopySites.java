package lphy.base.evolution.alignment;

import lphy.core.model.components.*;
import lphy.core.util.LoggerUtils;

import static lphy.base.evolution.alignment.AlignmentUtils.ALIGNMENT_PARAM_NAME;

/**
 * @author Walter Xie
 */
public class CopySites extends DeterministicFunction<Alignment> {

    public final String indParamName = "ids";

    public CopySites(@ParameterInfo(name = indParamName,
            description = "the array of site indices (start from 0) of the original alignment.")
                     Value<Integer[]> siteIndices,
//                     @ParameterInfo(name = indParamName + "2",
//                               description = "optional: the 2nd array of site indices of the original alignment.",
//                       optional = true) Value<Integer[]> siteIndices2,
                     @ParameterInfo(name = AlignmentUtils.ALIGNMENT_PARAM_NAME,
            description = "the original alignment.") Value<Alignment> originalAlignment) {
        setParam(indParamName, siteIndices);
//        setParam(indParamName + "2", siteIndices2); //optional

        Alignment origAlg = originalAlignment.value();
        if (origAlg == null)
            throw new IllegalArgumentException("Cannot find Alignment ! " + originalAlignment.getId());
        setParam(ALIGNMENT_PARAM_NAME, originalAlignment);
    }

    @GeneratorInfo(name = "copySites", verbClause = "is created by",
            category = GeneratorCategory.TAXA_ALIGNMENT,
            description = "Create a new alignment by copying sites from the original alignment. " +
                    "The sites can be duplicated. Use other function to sample or manipulate the site indices.")
    public Value<Alignment> apply() {

        Value<Alignment> originalAlignment = getAlignment();
        final Alignment original = originalAlignment.value();
        Integer[] sitesId = (Integer[]) getParams().get(indParamName).value();

//        Value<Integer[]> siteIndices2 = getParams().get(indParamName + "2");
//        Integer[] sitesId2 = new Integer[0];
//        if (siteIndices2 != null && siteIndices2.value() != null) {
//            sitesId2 = siteIndices2.value();
//        }

        // have to know nchar before create a new alignment
        int nchar = sitesId.length;// + sitesId2.length;
        Alignment newAlignment = new SimpleAlignment(nchar, original);
        int tmpS;
        int si;
        for (int j = 0; j < sitesId.length; j++) {
            si = sitesId[j];
            for (int i = 0; i < original.ntaxa(); i++) {
                tmpS = original.getState(i, si);
                newAlignment.setState(i, j, tmpS);
            }
        }
        // continue 2nd array if given
//        for (int j = 0; j < sitesId2.length; j++) {
//            si = sitesId2[j];
//            for (int i = 0; i < original.ntaxa(); i++) {
//                tmpS = original.getState(i, si);
//                newAlignment.setState(i, j+sitesId.length, tmpS);
//            }
//        }
        LoggerUtils.log.info("Create new alignment copying " + newAlignment.nchar() +
                " sites (can be duplicated) from the original alignment " + originalAlignment.getId() );

        return new Value<>(null, newAlignment, this);
    }

    public Value<Alignment> getAlignment() {
        return getParams().get(ALIGNMENT_PARAM_NAME);
    }

}
