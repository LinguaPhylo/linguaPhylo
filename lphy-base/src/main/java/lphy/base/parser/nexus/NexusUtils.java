package lphy.base.parser.nexus;

import lphy.base.evolution.HasTaxa;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.evolution.tree.TimeTree;

import java.util.List;
import java.util.Objects;

public class NexusUtils {

    public static final String KEY_WORD = "#NEXUS";

    public static String buildHeader(HasTaxa hasTaxa) {
        StringBuilder builder = new StringBuilder();
        builder.append(KEY_WORD).append("\n\n");

        String taxaBlock = getTaxaBlock(Objects.requireNonNull(hasTaxa));
        builder.append(taxaBlock).append("\n");

        NexusBlock nexusBlock = createNexusBlock(hasTaxa);
        builder.append("begin ").append(nexusBlock.getBlockName()).append(";");
        // use println, so not require \n in the last
        return builder.toString();
    }

    public static String buildBody(SimpleAlignment simpleAlignment) {
        StringBuilder builder = new StringBuilder();

        NexusBlock nexusBlock = createNexusBlock(Objects.requireNonNull(simpleAlignment));
        for (String line : nexusBlock.getBlockLines())
            builder.append("\t").append(line).append(";\n");

        return builder.toString();
    }

    public static String buildBody(TimeTree tree) {
        String newick = Objects.requireNonNull(tree).toString();
        if (! newick.endsWith(";")) {
            newick += ";";
        }
        return newick;
    }

    public static String buildFooter() {
        return "end;\n";
    }

    private static NexusBlock createNexusBlock(HasTaxa hasTaxa) {
        NexusBlock nexusBlock;
        if (hasTaxa instanceof SimpleAlignment alignment) {
            nexusBlock = new CharactersBlock(alignment);
        } else if (hasTaxa instanceof TimeTree tree) {
            //TODO do not use this to build body
            nexusBlock = new TreesBlock(List.of(tree));
        } else
            throw new UnsupportedOperationException("Cannot support " + hasTaxa.getClass() + " to nexus format !");
        return nexusBlock;
    }


    private static String getTaxaBlock(HasTaxa hasTaxa) {
        String[] taxa = null;
        if (hasTaxa != null)
            taxa = Objects.requireNonNull(hasTaxa.getTaxa()).getTaxaNames();

        if (taxa == null || taxa.length < 1)
            throw new RuntimeException("Taxa cannot be empty !");
        return (new TaxaBlock(taxa)).toString();
    }

}
