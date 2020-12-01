package lphy.app;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Argument;
import lphy.graphicalModel.Generator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GeneratorPanel extends JPanel {

    List<GeneratorPanelListener> listeners = new ArrayList<>();

    List<ArgumentInput> argumentInputs = new ArrayList<>();

    LPhyParser parser;

    Class<? extends Generator> generatorClass = null;

    public GeneratorPanel(LPhyParser parser) {
        this.parser = parser;
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setHgap(0);
        setLayout(flowLayout);
    }

    public void setGeneratorClass(Class<? extends Generator> generatorClass) {
        this.generatorClass = generatorClass;
        generateComponents();
    }

    void generateComponents() {

        removeAll();
        argumentInputs.clear();

        int arg = 0;
        for (Argument argument : Generator.getArguments(generatorClass, 0)) {
            JLabel label = new JLabel((arg > 0 ? ", " : "") + argument.name + "=");
            System.out.println(label.getBorder());
            label.setForeground(Color.gray);
            add(label);

            ArgumentInput argumentInput = new ArgumentInput(argument, parser);
            add(argumentInput);
            argumentInputs.add(argumentInput);
            argumentInput.valueComboBox.addItemListener(e -> fireGeneratePanelListeners());
            arg += 1;
        }

        fireGeneratePanelListeners();
    }

    public void addGeneratorPanelListener(GeneratorPanelListener listener) {
        listeners.add(listener);
    }

    private void fireGeneratePanelListeners() {
        for (GeneratorPanelListener listener : listeners) {
            listener.generatorPanelChanged();
        }
    }
}
