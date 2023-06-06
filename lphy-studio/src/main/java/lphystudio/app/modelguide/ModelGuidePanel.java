package lphystudio.app.modelguide;

import lphy.core.exception.LoggerUtils;
import lphy.core.model.GeneratorCategory;
import lphystudio.core.swing.SpringUtilities;
import lphystudio.core.swing.TableColumnAdjuster;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

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

        JPanel topPanel = new JPanel(new SpringLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

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
        jLabel.setToolTipText("The group of generative distributions or functions " +
                "play the same or similar roles in the Bayesian phylogenetic frame.");
        JComboBox<GeneratorCategory> cateDropList = new JComboBox<>(GeneratorCategory.values());
        cateDropList.setRenderer(new BasicComboBoxRenderer() {
            public Component getListCellRendererComponent(JList list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                    if (-1 < index) // tooltip
                        list.setToolTipText(Arrays.stream(GeneratorCategory.values()).toList().get(index).getDescription());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setFont(list.getFont());
                if (value instanceof Icon)
                    setIcon((Icon)value);
                else
                    setText((value == null) ? "" : value.toString());
                return this;
            }
        });
//        cateDropList.setMaximumRowCount(10);
        // set to ALL
//        cateDropList.setSelectedIndex(GeneratorCategory.values().length-1);
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
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        dataTable.getSelectionModel().addListSelectionListener(e -> {
            int i = dataTable.getSelectedRow();
            if (i >= 0)
                SwingUtilities.invokeLater(() -> textPane.setText(modelGuide.getModel(i).htmlDoc));
        });
        JScrollPane scrollPane = new JScrollPane(dataTable);

        cateDropList.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSame = modelGuide.setSelectedModels(cateDropList.getSelectedItem());
                if (!isSame) textPane.setText("");
                dataTableModel.fireTableDataChanged();
                if (dataTable.getRowCount() > 0)
                    dataTable.setRowSelectionInterval(0,0);
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
        textPane.setBorder(BorderFactory.createEmptyBorder(1, 6, 6, 10));
        JScrollPane scrollPane2 = new JScrollPane(textPane);

        // after textPane
        if (dataTable.getRowCount() > 0)
            dataTable.setRowSelectionInterval(0,0);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, scrollPane2);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

    }

}
