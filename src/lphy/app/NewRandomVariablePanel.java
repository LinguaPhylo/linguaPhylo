package lphy.app;

import lphy.core.lightweight.Argument;
import lphy.core.lightweight.LGenerator;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.GeneratorInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class NewRandomVariablePanel extends JPanel {

    // the name of the new random variable
    JTextField name;

    JComboBox<String> generativeDistributionCombo;
    List<Class<? extends LGenerator>> distributionClasses;

    GraphicalLPhyParser parser;

    List<JLabel> labels = new ArrayList<>();
    List<JComponent> editors = new ArrayList<>();
    GroupLayout layout = new GroupLayout(this);

    public NewRandomVariablePanel(GraphicalLPhyParser parser, List<Class<? extends LGenerator>> distributionClasses) {

        name = new JTextField(10);

        this.distributionClasses = distributionClasses;

        String[] names = new String[distributionClasses.size()];
        for (int i = 0; i < distributionClasses.size(); i++) {
            Class<? extends LGenerator> c = distributionClasses.get(i);

            GeneratorInfo generatorInfo = Generator.getGeneratorInfo(c);

            if (generatorInfo != null) {
                names[i] = generatorInfo.name();
            } else {
                names[i] = c.getSimpleName();
            }
        }
        generativeDistributionCombo = new JComboBox<>(names);

        labels.add(new JLabel("Variable Name"));
        editors.add(name);

        labels.add(new JLabel("Distribution"));
        editors.add(generativeDistributionCombo);

        setLayout(layout);

        generateComponents();

        generativeDistributionCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateComponents();
            }
        });
    }

    void generateComponents() {

        labels.clear();
        editors.clear();
        removeAll();

        labels.add(new JLabel("Variable Name"));
        editors.add(name);

        labels.add(new JLabel("Distribution"));
        editors.add(generativeDistributionCombo);


        for (Argument argument : LGenerator.getArguments(distributionClasses.get(generativeDistributionCombo.getSelectedIndex()), 0)) {
            JLabel label = new JLabel(argument.name + ":");
            label.setForeground(Color.gray);
            labels.add(label);

            JTextField textField = new JTextField(10);
            editors.add(textField);
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
