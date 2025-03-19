package lphy.base.parser;

import lphy.base.evolution.datatype.Variant;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VCFUtils {
    public static final String KEY_WORD = "##fileformat=VCFv4.3";

    public static String buildHeader(String[] taxaNames){
        StringBuilder builder = new StringBuilder();
        builder.append(KEY_WORD).append("\n");

        String formattedDate = getDate();
        builder.append("##fileDate=" + formattedDate).append("\n");

        builder.append("##source=LinguaPhylo").append("\n");
        builder.append("##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">").append("\n");

        builder.append("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\t" + String.join("\t", taxaNames));

        return builder.toString();
    }

    public static String buildBody (Variant[] variants) {
        StringBuilder builder = new StringBuilder();

        for (Variant variant: variants) {
            StringBuilder line = new StringBuilder();
            line.append(variant.getName()).append("\t")
                    .append(variant.getPosition()+1).append("\t") //vcf position starts from 1
                    .append(".").append("\t")   // ID (missing)
                    .append(variant.getCanonicalState(variant.getRef())).append("\t")  // REF
                    .append(variant.getCanonicalState(variant.getAlt())).append("\t")  // ALT
                    .append(".").append("\t")  // QUAL
                    .append("PASS").append("\t")  // FILTER
                    .append(".").append("\t")  // INFO
                    .append("GT").append("\t");  // FORMAT

            String genotype = fillInGenotype(variant, variant.getName(), getTaxaNames(variants));
            line.append(genotype);

            builder.append(line);
        }

        return builder.toString();
    }

    private static String getDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return today.format(formatter);
    }

    private static String fillInGenotype(Variant variant, String name, String[] names) {
        StringBuilder genotype = new StringBuilder();
        int index = Arrays.stream(names).toList().indexOf(name);
        int count = 0;

        while(count < index){
            genotype.append(".").append("\t");
            count++;
        }

        genotype.append(variant.getGenotype()).append("\t");
        for (int i = count; i<names.length-1; i++){
            genotype.append(".").append("\n");
        }
        return genotype.toString();
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
