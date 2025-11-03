package lphy.base.evolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mpileup {
    private final String chromName;
    private final int position;
    private final int ref;

    private final Map<String, PileupSite.CellPileupData> pileupData = new HashMap<>();

    /*
        only support one chromosome name and one position for a Mpileup
     */
    public Mpileup(String chromName, int position, int ref, Map<String, PileupSite.CellPileupData> pileupData) {
        this.chromName = chromName;
        this.position = position;
        this.ref = ref;
        this.pileupData.putAll(pileupData);
    }

    public Mpileup(String chromName, int position, int ref, List<PileupSite> sites) {
        this.chromName = chromName;
        this.position = position;
        this.ref = ref;
        for (PileupSite site : sites) {
            String name = site.getCellPosition().getCellName();
            PileupSite.CellPileupData data = site.getCellPileupData();
            pileupData.put(name, data);
        }
    }

    public Map<String, PileupSite.CellPileupData> getPileupData() {
        return pileupData;
    }
    public void addPileupData(String cellName, PileupSite.CellPileupData data) {
        pileupData.put(cellName, data);
    }
    public void removePileupData(String cellName) {
        pileupData.remove(cellName);
    }

    public String getChromName() {
        return chromName;
    }
    public int getPosition() {
        return position;
    }
    public int getRef() {
        return ref;
    }
}
