package lphystudio.app.modelguide;

import lphy.util.LoggerUtils;
import lphystudio.core.swing.TableColumnAdjuster;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author Walter Xie
 */
public class ModelGuidePanel extends JPanel {

    final ModelGuide modelGuide;
    final JTextPane textPane = new JTextPane();

    public ModelGuidePanel(ModelGuide modelGuide) {
        this.modelGuide = modelGuide;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // data table
        DataTableModel dataTableModel = new DataTableModel(modelGuide.getAllModels());
        JTable dataTable = new JTable(dataTableModel);

        TableColumnAdjuster tableColumnAdjuster = new TableColumnAdjuster(dataTable);
        // enable auto resize after turn off in TableColumnAdjuster
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        dataTable.getSelectionModel().addListSelectionListener(e -> {
//            modelGuide.setCurrentModel(modelGuide.getModel(dataTable.getSelectedRow()));
            textPane.setText(modelGuide.getModel(dataTable.getSelectedRow()).htmlDoc);
        });
        JScrollPane scrollPane = new JScrollPane(dataTable);

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

    class DataTableModel extends AbstractTableModel {

        String[] columnNames = {"Name", "Category", "Description"};

        private final List<Model> modelList;

        public DataTableModel(List<Model> extList) {
            this.modelList = extList;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return modelList.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            Model model = modelList.get(row);
            switch (col) {
                case 0:
                    return model.getName();
                case 1:
                    return model.getCategory();
                case 2:
                    String desc = model.getDescription();
                    final int max = 80;
                    if (desc.length() > max) {
                        desc = desc.substring(0, max);
                        desc += " ...";
                    }
                    return desc;
//                case 3:
//                    return model.getDependenciesStr();
//                case 4:
//                    return ext.getWebsite();
//                case 5:
//                    return model.getDesc();
                default:
                    throw new IllegalArgumentException("unknown column, " + col);
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();

            buffer.append(getColumnName(0));
            for (int j = 1; j < getColumnCount(); j++) {
                buffer.append("\t");
                buffer.append(getColumnName(j));
            }
            buffer.append("\n");

            for (int i = 0; i < getRowCount(); i++) {
                buffer.append(getValueAt(i, 0));
                for (int j = 1; j < getColumnCount(); j++) {
                    buffer.append("\t");
                    buffer.append(getValueAt(i, j));
                }
                buffer.append("\n");
            }

            return buffer.toString();
        }
    }

}
