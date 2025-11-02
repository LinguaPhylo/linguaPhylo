package lphy.base.evolution.datatype;

import lphy.core.logger.LoggerUtils;

import java.util.*;

public class Variant{
    private String taxaName;
    private int position;
    private int ref;
    private int alt;
    private String genotype;

    public Variant(String taxaName, int position, int ref, int alt, String genotype) {
        this.taxaName = taxaName;
        this.position = position;
        this.ref = ref;
        this.alt = alt;
        this.genotype = genotype;
    }

    public String getName(){
        return taxaName;
    }

    public int getPosition(){
        return position;
    }

    public int getRef(){
        return ref;
    }

    public void setRef(int ref){
        this.ref = ref;
    }

    public int getAlt(){
        return alt;
    }

    public void setAlt(int alt){
        this.alt = alt;
    }

    public String getGenotype(){
        return genotype;
    }

    public void setGenotype(String genotype){
        this.genotype = genotype;
    }

    private static final String[] SYMBOLS = {
            "A", "C", "G", "T",
            "R", "Y", "M", "W", "S", "K", "B", "D", "H", "V",
            "N", "?", "-"
    };

    // Reverse lookup table for quick symbol → index mapping
    private static final Map<String, Integer> SYMBOL_TO_INDEX = Map.ofEntries(
            Map.entry("A", 0), Map.entry("C", 1), Map.entry("G", 2), Map.entry("T", 3),
            Map.entry("R", 4), Map.entry("Y", 5), Map.entry("M", 6), Map.entry("W", 7),
            Map.entry("S", 8), Map.entry("K", 9), Map.entry("B", 10), Map.entry("D", 11),
            Map.entry("H", 12), Map.entry("V", 13),
            Map.entry("N", 14), Map.entry("?", 15), Map.entry("-", 16)
    );

    public static String getCanonicalState(int index) {
        return (index >= 0 && index < SYMBOLS.length)
                ? SYMBOLS[index]
                : "?"; // fallback for invalid index
    }

    public static int getCanonicalState(String symbol) {
        if (symbol == null) return -1;
        Integer idx = SYMBOL_TO_INDEX.get(symbol.toUpperCase());
        return (idx != null) ? idx : -1;
    }

    public static String inferGenotype(int ref, int alt) {
        String genotype = "";
        if (ref == alt){
            genotype = "0|0";
        } else if (ref !=alt){
            genotype = "0|1";
        }
        return genotype;
    }

    public class VariantFinder {
        private static Map<String, Map<Integer, Variant>> variantIndex = null;

        public static Variant getVariant(List<Variant> variants, String taxonName, int position) {
            if (variantIndex == null) {
                variantIndex = new HashMap<>();
                for (Variant variant : variants) {
                    variantIndex.computeIfAbsent(variant.getName(), k -> new HashMap<>()).put(variant.getPosition(), variant);
                }
            }

            return variantIndex.getOrDefault(taxonName, Collections.emptyMap()).get(position);
        }
    }

    public static String[] getTaxaNames(Variant[] variants){
        List<String> names = new ArrayList<>();
        for (Variant variant: variants) {
            if (names.isEmpty()) {
                names.add(variant.getName());
            } else {
                if (! names.contains(variant.getName())) {
                    names.add(variant.getName());
                }
            }
        }
        return names.toArray(new String[names.size()]);
    }

}
