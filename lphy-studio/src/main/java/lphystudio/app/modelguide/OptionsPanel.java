package lphystudio.app.modelguide;

import lphy.graphicalModel.GeneratorCategory;
import lphystudio.core.swing.SpringUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Walter Xie
 */
public class OptionsPanel extends JPanel {

    final String[] geTy = new String[]{"ALL","Generative Distribution","Functions"};

    public OptionsPanel() {
        setLayout(new SpringLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel jLabel = new JLabel("Generator type : ", JLabel.TRAILING);
        JComboBox<String> geneTypeDropList = new JComboBox<>(geTy);
        geneTypeDropList.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                geneTypeDropList.getSelectedItem();
            }
        });
        add(jLabel);
        add(geneTypeDropList);

        jLabel = new JLabel("Model category : ", JLabel.TRAILING);
        JComboBox<GeneratorCategory> cateDropList = new JComboBox<>(GeneratorCategory.values());
        // set to ALL
        cateDropList.setSelectedIndex(GeneratorCategory.values().length-1);
        cateDropList.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cateDropList.getSelectedItem();
            }
        });
        add(jLabel);
        add(cateDropList);

        //Lay out the panel.
        SpringUtilities.makeCompactGrid(this,
                2, 2, //rows, cols
                6, 6,    //initX, initY
                6, 6);     //xPad, yPad
    }
}
