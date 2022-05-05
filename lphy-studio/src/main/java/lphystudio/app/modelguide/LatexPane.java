package lphystudio.app.modelguide;

import lphy.graphicalModel.Citation;
import lphy.graphicalModel.GeneratorCategory;
import lphy.graphicalModel.NarrativeUtils;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

    private String createLatexTable(List<Model> models) {
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
            else key = NarrativeUtils.getCitationKey(citation, "et. al.");
            builder.append(INDENT).append(model.getName()).append(" & ")
                    .append(key).append("\\\\  \n");

            if (i >= MAX_ROW_1ST_PAGE && (i-MAX_ROW_1ST_PAGE) % MAX_ROW_PER_PAGE == 0) {
                tableEnd(builder);
                builder.append("\\pagebreak\n");
                tableBegin(builder);
            }
        }

        tableEnd(builder);

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
        builder.append("\n\\begin{tabular}{ l | c }\n").append(INDENT).append("\\hline\\hline\n")
                // column header
                .append(INDENT).append("Model & Citation \\\\ \n")
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
}
