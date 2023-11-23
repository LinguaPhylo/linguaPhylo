package lphystudio.app.graphicalmodelpanel;

import lphy.core.model.Generator;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.argument.Argument;
import lphy.core.parser.argument.ArgumentUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GeneratorPanel extends JPanel {

    List<ArgumentButton> argButtons = new ArrayList<>();
    List<ArgumentInput> argumentInputs = new ArrayList<>();

    LPhyParserDictionary parser;

    Class<? extends Generator> generatorClass = null;

    public GeneratorPanel(LPhyParserDictionary parser) {
        this(parser, null);
    }

    public GeneratorPanel(LPhyParserDictionary parser, Class<? extends Generator> generatorClass) {
        this.parser = parser;
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setHgap(0);
        setLayout(flowLayout);

        this.generatorClass = generatorClass;
        if (generatorClass != null) generateComponents();
    }

    public void setGeneratorClass(Class<? extends Generator> generatorClass) {
        this.generatorClass = generatorClass;
        generateComponents();
    }

    void generateComponents() {

        removeAll();
        argumentInputs.clear();

        int arg = 0;
        for (Argument argument : ArgumentUtils.getArguments(generatorClass, 0)) {
            if (arg > 0) {
                JLabel label = new JLabel(", ");
                label.setForeground(Color.gray);
                add(label);
            }

            ArgumentInput argumentInput = new ArgumentInput(argument, parser);
            ArgumentButton argumentButton = new ArgumentButton(argumentInput);

            add(argumentButton);
            add(argumentInput);
            argumentInputs.add(argumentInput);
            arg += 1;
        }
    }


    class ArgumentButton extends JToggleButton {

        ArgumentInput input = null;

        public ArgumentButton(ArgumentInput input) {
            this.input = input;
            setForeground(Color.gray);
            setSelected(!input.argument.optional);
            input.setVisible(!input.argument.optional);
            setEnabled(input.argument.optional);
            setFont(StudioConsoleInterpreter.smallInterpreterFont);

            updateText();

            this.addItemListener(e -> {
                input.setVisible(isSelected());
                updateText();
            });
        }

        private void updateText() {
            setText(isSelected() ? (input.argument.name + "=") : ("(" + input.argument.name + ")"));
            setBorder(null);
        }
    }

}
