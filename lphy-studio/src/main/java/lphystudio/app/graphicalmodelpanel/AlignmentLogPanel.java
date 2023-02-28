package lphystudio.app.graphicalmodelpanel;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * @author Walter Xie
 */
public class AlignmentLogPanel extends JPanel {

    static Preferences preferences = Preferences.userNodeForPackage(AlignmentLogPanel.class);

    final JScrollPane jScrollPane;

    public AlignmentLogPanel(Component view) {
        this.jScrollPane = new JScrollPane(view);

        setLayout(new BorderLayout());
        add(jScrollPane, BorderLayout.CENTER);

//        JTextField dirTextField = new JTextField(getAlignmentDir());
//        dirTextField.setEnabled(isLogAlignment());

//        JButton buttonDir = new JButton("To directory : ");
//        buttonDir.setEnabled(isLogAlignment());
//        buttonDir.setToolTipText("Log alignment file to this directory.");
//        buttonDir.addActionListener(e -> {
//            JFileChooser jfc = new JFileChooser();
//            final String alignmentDir = getAlignmentDir();
//            File lastDirectory = new File(alignmentDir);
//            jfc.setCurrentDirectory(lastDirectory);
//
//            jfc.setMultiSelectionEnabled(false);
//            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//            int returnValue = jfc.showSaveDialog(this);
//
//            if (returnValue == JFileChooser.APPROVE_OPTION) {
//                File selectedFile = jfc.getSelectedFile();
//                if (selectedFile.exists() && selectedFile.isDirectory()) {
//                    dirTextField.setText(selectedFile.getAbsolutePath());
//                    setAlignmentDir(selectedFile.getAbsolutePath());
//                } else
//                    JOptionPane.showMessageDialog(this,
//                            "Must select an existing directory !\n" + selectedFile.getAbsolutePath(),
//                            "Alignment Output Directory", JOptionPane.ERROR_MESSAGE);
//
//            }
//        });

        JCheckBox logAlignmentCheckBox = new JCheckBox("Write alignment(s)");
        logAlignmentCheckBox.setToolTipText("Click 'Sample' button to log alignment when check box is selected.");
        logAlignmentCheckBox.setSelected(isLogAlignment());
        logAlignmentCheckBox.addActionListener(e -> {
            setLogAlignment(logAlignmentCheckBox.isSelected());
//            buttonDir.setEnabled(logAlignmentCheckBox.isSelected());
//            dirTextField.setEnabled(logAlignmentCheckBox.isSelected());
        });


        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BorderLayout());
        optionsPanel.add(logAlignmentCheckBox);
//        GroupLayout layout = new GroupLayout(optionsPanel);
//        optionsPanel.setLayout(layout);
//
//        layout.setAutoCreateGaps(true);
//        layout.setAutoCreateContainerGaps(true);

//        layout.setHorizontalGroup(
//                layout.createSequentialGroup()
//                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                .addComponent(logAlignmentCheckBox)
//                                .addComponent(buttonDir) )
//                        .addComponent(dirTextField)
//        );
//        layout.setVerticalGroup(
//                layout.createSequentialGroup()
//                        .addComponent(logAlignmentCheckBox)
//                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                .addComponent(buttonDir)
//                                .addComponent(dirTextField))
//                        );

        add(optionsPanel, BorderLayout.SOUTH);
    }

    public static boolean isLogAlignment() {
        return preferences.getBoolean("logAlignment", false);
    }

    public static void setLogAlignment(boolean logAlignment) {
        preferences.putBoolean("logAlignment", logAlignment);
    }

//TODO not working
//    public static String getAlignmentDir() {
//        return preferences.get("alignmentDir", System.getProperty("user.dir"));
//    }
//    public static void setAlignmentDir(String dirAbsolutePath) {
//        preferences.put("alignmentDir", dirAbsolutePath);
//    }
}
