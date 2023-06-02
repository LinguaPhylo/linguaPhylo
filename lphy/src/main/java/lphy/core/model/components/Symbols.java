package lphy.core.model.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Symbols {

    public static final String[] symbolNames = {
            "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa",
            "lambda", "mu", "nu", "xi", "omicron", "pi", "rho", "sigma", "tau", "upsilon",
            "phi", "chi", "psi", "omega", "Gamma", "Delta", "Theta", "Lambda", "Xi", "Pi",
            "Sigma", "Omega", "propto"};

    public static final String[] symbolCodes = prepend("\\", symbolNames);

    public static final String[] unicodeSymbols = {
            "α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο", "π", "ρ", "σ", "τ", "υ", "φ",
            "χ", "ψ", "ω", "Γ", "Δ", "Θ", "Λ", "Ξ", "Π", "Σ", "Ω", "∝"};

    public static final String allUnicodeSymbols = String.join("", unicodeSymbols);

    private static String[] prepend(String pre, String[] array) {
        String[] prepended = new String[array.length];
        for (int i = 0; i < prepended.length; i++) {
            prepended[i] = pre + array[i];
        }
        return prepended;
    }

    static final Map<String,String> symbolToNameMap = IntStream.range(0, unicodeSymbols.length).boxed()
    .collect(Collectors.toMap(i -> unicodeSymbols[i], i -> symbolNames[i]));

    static final Map<String,String> nameToSymbolMap = IntStream.range(0, unicodeSymbols.length).boxed()
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

    /**
     * @param name a name that may contain unicode letters/symbols and ascii characters
     * @return a list of text blocks, each block is either canonicalized or left unchanged, and together they represent the original name.
     */
    public static List<Block> getCanonicalizedName(String name) {

        List<Block> blocks = new ArrayList<>();

        StringBuilder currentAsciiBlock = new StringBuilder();
        boolean inAsciiBlock = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            int index = allUnicodeSymbols.indexOf(c);
            if (index >=0) {
                if (inAsciiBlock) {
                    blocks.add(new Block(currentAsciiBlock.toString(), false));
                    currentAsciiBlock = new StringBuilder();
                }
                blocks.add(new Block(symbolNames[index], true));
            } else {
                if (!inAsciiBlock) {
                    inAsciiBlock = true;
                }
                currentAsciiBlock.append(c);
            }
        }
        if (inAsciiBlock) {
            inAsciiBlock = false;
            blocks.add(new Block(currentAsciiBlock.toString(), false));
        }
        return blocks;
    }

    public static class Block {
        public final String string;
        public final boolean isCanonicalized;

        public Block(String string, boolean isCanonicalized) {
            this.string = string;
            this.isCanonicalized = isCanonicalized;
        }
    }
}
