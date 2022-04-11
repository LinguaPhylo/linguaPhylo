package lphystudio.app.graphicalmodelpanel;

import lphy.core.GraphicalLPhyParser;
import lphy.core.LPhyParser;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.GeneratorInfo;
import lphystudio.core.swing.BoundsPopupMenuListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NewRandomVariablePanel extends JPanel {

    // the name of the new random variable
    JTextField name;

    JLabel sim = new JLabel("~");

    JComboBox<String> distributionNameComboBox;
    JComboBox<String> parameterizationComboBox;

    Map<String,List<Class<GenerativeDistribution>>> distributionClasses = new TreeMap<>();

    GraphicalModelInterpreter interpreter;

    GeneratorPanel generatorPanel;

    JButton button = new JButton("Add to Model");

    public NewRandomVariablePanel(GraphicalModelInterpreter interpreter, List<Class<GenerativeDistribution>> distributionClasses) {

        setLayout(new FlowLayout(FlowLayout.LEFT));

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
        name.setFont(GraphicalModelInterpreter.interpreterFont);
        name.setForeground(Color.green.darker());

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

        Vector<String> names = new Vector<>();
        for (int i = 0; i < distributionClasses.size(); i++) {
            Class<GenerativeDistribution> c = distributionClasses.get(i);

            String name = generatorName(c);

            if (!names.contains(name)) {
                names.add(name);
            }

            List<Class<GenerativeDistribution>> list = this.distributionClasses.computeIfAbsent(name, k -> new ArrayList<>());
            list.add(c);
        }
        names.sort(Comparator.naturalOrder());

        BoundsPopupMenuListener boundsPopupMenuListener = new BoundsPopupMenuListener(true, false);
        distributionNameComboBox = new JComboBox<>(names);
        distributionNameComboBox.addPopupMenuListener(boundsPopupMenuListener);
        distributionNameComboBox.setPrototypeDisplayValue((String) distributionNameComboBox.getSelectedItem());
        distributionNameComboBox.setFont(GraphicalModelInterpreter.interpreterFont.deriveFont(Font.BOLD));
        distributionNameComboBox.setForeground(Color.blue);

        distributionNameComboBox.addActionListener(e -> {
            distributionNameComboBox.setPrototypeDisplayValue((String) distributionNameComboBox.getSelectedItem());
            generateComponents();
        });

        generatorPanel = new GeneratorPanel(interpreter.parser);

        button.addActionListener(e -> {
            interpreter.interpretInput(getCodeString(), LPhyParser.Context.model);
            name.setText(randomVarName(interpreter.parser));
        });

        button.setEnabled(true);

        add(name);
        add(sim);
        add(distributionNameComboBox);

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
    }

    private String generatorName(Class c) {
        GeneratorInfo generatorInfo = Generator.getGeneratorInfo(c);

        if (generatorInfo != null) {
           return generatorInfo.name();
        } else {
            return c.getSimpleName();
        }
    }

    public Class<GenerativeDistribution> getSelectedClass() {
        List<Class<GenerativeDistribution>> list = distributionClasses.get(distributionNameComboBox.getSelectedItem());
        if (list.size() == 1) return list.get(0);
        return list.get(parameterizationComboBox.getSelectedIndex());
    }

    public List<Class<GenerativeDistribution>> getSelectedClasses() {
        return distributionClasses.get(distributionNameComboBox.getSelectedItem());
    }

    String getCodeString() {
        StringBuilder builder = new StringBuilder();

        Class genClass = getSelectedClass();

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

        add(name);

        add(sim);
        add(distributionNameComboBox);

        List<Class<GenerativeDistribution>> classes = getSelectedClasses();
        if (classes.size() > 1) {
            Vector<String> parameterizations = IntStream.range(0, classes.size()).mapToObj(i -> "#" + (i + 1)).collect(Collectors.toCollection(Vector::new));
            parameterizationComboBox = new JComboBox<>(parameterizations);
            parameterizationComboBox.setFont(GraphicalModelInterpreter.smallInterpreterFont);
            add(parameterizationComboBox);
            parameterizationComboBox.addActionListener(e -> {
                generatorPanel.setGeneratorClass(getSelectedClass());
                revalidate();
            });
        }

        add(new JLabel("("));

        generatorPanel.setGeneratorClass(getSelectedClass());

        add(generatorPanel);
        add(new JLabel(");"));

        add(button);
    }
}
