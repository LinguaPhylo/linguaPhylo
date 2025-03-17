package lphy.base.evolution;

import lphy.core.logger.TextFileFormatted;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class VCFLines implements TextFileFormatted {
    private List<VCFSite> sites;

    public VCFLines(List<VCFSite> sites) {
        this.sites = new ArrayList<>(sites);
    }

    public void addSite(VCFSite site) {
        this.sites.add(site);
    }

    public List<VCFSite> getSites() {
        return sites;
    }

    public String[] getTaxaNames(){
        List<String> names = new ArrayList<>();
        for (VCFSite site : sites) {
            if (names.size() == 0) {
                names.add(site.getName());
            } else {
                if (! names.contains(site.getName())) {
                    names.add(site.getName());
                }
            }
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public List<String> getTextForFile() {
        List<String> lines = new ArrayList<>();
        String formattedDate = getDate();

        // add info lines
        lines.add("##fileformat=VCFv4.3");
        lines.add("##fileDate="+formattedDate);
        lines.add("##source=LinguaPhylo");
        lines.add("##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">");

        String[] names = getTaxaNames();
        lines.add("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\t" + String.join("\t", names));

        this.sites.sort(Comparator.comparingInt(VCFSite::getPosition));

        for (VCFSite site: this.sites) {
            StringBuilder line = new StringBuilder();
            line.append(site.getName()).append("\t")
                    .append(site.getPosition()).append("\t")
                    .append(".").append("\t")   // ID (missing)
                    .append(site.getCanonicalState(site.getRef())).append("\t")  // REF
                    .append(site.getCanonicalState(site.getAlt())).append("\t")  // ALT
                    .append(".").append("\t")  // QUAL
                    .append("PASS").append("\t")  // FILTER
                    .append(".").append("\t")  // INFO
                    .append("GT").append("\t");  // FORMAT

            String genotype = fillInGenotype(site, site.getName(), names);
            line.append(genotype);

            lines.add(line.toString());
        }

        return lines;
    }

    private String fillInGenotype(VCFSite site, String name, String[] names) {
        StringBuilder genotype = new StringBuilder();
        int index = Arrays.stream(names).toList().indexOf(name);
        int count = 0;

        while(count < index){
            genotype.append(".").append("\t");
            count++;
        }

        genotype.append(site.getGenotype()).append("\t");
        for (int i = count; i<names.length-1; i++){
            genotype.append(".").append("\t");
        }
        return genotype.toString();
    }

    private static String getDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = today.format(formatter);
        return formattedDate;
    }

    @Override
    public String getFileType() {
        return ".vcf";
    }
}
