package lphy.base.evolution.datatype;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void setRef(Variant variant,int ref){
        variant.ref = ref;
    }

    public int getAlt(){
        return alt;
    }

    public void setAlt(Variant variant, int alt){
        variant.alt = alt;
    }

    public String getGenotype(){
        return genotype;
    }

    public void setGenotype(Variant variant, String genotype){
        variant.genotype = genotype;
    }


    public String getCanonicalState(int index){
        if (index == 0){
            return "A";
        } else if (index == 1){
            return "C";
        } else if (index == 2){
            return "G";
        } else if (index == 3){
            return "T";
        } else if (index == 4){
            return "R";
        } else if (index == 5){
            return "Y";
        } else if (index == 6){
            return "M";
        } else if (index == 7){
            return "W";
        } else if (index == 8){
            return "S";
        } else if (index == 9){
            return "K";
        } else if (index == 10){
            return "B";
        } else if (index == 11){
            return "D";
        } else if (index == 12){
            return "H";
        } else if (index == 13){
            return "V";
        } else if (index == 14){
            return "N";
        } else if (index == 15){
            return "?";
        } else if (index == 16){
            return "-";
        } else {
            return "invalid state index";
        }
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

}
