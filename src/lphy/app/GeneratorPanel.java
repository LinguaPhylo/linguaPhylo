package lphy.app;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Argument;
import lphy.graphicalModel.Generator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GeneratorPanel extends JPanel {

    java.util.List<JLabel> labels = new ArrayList<>();
    List<JComponent> editors = new ArrayList<>();
    GroupLayout layout = new GroupLayout(this);

    LPhyParser parser = null;

    Class<? extends Generator> generatorClass = null;

    public GeneratorPanel(LPhyParser parser) {
        this.parser = parser;
    }

    public void setGeneratorClass(Class<? extends Generator> generatorClass) {
        this.generatorClass = generatorClass;
        generateComponents();
    }

    void generateComponents() {

        labels.clear();
        editors.clear();
        removeAll();

        for (Argument argument : Generator.getArguments(generatorClass, 0)) {
            JLabel label = new JLabel(argument.name + ":");
            label.setForeground(Color.gray);
            labels.add(label);

            ArgumentInput argumentInput = new ArgumentInput(argument, parser);
            editors.add(argumentInput);
        }

        GroupLayout.ParallelGroup horizParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        GroupLayout.ParallelGroup horizParallelGroup2 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vertSequentialGroup = layout.createSequentialGroup();
        for (
                int i = 0; i < labels.size(); i++) {
            horizParallelGroup.addComponent(labels.get(i));
            horizParallelGroup2.addComponent(editors.get(i));
            GroupLayout.ParallelGroup vertParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
            vertParallelGroup.addComponent(labels.get(i));
            vertParallelGroup.addComponent(editors.get(i));
            vertSequentialGroup.addGroup(vertParallelGroup);
            vertSequentialGroup.addGap(2);
        }

        GroupLayout.SequentialGroup horizSequentialGroup = layout.createSequentialGroup();
        horizSequentialGroup.addGroup(horizParallelGroup).

                addGap(5).

                addGroup(horizParallelGroup2);

        layout.setHorizontalGroup(horizSequentialGroup);

        layout.setVerticalGroup(vertSequentialGroup);
    }
}
