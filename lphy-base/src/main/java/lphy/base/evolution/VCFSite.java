package lphy.base.evolution;

import java.util.ArrayList;
import java.util.List;

public class VCFSite{
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

    // TODO: deal with ambiguities
    public String getCanonicalState(int index){
        if (index == 0){
            return "A";
        } else if (index == 1){
            return "C";
        } else if (index == 2){
            return "G";
        } else if (index == 3){
            return "T";
        }
        return null;
    }

    public String getGenotype(){
        return genotype;
    }

    public void addSite(List<VCFSite> sites, VCFSite site) {
        sites.add(site);
    }

}
