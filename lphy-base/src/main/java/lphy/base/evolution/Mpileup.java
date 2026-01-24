package lphy.base.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Mpileup {
    private final String[] chromNames;
    private final int[] positions;
    private final int[] ref;

    private final List<Map<String, PileupSite.CellPileupData>> pileupData = new ArrayList<>();

    /*
        only support one chromosome name and one position for a Mpileup
     */
    public Mpileup(String[] chromName, int[] positions, int[] ref, List<Map<String, PileupSite.CellPileupData>> pileupData) {
        this.chromNames = chromName;
        this.positions = positions;
        this.ref = ref;
        if (!pileupData.isEmpty()) {
            Set<String> referenceKeys = pileupData.get(0).keySet();

            for (int i = 1; i < pileupData.size(); i++) {
                if (!pileupData.get(i).keySet().equals(referenceKeys)) {
                    throw new IllegalArgumentException("The taxa names are not the same in pileupData");
                }
            }
            this.pileupData.addAll(pileupData);
        }
    }

//    public Mpileup(String chromName, int position, int ref, List<PileupSite> sites) {
//        this.chromName = chromName;
//        this.position = position;
//        this.ref = ref;
//        for (PileupSite site : sites) {
//            String name = site.getCellPosition().getCellName();
//            PileupSite.CellPileupData data = site.getCellPileupData();
//            pileupData.put(name, data);
//        }
//    }

    public  List<Map<String, PileupSite.CellPileupData>> getPileupData() {
        return pileupData;
    }
//    public void addPileupData(String cellName, PileupSite.CellPileupData data) {
//        pileupData.put(cellName, data);
//    }
    public void removePileupData(String cellName) {
        pileupData.remove(cellName);
    }

    public String[] getChromNames() {
        return chromNames;
    }
    public int[] getPositions() {
        return positions;
    }
    public int[] getRefs() {
        return ref;
    }

    public int getNumPileups() {
        return pileupData.size();
    }
}
