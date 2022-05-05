package lphystudio.app.modelguide;

import lphy.graphicalModel.Citation;
import lphy.graphicalModel.NarrativeUtils;

import javax.swing.*;
import java.util.List;

/**
 * @author Walter Xie
 */
public class LatexPane extends JTextPane {

    final ModelGuide modelGuide;

    public LatexPane(ModelGuide modelGuide) {
        this.modelGuide = modelGuide;

        setText(modelGuide.getSelectedModels());

        setEditable(false);
        setAutoscrolls(true);
    }

    public void setText(List<Model> selectedModels) {
        setText(createLatexTable(selectedModels));
    }

    private String createLatexTable(List<Model> models) {
        StringBuilder builder = new StringBuilder("\\documentclass{article}\n")
                .append("\\begin{document}");

        builder.append("\\begin{tabular}{ l | c }\n").append("    \\hline\n");

        String key;
        for (Model model : models) {
            Citation citation = model.getCitation();
            if (citation == null) key = "";
            else key = NarrativeUtils.getCitationKey(citation, "et. al.");
            builder.append("    ").append(model.getName()).append(" & ")
                    .append(key).append("\\\\  \n");
        }

        builder.append("    \\hline\n").append("\\end{tabular}\n\n");

        builder.append("\\end{document}");
        return builder.toString();
    }
}
