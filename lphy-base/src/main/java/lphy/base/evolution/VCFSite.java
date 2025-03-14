package lphy.base.evolution;

import lphy.core.logger.TextFileFormatted;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * implement a VCFSites data type {@link TextFileFormatted},
 * which will trigger logging as a vcf format to file.
 */
public class VCFSite implements TextFileFormatted{
    private List<VCFSite> sites = new ArrayList<>();
    private String taxaName;
    private int position;
    private int ref;
    private int alt;
    private String genotype;

    public VCFSite(String taxaName, int position, int ref, int alt, String genotype) {
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

    public int getAlt(){
        return alt;
    }

    public String getGenotype(){
        return genotype;
    }

    public void addSite(List<VCFSite> sites, VCFSite site) {
        sites.add(site);
    }

    public String toVCFLines(){
        return taxaName + "\t" + position + "\t.\t" + ref + "\t" + alt + "\t.\t" + "PASS" + "\t.\t" + "GT" + genotype + "\t";
    }

    @Override
    public List<String> getTextForFile() {
        List<String> lines = new ArrayList<>();
        String formattedDate = getDate();

        // add info lines
        lines.add("##fileformat=VCFv4.3\n");
        lines.add("##fileDate="+formattedDate+"\n");
        lines.add("##source=LinguaPhylo");
        lines.add("##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">");
        lines.add("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tCELL\n");

        // add each site line
        for (VCFSite site : this.sites) {
            lines.add(site.toVCFLines());
        }

        return lines;
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
