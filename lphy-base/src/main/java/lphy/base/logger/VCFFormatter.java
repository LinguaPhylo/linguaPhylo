package lphy.base.logger;

import lphy.base.parser.VCFUtils;
import lphy.base.evolution.Variant;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Symbols;

public class VCFFormatter implements ValueFormatter<Variant[]> {
    String valueID;
    Variant[] variants;

    public VCFFormatter(String valueID, Variant[] variants) {
        this.valueID = Symbols.getCanonical(valueID);
        this.variants = variants;
    }

    @Override
    public String getExtension() {
        return ".vcf";
    }

    @Override
    public String getRowName(int rowId) {
        return "";
    }

    @Override
    public Mode getMode() {
        return Mode.VALUE_PER_FILE;
    }


    @Override
    public Class<Variant[]> getDataTypeClass() {
        return Variant[].class;
    }

    @Override
    public String getValueID() {
        return valueID;
    }

    @Override
    public String header() {
        String[] taxaNames = VCFUtils.getTaxaNames(variants);
        return VCFUtils.buildHeader(taxaNames);
    }

    @Override
    public String format(Variant[] variants) {
        return VCFUtils.buildBody(variants);
    }

}
