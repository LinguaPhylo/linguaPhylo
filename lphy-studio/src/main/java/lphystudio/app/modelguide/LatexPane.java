package lphystudio.app.modelguide;

import lphy.core.model.annotation.Citation;
import lphy.core.model.component.GeneratorCategory;
import lphy.core.narrative.NarrativeUtils;
import lphystudio.core.narrative.LaTeXUtils;

import javax.swing.*;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Walter Xie
 */
public class LatexPane extends JTextPane {

    private final List<GeneratorCategory> excluded = List.of(GeneratorCategory.NONE);

    final ModelGuide modelGuide;

    public LatexPane(ModelGuide modelGuide) {
        this.modelGuide = modelGuide;

//        setLatexTable(modelGuide.getSelectedModels());
        setLatexTable();

        setEditable(false);
        setAutoscrolls(true);
    }

    public void setLatexTable() {
        setText(createLatexTable(modelGuide.getModelsExcl(excluded)));
    }

    private final String INDENT = "    ";
    private final int MAX_ROW_PER_PAGE = 40;
    private final int MAX_ROW_1ST_PAGE = 25;

    // insertion-order (in which keys were inserted into the map)
    private Map<String, Citation> refMap = new LinkedHashMap<>();

    private String createLatexTable(List<Model> models) {
        refMap.clear();
        StringBuilder builder = new StringBuilder("\\documentclass{article}\n")
                .append("\\begin{document}\n");

        makeTitle(builder);
        builder.append("Auto-generated LaTeX table from Model Guide.").append("\\ \n\n")
                .append("%% If the category All is selected, the category None will be excluded from table.\n")
                .append("The category ").append(modelGuide.getCurrentCategory())
                .append(" is selected, which includes ")
                .append(firstCharLowCase(modelGuide.getCurrentCategory().getDescription()))
                .append(".\\\\ \n\n");

        tableBegin(builder);

        String key;
        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            Citation citation = model.getCitation();
            if (citation == null) key = "";
            else {
                key = NarrativeUtils.getCitationKey(citation, "et. al.");
                refMap.put(key, citation);
            }
            // Latex table rows here
            builder.append(INDENT).append(model.getName()).append(" & ").
                    append(model.getNarrativeName());
            if (!key.isEmpty()) // \cite{key}, key must be valid in biblatex
                builder.append("\\cite{").append(sanitizeKey(key)).append("}");
            // examples
            builder.append(" & ").append(String.join(", ", model.getExamples()));
            builder.append("\\\\  \n");

            if (i >= MAX_ROW_1ST_PAGE && (i-MAX_ROW_1ST_PAGE) % MAX_ROW_PER_PAGE == 0) {
                tableEnd(builder);
                builder.append("\\pagebreak\n");
                tableBegin(builder);
            }
        }

        tableEnd(builder);

        referenceSection(builder);

        builder.append("\\end{document}");
        return builder.toString();
    }

    private void makeTitle(StringBuilder builder) {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat= new SimpleDateFormat("dd/MMM/yyyy");
        builder.append("\n\\title{LPhy Models}\n")
                .append("\\date{ ").append(dateFormat.format(currentDate)).append(" }\n")
                .append("\n\\maketitle\n\n");
    }

    private void tableBegin(StringBuilder builder) {
        builder.append("\n\\begin{tabular}{ l | l | l }\n").append(INDENT).append("\\hline\\hline\n")
                // column header
                .append(INDENT).append("Name & Brief & Examples \\\\ \n")
                .append(INDENT).append("\\hline\\hline\n");
    }

    private void tableEnd(StringBuilder builder) {
        builder.append(INDENT).append("\\hline\n").append("\\end{tabular}\n");
    }

    private String firstCharLowCase(String string) {
        char[] c = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    private void referenceSection(StringBuilder builder) {
        if (refMap.size() > 0) {
            builder.append("\\begin{thebibliography}{9}\n\n");
            for (Map.Entry<String, Citation> ci : refMap.entrySet()) {
                builder.append("\\bibitem{");
                builder.append(sanitizeKey(ci.getKey())); // must be valid in biblatex
                builder.append("}\n");

                String ref = LaTeXUtils.sanitizeText(ci.getValue().value());

                builder.append(ref);
                builder.append("\n\n");
            }
            builder.append("\\end{thebibliography}\n");
        }
    }

    private String sanitizeKey(String key) {
        String citeKey = key.replaceAll(" and ", "&")
                .replaceAll("\\.", "_")
                .replaceAll(" et_", "_et_")
                .replaceAll("\\s+", "");
                // replace all non ASCII letters
//                .replaceAll("[^\\x00-\\x7F]", "_");
        return deAccent(citeKey);
    }

    // convert accent letters to English alphabet
    private String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
}
