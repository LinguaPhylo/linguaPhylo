package lphy.app;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Symbols {

    static String[] symbolNames = {
            "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa",
            "lambda", "mu", "nu", "xi", "omicron", "pi", "rho", "sigma", "tau", "upsilon",
            "phi", "chi", "psi", "omega", "Gamma", "Delta", "Theta", "Lambda", "Xi", "Pi",
            "Sigma", "Omega", "propto"};

    static String[] symbolCodes = prepend("\\", symbolNames);

    private static String[] prepend(String pre, String[] array) {
        String[] prepended = new String[array.length];
        for (int i = 0; i < prepended.length; i++) {
            prepended[i] = pre + array[i];
        }
        return prepended;
    }

    static String[] unicodeSymbols = {
            "α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο", "π", "ρ", "σ", "τ", "υ", "φ",
            "χ", "ψ", "ω", "Γ", "Δ", "Θ", "Λ", "Ξ", "Π", "Σ", "Ω", "∝"};

    static Map<String,String> symbolToNameMap = IntStream.range(0, unicodeSymbols.length).boxed()
    .collect(Collectors.toMap(i -> unicodeSymbols[i], i -> symbolNames[i]));

    static Map<String,String> nameToSymbolMap = IntStream.range(0, unicodeSymbols.length).boxed()
            .collect(Collectors.toMap(i -> symbolNames[i], i -> unicodeSymbols[i]));

    public static String getCanonical(String name) {
       return getCanonical(name, "", "");
    }

    public static String getCanonical(String name, String prefix, String suffix) {
        if (name == null) return null;
        for (String unicodeSymbol : unicodeSymbols) {
            if (name.contains(unicodeSymbol)) {
                int index = name.indexOf(unicodeSymbol);
                String newname = name.substring(0, index) + prefix + symbolToNameMap.get(unicodeSymbol) + suffix;
                if (index < name.length() - 1) {
                    newname += name.substring(index + 1);
                }
                name = newname;
            }
        }
        return name;
    }
}
