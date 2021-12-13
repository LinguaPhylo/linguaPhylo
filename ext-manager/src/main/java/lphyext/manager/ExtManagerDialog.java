package lphyext.manager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Run from LPhy studio
 */
public class ExtManagerDialog extends JDialog {

    final ExtManager manager = new ExtManager();

    public ExtManagerDialog(Frame owner) throws IOException {
        super(owner, true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        List<Extension> extList = manager.getLoadedLPhyExts();
        String dirStr = manager.getJarDirStr();
        if (dirStr.trim().isEmpty() || dirStr.contains(";"))
            System.err.println("Warning: no directory or multiple directories " +
                    "found to store lphy extensions " + dirStr);

        // main components
        JPanel repoPathPanel = new JPanel();
        repoPathPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        repoPathPanel.add(new JLabel("Extensions are located in "));
        JTextField repo = new JTextField(dirStr);
        repo.setEditable(false);
        repoPathPanel.add(repo);
        this.getContentPane().add(repoPathPanel, BorderLayout.NORTH);

//        JLabel jLabel = new JLabel("Available extensions for LPhy " +
//                LinguaPhyloStudio.VERSION + " : "); // TODO
//        this.getContentPane().add(jLabel, BorderLayout.NORTH);

        DataTableModel dataTableModel = new DataTableModel(extList);
        JTable dataTable = new JTable(dataTableModel);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane scrollPane = new JScrollPane(dataTable);
//        scrollPane.setPreferredSize(new Dimension(660, 400));
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);

        Box buttonBox = createButtonBox();
        this.getContentPane().add(buttonBox, BorderLayout.SOUTH);
        this.pack();

        // launch from LinguaPhyloStudio
        if ( owner != null ) {
            // size and location
            this.setSize((int) (owner.getWidth() * 0.8), (int) (owner.getHeight() * 0.5));
            this.setLocation(owner.getX() + owner.getWidth() / 4,
                    owner.getY() + owner.getHeight() / 5);
        } else { // launch from ExtManagerApp
            final int MAX_WIDTH = 1600;
            final int MAX_HEIGHT = 1200;
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            int width = Math.min(MAX_WIDTH, dim.width * 7 / 10);
            int height = Math.min(MAX_HEIGHT, dim.height * 5 / 10);

            this.setSize(width, height);
            this.setLocation((dim.width - getSize().width) / 2, (dim.height - getSize().height) / 2);
        }

        this.setTitle("LPhy Extension Manager " +
                DependencyUtils.getVersion(ExtManagerDialog.class, "lphy.ext.manager.version"));

    }

    class DataTableModel extends AbstractTableModel {

        String[] columnNames = {"ID", "GroupID", "Installed", "Latest", "Dependencies", "Description"};
        public final int linkColumn = 4;

        private final List<Extension> extList;

        public DataTableModel(List<Extension> extList) {
            this.extList = extList;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        @Override
        public int getRowCount() {
            return extList.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            Extension ext = extList.get(row);
            switch (col) {
                case 0:
                    return ext.getArtifactId();
                case 1:
                    return ext.getGroupId();
                case 2:
                    return ext.getVersion();
                case 3:
                    return "";
                case 4:
                    return ext.getDependenciesStr();
                case 5:
                    return ext.getWebsite();
                case 6:
                    return ext.getDesc();
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


    private Box createButtonBox() {
        Box box = Box.createHorizontalBox();
        final JCheckBox latestVersionCheckBox = new JCheckBox("Latest");
        latestVersionCheckBox.setToolTipText("If selected, only the latest version is installed when hitting the Install/Upgrade button. "
                + "Otherwise, you can select from a list of available versions.");
        box.add(latestVersionCheckBox);
        latestVersionCheckBox.addActionListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
//            useLatestVersion = checkBox.isSelected();
        });
//        latestVersionCheckBox.setSelected(useLatestVersion);
        JButton installButton = new JButton("Install/Upgrade");
//        installButton.addActionListener(e -> {
//            // first get rid of existing packages
//            int[] selectedRows = dataTable.getSelectedRows();
//            String installedPackageNames = "";
//
//            setCursor(new Cursor(Cursor.WAIT_CURSOR));
//
//            Map<Package, PackageVersion> packagesToInstall = new HashMap<>();
//            PackageManager.useArchive(!useLatestVersion);
//            for (int selRow : selectedRows) {
//                Package selPackage = getSelectedPackage(selRow);
//                if (selPackage != null) {
//                    if (useLatestVersion) {
//                        packagesToInstall.put(selPackage, selPackage.getLatestVersion());
//                    } else {
//                        PackageVersion version = (PackageVersion) JOptionPane.showInputDialog( null, "Select DependencyUtils for " + selPackage.getName(),
//                                "Select version",
//                                JOptionPane.QUESTION_MESSAGE, null,
//                                selPackage.getAvailableVersions().toArray(), selPackage.getAvailableVersions().toArray()[0]);
//                        if (version == null) {
//                            return;
//                        }
//                        packagesToInstall.put(selPackage, version);
//                    }
//                }
//            }
//
//            try {
//                populatePackagesToInstall(packageMap, packagesToInstall);
//
//                prepareForInstall(packagesToInstall, false, null);
//
//                if (getToDeleteListFile().exists()) {
//                    JOptionPane.showMessageDialog(frame,
//                            "<html><body><p style='width: 200px'>Upgrading packages on your machine requires BEAUti " +
//                                    "to restart. Shutting down now.</p></body></html>");
//                    System.exit(0);
//                }
//
//                installPackages(packagesToInstall, false, null);
//
//                // Refresh classes:
//                loadExternalJars();
//
//                installedPackageNames = String.join(",",
//                        packagesToInstall.keySet().stream()
//                                .map(Package::toString)
//                                .collect(Collectors.toList()));
//
//                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//
//            } catch (DependencyResolutionException | IOException ex) {
//                JOptionPane.showMessageDialog(null, "Install failed because: " + ex.getMessage());
//                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//            }
//
//            resetPackages();
//            dataTable.setRowSelectionInterval(selectedRows[0], selectedRows[0]);
//
//            if (installedPackageNames.length()>0)
//                JOptionPane.showMessageDialog(null, "Package(s) "
//                        + installedPackageNames + " installed. "
//                        + "Note that any changes to the BEAUti "
//                        + "interface will\n not appear until a "
//                        + "new document is created or BEAUti is "
//                        + "restarted.");
//        });
        box.add(installButton);

        JButton uninstallButton = new JButton("Uninstall");
//        uninstallButton.addActionListener(e -> {
//            StringBuilder removedPackageNames = new StringBuilder();
//            int[] selectedRows = dataTable.getSelectedRows();
//
//            for (int selRow : selectedRows) {
//                Package selPackage = getSelectedPackage(selRow);
//                if (selPackage != null) {
//                    try {
//                        if (selPackage.isInstalled()) {
//                            setCursor(new Cursor(Cursor.WAIT_CURSOR));
//                            List<String> deps = getInstalledDependencyNames(selPackage, packageMap);
//
//                            if (deps.isEmpty()) {
//                                String result = uninstallPackage(selPackage, selPackage.getInstalledVersion(), false, null);
//
//                                if (result != null) {
//                                    if (removedPackageNames.length() > 0)
//                                        removedPackageNames.append(", ");
//                                    removedPackageNames.append("'")
//                                            .append(selPackage.getName())
//                                            .append(" v")
//                                            .append(selPackage.getInstalledVersion())
//                                            .append("'");
//                                }
//                            } else {
//                                throw new DependencyResolutionException("package " + selPackage
//                                        + " is used by the following packages: "
//                                        + String.join(", ", deps) + "\n"
//                                        + "Remove those packages first.");
//                            }
//
//                            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//                        }
//
//                        resetPackages();
//                        dataTable.setRowSelectionInterval(selectedRows[0], selectedRows[0]);
//                    } catch (IOException | DependencyResolutionException ex) {
//                        JOptionPane.showMessageDialog(null, "Uninstall failed because: " + ex.getMessage());
//                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//                    }
//                }
//            }
//
//            if (getToDeleteListFile().exists()) {
//                JOptionPane.showMessageDialog(frame,
//                        "<html><body><p style='width: 200px'>Removing packages on your machine requires BEAUti " +
//                                "to restart. Shutting down now.</p></body></html>");
//                System.exit(0);
//            }
//
//            if (removedPackageNames.length()>0)
//                JOptionPane.showMessageDialog(null, "Package(s) "
//                        + removedPackageNames.toString() + " removed. "
//                        + "Note that any changes to the BEAUti "
//                        + "interface will\n not appear until a "
//                        + "new document is created or BEAUti is "
//                        + "restarted.");
//        });
        box.add(uninstallButton);

        box.add(Box.createHorizontalGlue());

        JButton packageRepoButton = new JButton("Package repositories");
//        packageRepoButton.addActionListener(e -> {
//            JPackageRepositoryDialog dlg = new JPackageRepositoryDialog(frame);
//            dlg.setVisible(true);
//            resetPackages();
//        });
        box.add(packageRepoButton);

        box.add(Box.createGlue());

//        JButton closeButton = new JButton("Close");
//        closeButton.addActionListener(e -> {
//            if (dlg != null) {
//                dlg.setVisible(false);
//            } else {
//                setVisible(false);
//            }
//        });
//        box.add(closeButton);

        JButton button = new JButton("?");
        button.setToolTipText("help");
//        button.addActionListener(e -> {
//            JOptionPane.showMessageDialog(this,
//                    "<html>By default, packages are installed in <br><br><em>" + getPackageUserDir() +
//                    "</em><br><br>and are available only to you.<br>" +
//                    "<br>Packages can also be moved manually to <br><br><em>" + getPackageSystemDir() +
//                    "</em><br><br>which makes them available to all users<br>"
//                    + "on your system.</html>");
//        });
        box.add(button);
        return box;
    }
}
