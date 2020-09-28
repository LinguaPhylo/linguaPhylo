package lphy.app;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Symbols {

    static String[] greekLetterNames = {
            "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa",
            "lambda", "mu", "nu", "xi", "omicron", "pi", "rho", "sigma", "tau", "upsilon",
            "phi", "chi", "psi", "omega", "Gamma", "Delta", "Theta", "Lambda", "Xi", "Pi",
            "Sigma", "Omega"};

    static String[] greekLetterCodes = prepend("\\", greekLetterNames);

    private static String[] prepend(String pre, String[] array) {
        String[] prepended = new String[array.length];
        for (int i = 0; i < prepended.length; i++) {
            prepended[i] = pre + array[i];
        }
        return prepended;
    }

    static String[] greekLetters = {
            "α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο", "π", "ρ", "σ", "τ", "υ", "φ",
            "χ", "ψ", "ω", "Γ", "Δ", "Θ", "Λ", "Ξ", "Π", "Σ", "Ω"};

    static Map<String,String> symbolToNameMap = IntStream.range(0, greekLetters.length).boxed()
    .collect(Collectors.toMap(i -> greekLetters[i], i -> greekLetterNames[i]));

    static Map<String,String> nameToSymbolMap = IntStream.range(0, greekLetters.length).boxed()
            .collect(Collectors.toMap(i -> greekLetterNames[i], i -> greekLetters[i]));

    public static String getCanonical(String name) {
        for (int i = 0; i < greekLetters.length; i++) {
            if (name.contains(greekLetters[i])) {
                int index = name.indexOf(greekLetters[i]);
                String newname = name.substring(0, index) + symbolToNameMap.get(greekLetters[i]);
                if (index < name.length()-1) {
                   newname += name.substring(index+1);
                }
                name = newname;
            }
        }
        return name;
    }
}
