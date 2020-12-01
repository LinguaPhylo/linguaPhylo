package lphy.app;

import lphy.core.LPhyParser;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.GeneratorInfo;
import lphy.swing.BoundsPopupMenuListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class NewRandomVariablePanel extends JPanel {

    // the name of the new random variable
    JTextField name;

    JLabel sim = new JLabel("~");

    JComboBox<String> generativeDistributionCombo;
    List<Class<GenerativeDistribution>> distributionClasses;

    GraphicalModelInterpreter interpreter;

    GeneratorPanel generatorPanel;

    LPhyCodeLabel codeStringLabel;

    JButton button = new JButton("Add to Model");

    public NewRandomVariablePanel(GraphicalModelInterpreter interpreter, List<Class<GenerativeDistribution>> distributionClasses) {

        this.interpreter = interpreter;

        sim.setFont(sim.getFont().deriveFont(Font.BOLD));

        name = new JTextField(randomVarName(interpreter.parser)) {

            @Override
            public boolean isValidateRoot() {
                return false;
            }
        };
        name.setText(randomVarName(interpreter.parser));
        name.setOpaque(false);
        name.setForeground(Color.green.darker());

        codeStringLabel = new LPhyCodeLabel(interpreter.parser, "");

        ((GraphicalLPhyParser)interpreter.parser).addGraphicalModelChangeListener(() -> generateComponents());


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

            int paramCount = c.getConstructors()[0].getParameterCount();

            names[i] = generatorName(c) + " (" +  paramCount + " params)";
        }

        BoundsPopupMenuListener boundsPopupMenuListener = new BoundsPopupMenuListener(true, false);
        generativeDistributionCombo = new JComboBox<>(names);
        generativeDistributionCombo.addPopupMenuListener(boundsPopupMenuListener);
        generativeDistributionCombo.setPrototypeDisplayValue((String)generativeDistributionCombo.getSelectedItem());
        generativeDistributionCombo.setFont(generativeDistributionCombo.getFont().deriveFont(Font.BOLD));
        generativeDistributionCombo.setForeground(Color.blue);

        generativeDistributionCombo.addActionListener(e -> {
            codeStringLabel.setText(getCodeString());
            generativeDistributionCombo.setPrototypeDisplayValue((String)generativeDistributionCombo.getSelectedItem());
            generateComponents();
        });

        generatorPanel = new GeneratorPanel(interpreter.parser);


        button.addActionListener(e -> {
            interpreter.interpretInput(getCodeString(), LPhyParser.Context.model);
            name.setText(randomVarName(interpreter.parser));
        });

        generatorPanel.addGeneratorPanelListener(() -> codeStringLabel.setCodeColorizedText(getCodeString()));

        button.setEnabled(true);

        add(name);
        add(sim);
        add(generativeDistributionCombo);

        generateComponents();
    }

    private static String randomVarName(LPhyParser parser) {
        String randomVarName = "randomVar";
        int i = 0;
        while (parser.hasValue(randomVarName, LPhyParser.Context.model)) {
            randomVarName = "randomVar" + i;
            i += 1;
        }
        return randomVarName;
    }

    private void nameUpdated() {
        button.setEnabled(name.getText().trim().length()>0);
        codeStringLabel.setText(getCodeString());
    }

    private String generatorName(Class c) {
        GeneratorInfo generatorInfo = Generator.getGeneratorInfo(c);

        if (generatorInfo != null) {
           return generatorInfo.name();
        } else {
            return c.getSimpleName();
        }
    }

    String getCodeString() {
        StringBuilder builder = new StringBuilder();

        Class genClass = distributionClasses.get(generativeDistributionCombo.getSelectedIndex());

        builder.append(name.getText()).append(" ~ ").append(generatorName(genClass)).append("(");
        int i = 0;
        for (ArgumentInput input : generatorPanel.argumentInputs) {

            String value = (String)input.valueComboBox.getSelectedItem();

            if (value != null) {
                if (i > 0) builder.append(", ");

                builder.append(input.argument.name).append("=");
                if (interpreter.parser.hasValue(value, LPhyParser.Context.model)) {
                    builder.append(value);
                } else {
                    builder.append(value);
                }
                i += 1;
            }
        }
        builder.append(");");
        return builder.toString();
    }

    void generateComponents() {

        removeAll();
        generatorPanel.setGeneratorClass(distributionClasses.get(generativeDistributionCombo.getSelectedIndex()));

        add(name);
        add(sim);
        add(generativeDistributionCombo);
        add(new JLabel("("));
        add(generatorPanel);
        add(new JLabel(")"));

        add(button);
    }
}
