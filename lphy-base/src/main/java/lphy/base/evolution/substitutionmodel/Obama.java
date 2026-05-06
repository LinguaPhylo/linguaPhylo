package lphy.base.evolution.substitutionmodel;

import lphy.core.model.Value;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.DoubleArray2DValue;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Obama-style amino acid model averaging
 * (Bouckaert 2020). Picks one of an arbitrary subset of empirical amino acid
 * substitution models by integer index. Coupling the index to a stochastic node
 * (e.g. {@code modelIndicator ~ UniformDiscrete(lower=0, upper=k-1)}) averages
 * the posterior over the selected models exactly as in OBAMA's BEAUti template,
 * which lets the user pick an arbitrary subset of the 15 supported models.
 * <p>
 * Available model names (case-insensitive):
 * {@code WAG, JTT, LG, Dayhoff, DCMut, cpREV, mtREV, mtMam, mtArt,
 *        Blosum62, VT, RtREV, FLU, HIVb, HIVw}.
 * If {@code models} is omitted, all 15 are used in the order above.
 * <p>
 * Frequency override behaviour mirrors OBAMA's {@code useExternalFreqs} switch:
 * <ul>
 *   <li>{@code useExternalFreqs} unset: use the selected model's empirical
 *       frequencies, unless {@code freq} is supplied — in which case {@code freq}
 *       overrides them.</li>
 *   <li>{@code useExternalFreqs=true}: use {@code freq} (which must be supplied).</li>
 *   <li>{@code useExternalFreqs=false}: use the selected model's empirical
 *       frequencies, ignoring any supplied {@code freq}.</li>
 * </ul>
 */
@Citation(value = "Bouckaert, R. (2020). " +
        "OBAMA: OBAMA for Bayesian amino-acid model averaging. " +
        "PeerJ, 8, e9460.",
        title = "OBAMA: OBAMA for Bayesian amino-acid model averaging",
        year = 2020,
        authors = {"Bouckaert"},
        DOI = "https://doi.org/10.7717/peerj.9460")
public class Obama extends RateMatrix {

    public static final String modelIndicatorParamName = "modelIndicator";
    public static final String modelsParamName = "models";
    public static final String useExternalFreqsParamName = "useExternalFreqs";

    /** All supported empirical AA model names, in canonical (default) order. */
    public static final String[] ALL_MODELS = {
            "WAG", "JTT", "LG", "Dayhoff", "DCMut",
            "cpREV", "mtREV", "mtMam", "mtArt", "Blosum62",
            "VT", "RtREV", "FLU", "HIVb", "HIVw"
    };

    /** Lower-cased name -> canonical name, for case-insensitive lookup. */
    private static final Map<String, String> CANONICAL_NAME = new LinkedHashMap<>();
    static {
        for (String n : ALL_MODELS) CANONICAL_NAME.put(n.toLowerCase(Locale.ROOT), n);
    }

    protected final String freqParamName = SubstModelParamNames.FreqParamName;

    public Obama(@ParameterInfo(name = modelIndicatorParamName,
            description = "integer in [0, k-1] selecting one of the k empirical AA models in the models parameter " +
                    "(or in [0, 14] when models is omitted, indexing the canonical OBAMA list).")
                 Value<Integer> modelIndicator,
                 @ParameterInfo(name = modelsParamName,
                         description = "optional subset of empirical AA model names (case-insensitive). " +
                                 "Allowed: WAG, JTT, LG, Dayhoff, DCMut, cpREV, mtREV, mtMam, mtArt, " +
                                 "Blosum62, VT, RtREV, FLU, HIVb, HIVw. Defaults to all 15.",
                         optional = true) Value<String[]> models,
                 @ParameterInfo(name = useExternalFreqsParamName,
                         description = "if true, use the supplied freq; if false, use the selected model's empirical frequencies. " +
                                 "If unset, freq overrides empirical when supplied.",
                         optional = true) Value<Boolean> useExternalFreqs,
                 @ParameterInfo(name = SubstModelParamNames.FreqParamName,
                         description = "optional base frequencies (alphabetical AA order) overriding the selected model's empirical frequencies.",
                         optional = true) Value<Double[]> freq,
                 @ParameterInfo(name = RateMatrix.meanRateParamName,
                         description = "the mean rate of the process. default 1.0",
                         optional = true) Value<Number> meanRate) {
        super(meanRate);
        if (modelIndicator == null)
            throw new IllegalArgumentException(modelIndicatorParamName + " is required");
        setParam(modelIndicatorParamName, modelIndicator);
        if (models != null) {
            // validate eagerly: every name must be a recognised AA model
            String[] names = models.value();
            if (names == null || names.length == 0)
                throw new IllegalArgumentException("Obama: models must contain at least one name");
            for (String n : names) canonicalize(n);
            setParam(modelsParamName, models);
        }
        if (useExternalFreqs != null) setParam(useExternalFreqsParamName, useExternalFreqs);
        if (freq != null) {
            if (freq.value().length != 20)
                throw new IllegalArgumentException("Amino acid frequencies must have 20 dimensions.");
            setParam(freqParamName, freq);
        }
    }

    @GeneratorInfo(name = "obama", verbClause = "is", narrativeName = "OBAMA model averaging",
            category = GeneratorCategory.RATE_MATRIX, examples = {"obamaCoalescent.lphy"},
            description = "OBAMA-style averaging over an arbitrary subset of 15 empirical amino acid " +
                    "substitution models, selected by an integer indicator. Optional useExternalFreqs " +
                    "gates whether the supplied freq overrides the model's empirical frequencies.")
    public Value<Double[][]> apply() {
        @SuppressWarnings("unchecked")
        Value<Integer> indicator = (Value<Integer>) getParams().get(modelIndicatorParamName);
        @SuppressWarnings("unchecked")
        Value<String[]> models = (Value<String[]>) getParams().get(modelsParamName);
        String[] names = models != null ? models.value() : ALL_MODELS;

        int idx = indicator.value();
        if (idx < 0 || idx >= names.length)
            throw new IllegalArgumentException(
                    "Obama modelIndicator out of range [0," + (names.length - 1) + "]: " + idx);

        @SuppressWarnings("unchecked")
        Value<Double[]> freq = (Value<Double[]>) getParams().get(freqParamName);
        @SuppressWarnings("unchecked")
        Value<Boolean> useExt = (Value<Boolean>) getParams().get(useExternalFreqsParamName);
        Value<Number> meanRate = getParams().get(meanRateParamName);

        // Resolve OBAMA's useExternalFreqs switch.
        Value<Double[]> effectiveFreq;
        if (useExt == null) {
            effectiveFreq = freq;
        } else if (useExt.value()) {
            if (freq == null)
                throw new IllegalArgumentException(
                        "Obama: useExternalFreqs=true but no freq supplied");
            effectiveFreq = freq;
        } else {
            effectiveFreq = null;
        }

        EmpiricalAminoAcidModel model = createModel(canonicalize(names[idx]), effectiveFreq, meanRate);
        Double[][] Q = model.apply().value();
        return new DoubleArray2DValue(Q, this);
    }

    private static String canonicalize(String name) {
        String c = CANONICAL_NAME.get(name == null ? "" : name.toLowerCase(Locale.ROOT));
        if (c == null) throw new IllegalArgumentException(
                "Obama: unknown AA model name '" + name + "'. Allowed: " + String.join(", ", ALL_MODELS));
        return c;
    }

    private static EmpiricalAminoAcidModel createModel(String canonicalName,
                                                       Value<Double[]> freq,
                                                       Value<Number> meanRate) {
        return switch (canonicalName) {
            case "WAG"      -> new WAG(freq, meanRate);
            case "JTT"      -> new JTT(freq, meanRate);
            case "LG"       -> new LG(freq, meanRate);
            case "Dayhoff"  -> new Dayhoff(freq, meanRate);
            case "DCMut"    -> new DCMut(freq, meanRate);
            case "cpREV"    -> new CpREV(freq, meanRate);
            case "mtREV"    -> new MtREV(freq, meanRate);
            case "mtMam"    -> new MtMam(freq, meanRate);
            case "mtArt"    -> new MtArt(freq, meanRate);
            case "Blosum62" -> new Blosum62(freq, meanRate);
            case "VT"       -> new VT(freq, meanRate);
            case "RtREV"    -> new RtREV(freq, meanRate);
            case "FLU"      -> new FLU(freq, meanRate);
            case "HIVb"     -> new HIVb(freq, meanRate);
            case "HIVw"     -> new HIVw(freq, meanRate);
            default -> throw new IllegalStateException("unreachable: " + canonicalName);
        };
    }
}
