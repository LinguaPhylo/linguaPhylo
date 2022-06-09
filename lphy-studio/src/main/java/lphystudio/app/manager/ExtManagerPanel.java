package lphystudio.app.manager;

import lphystudio.core.swing.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main panel
 */
public class ExtManagerPanel extends JPanel {

    final ExtManager manager;

    public ExtManagerPanel(ExtManager manager) {
        this.manager = manager;

        final List<Extension> extList = manager.getLoadedLPhyExts();
        String dirStr = manager.getJarDirStr();
        if (dirStr.trim().isEmpty())
            System.err.println("Warning: no directory is found to store lphy extensions " + dirStr);

        // main components
        JPanel topPanel = new JPanel(new SpringLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        topPanel.add(new JLabel("Extensions are in directory : "));
        JTextField repo = new JTextField(dirStr);
        repo.setEditable(false);
        topPanel.add(repo);

        SpringUtilities.makeCompactGrid(topPanel,
                1, 2, //rows, cols
                6, 6,    //initX, initY
                6, 6);     //xPad, yPad

        // data table
        ExtManagerTableModel extManagerTableModel = new ExtManagerTableModel(extList);
        JTable dataTable = new JTable(extManagerTableModel);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane scrollPane = new JScrollPane(dataTable);
//        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, scrollPane);
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

//TODO installation ?

    }

}
