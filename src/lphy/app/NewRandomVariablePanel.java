package lphy.app;

import lphy.core.LPhyParser;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.Value;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.List;

public class NewRandomVariablePanel extends JPanel {

    // the name of the new random variable
    JTextField name;

    JComboBox<String> generativeDistributionCombo;
    List<Class<GenerativeDistribution>> distributionClasses;

    GraphicalLPhyParser parser;

    List<JComponent> labels = new ArrayList<>();
    List<JComponent> editors = new ArrayList<>();
    GroupLayout layout = new GroupLayout(this);

    GeneratorPanel generatorPanel = null;

    JLabel codeStringLabel = new JLabel();

    JButton button = new JButton("Add to Model");

    public NewRandomVariablePanel(GraphicalLPhyParser parser, List<Class<GenerativeDistribution>> distributionClasses) {

        GeneratorPanel panel = new GeneratorPanel(parser);

        name = new JTextField(10);

        name.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                nameUpdated();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                nameUpdated();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                nameUpdated();
            }
        });

        this.distributionClasses = distributionClasses;

        String[] names = new String[distributionClasses.size()];
        for (int i = 0; i < distributionClasses.size(); i++) {
            Class<? extends Generator> c = distributionClasses.get(i);

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

        generatorPanel = new GeneratorPanel(parser);

        setLayout(layout);

        generateComponents();

        generativeDistributionCombo.addActionListener(e -> generateComponents());

        button.addActionListener(e -> parser.parse(getCodeString(), LPhyParser.Context.model));

        generatorPanel.addGeneratorPanelListener(() -> codeStringLabel.setText(getCodeString()));

        button.setEnabled(false);
    }

    private void nameUpdated() {
        button.setEnabled(name.getText().trim().length()>0);
        codeStringLabel.setText(getCodeString());
    }

    String getCodeString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name.getText()).append(" ~ ").append(generativeDistributionCombo.getSelectedItem()).append("(");
        int i = 0;
        for (ArgumentInput input : generatorPanel.argumentInputs) {

            Value value = (Value)input.valueComboBox.getSelectedItem();

            if (value != null) {
                if (i > 0) builder.append(", ");
                builder.append(input.argument.name).append("=").append(value.getCanonicalId());
                i += 1;
            }
        }
        builder.append(")");
        return builder.toString();
    }

    void generateComponents() {

        labels.clear();
        editors.clear();
        removeAll();

        labels.add(new JLabel("Variable Name"));
        editors.add(name);

        labels.add(new JLabel("Distribution"));
        editors.add(generativeDistributionCombo);

        generatorPanel.setGeneratorClass(distributionClasses.get(generativeDistributionCombo.getSelectedIndex()));

        labels.add(new JLabel("Parameters"));
        editors.add(generatorPanel);



        labels.add(button);
        editors.add(codeStringLabel);

        GroupLayout.ParallelGroup horizParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        GroupLayout.ParallelGroup horizParallelGroup2 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vertSequentialGroup = layout.createSequentialGroup();
        for (int i = 0; i < labels.size(); i++) {
            horizParallelGroup.addComponent(labels.get(i));
            horizParallelGroup2.addComponent(editors.get(i));
            GroupLayout.ParallelGroup vertParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
            vertParallelGroup.addComponent(labels.get(i));
            vertParallelGroup.addComponent(editors.get(i));
            vertSequentialGroup.addGroup(vertParallelGroup);
            vertSequentialGroup.addGap(2);
        }

        GroupLayout.SequentialGroup horizSequentialGroup = layout.createSequentialGroup();
        horizSequentialGroup.addGroup(horizParallelGroup).addGap(5).addGroup(horizParallelGroup2);

        layout.setHorizontalGroup(horizSequentialGroup);

        layout.setVerticalGroup(vertSequentialGroup);
    }
}
