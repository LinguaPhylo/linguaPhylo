package lphystudio.app.modelguide;

import lphy.graphicalModel.GeneratorCategory;
import lphy.util.LoggerUtils;
import lphystudio.core.swing.SpringUtilities;
import lphystudio.core.swing.TableColumnAdjuster;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Table + TextPane
 * @author Walter Xie
 */
public class ModelGuidePanel extends JPanel {

    final ModelGuide modelGuide;
    final JTextPane textPane = new JTextPane();

    public ModelGuidePanel(ModelGuide modelGuide) {
        this.modelGuide = modelGuide;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel topPanel = new JPanel(new SpringLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel jLabel;
//        jLabel = new JLabel("Generator type : ", JLabel.TRAILING);
//        JComboBox<String> geneTypeDropList = new JComboBox<>(ModelGuide.geTy);
//        geneTypeDropList.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                geneTypeDropList.getSelectedItem();
//            }
//        });
//        topPanel.add(jLabel);
//        topPanel.add(geneTypeDropList);

        jLabel = new JLabel("Model category : ", JLabel.TRAILING);
        JComboBox<GeneratorCategory> cateDropList = new JComboBox<>(GeneratorCategory.values());
        // set to ALL
        cateDropList.setSelectedIndex(GeneratorCategory.values().length-1);
        topPanel.add(jLabel);
        topPanel.add(cateDropList);

        //Lay out the panel.
        SpringUtilities.makeCompactGrid(topPanel,
                1, 2, //rows, cols
                6, 6,    //initX, initY
                6, 6);     //xPad, yPad

        add(topPanel, BorderLayout.NORTH);

        // data table
        ModelGuideTableModel dataTableModel = new ModelGuideTableModel(modelGuide.getSelectedModels());
        JTable dataTable = new JTable(dataTableModel);

        TableColumnAdjuster tableColumnAdjuster = new TableColumnAdjuster(dataTable);
        // enable auto resize after turn off in TableColumnAdjuster
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        dataTable.getSelectionModel().addListSelectionListener(e -> {
            int i = dataTable.getSelectedRow();
            if (i >= 0) textPane.setText(modelGuide.getModel(i).htmlDoc);
        });
        JScrollPane scrollPane = new JScrollPane(dataTable);

        cateDropList.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSame = modelGuide.setSelectedModels(cateDropList.getSelectedItem());
                dataTableModel.fireTableDataChanged();
                if (!isSame) textPane.setText("");
            }
        });

        // split pane bottom
        textPane.setEditorKit(JTextPane.createEditorKitForContentType("text/html"));
        textPane.setEditable(false);
        textPane.setAutoscrolls(true);
        textPane.addHyperlinkListener(e -> {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if(Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        LoggerUtils.log.severe(ex.toString());
                        ex.printStackTrace();
                    }
                }
            }
        });
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel.setBackground(Color.white);
        jPanel.add(textPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollPane, jPanel);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

    }

}
