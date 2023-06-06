package lphystudio.app;

import lphy.base.math.RandomUtils;
import lphy.base.system.UserDir;
import lphy.core.exception.LoggerUtils;
import lphy.core.model.Value;
import lphy.core.parser.GraphicalLPhyParser;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelpanel.GraphicalModelPanel;
import lphystudio.app.manager.DependencyUtils;
import lphystudio.core.awt.AboutMenuHelper;
import lphystudio.core.awt.PreferencesHelper;
import lphystudio.core.codebuilder.CanonicalCodeBuilder;
import lphystudio.core.editor.UndoManagerHelper;
import lphystudio.core.log.AlignmentLog;
import lphystudio.core.narrative.HTMLNarrative;

import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

public class LinguaPhyloStudio {
    static final String LPHY_FILE_EXT = ".lphy";
    private static final String APP_NAME = "LPhy Studio";
    private static final String LPHY_ICON = "lphy512x512.png";
    static {
        LPhyAppConfig.setupEcoSys(APP_NAME);

        // set icon for mac os
        // https://stackoverflow.com/questions/6006173/how-do-you-change-the-dock-icon-of-a-java-program
        if (Desktop.isDesktopSupported() && Taskbar.isTaskbarSupported()) {
            final Taskbar taskbar = Taskbar.getTaskbar();
            try {
                BufferedImage ioc = LPhyAppConfig.getIcon(LPHY_ICON);
                if (ioc != null)
                    taskbar.setIconImage(ioc);
            } catch (final UnsupportedOperationException e) {
                LoggerUtils.log.warning("The os does not support: 'taskbar.setIconImage'");
            } catch (final SecurityException e) {
                LoggerUtils.log.severe("There was a security exception for: 'taskbar.setIconImage'");
            }
        }
        // or java -Xdock:icon=/path/lphy.png LinguaPhyloStudio
    }

    private final int MASK = LPhyAppConfig.MASK;
    private final String VERSION;

    static Preferences preferences = Preferences.userNodeForPackage(LinguaPhyloStudio.class);
    private final String PRINT_PREVIEW = "Show Print Preview";
    private boolean showPrintPreview = preferences.getBoolean(PRINT_PREVIEW, true);

    GraphicalLPhyParser parser = Utils.createParser();
    GraphicalModelPanel panel;
    JFrame frame;

    protected UndoManagerHelper undoManagerHelper = new UndoManagerHelper();

    public LinguaPhyloStudio() {
        this(0);
    }

    public LinguaPhyloStudio(int frameLocationOffset) {

        // use MANIFEST.MF to store version in jar, or use system property in development,
        // otherwise VERSION = "DEVELOPMENT"
        VERSION = DependencyUtils.getVersion(LinguaPhyloStudio.class, "lphy.studio.version");
        panel = new GraphicalModelPanel(parser, undoManagerHelper);

        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();

        // 1. Build File menu.
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.setDisplayedMnemonicIndex(1);
        menuBar.add(fileMenu);
        buildFileMenu(fileMenu);

        // 2. Build Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(editMenu);
        buildEditMenu(editMenu);

        // 3. Build View menu
        // mv to PreferencesHelper
//        JMenu prefMenu = new JMenu("Preferences");
//        prefMenu.setMnemonic(KeyEvent.VK_R);
//        menuBar.add(prefMenu);
//        buildPreferenceMenu(prefMenu, panel.getComponent());

        // 4. Viewer menu
        JMenu viewMenu = panel.getRightPane().getMenu();
        viewMenu.setMnemonic(KeyEvent.VK_W);
        menuBar.add(viewMenu);

        // 5. Tools
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(toolsMenu);
        buildToolsMenu(toolsMenu);

        AboutMenuHelper aboutMenuHelper =
                new AboutMenuHelper(frame, APP_NAME + " v " + VERSION,
                        getHTMLCredits(), menuBar);

        PreferencesHelper preferencesHelper = new PreferencesHelper(frame, panel, editMenu);

//            desktop.setQuitHandler((e,r) -> {
//                        JOptionPane.showMessageDialog(frame, "Quit dialog");
//                        System.exit(0);
//                    }
//            );


        // main frame
        frame = new JFrame(APP_NAME + " version " + VERSION);
        Image img = LPhyAppConfig.getIcon(LPHY_ICON);
        if (img != null)
            frame.setIconImage(img);

        // Hide and dispose of the window when the user closes it.
        // It will not close all new frames.
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JToolBar toolbar = createToolbar(preferencesHelper);
        panel.setToolbar(toolbar);
        frame.getContentPane().add(toolbar, BorderLayout.NORTH);

        if (GraphicalModelComponent.getShowToolbar()) {
            toolbar.setVisible(true);
        } else {
            toolbar.setVisible(false);
        }

        frame.getContentPane().add(panel, BorderLayout.CENTER);

        final int MAX_WIDTH = 1600;
        final int MAX_HEIGHT = 1200;
        LPhyAppConfig.setFrameLocation(frame, MAX_WIDTH, MAX_HEIGHT, frameLocationOffset);

        frame.setJMenuBar(menuBar);
//        System.out.println("LPhy studio working directory = " + Utils.getUserDir());
        frame.setVisible(true);
    }

    private JToolBar createToolbar(PreferencesHelper preferencesHelper){
        JToolBar toolbar = new JToolBar();
        // TODO add clean?
        JButton toolButt = createToolbarButton("New");
        toolbar.add(toolButt);
        toolButt.setToolTipText("Create new LPhy studio");
        toolButt.addActionListener(e -> new LinguaPhyloStudio(50));
        toolButt = createToolbarButton("Open");
        toolButt.setToolTipText("Open a LPhy script file");
        toolbar.add(toolButt);
        toolButt.addActionListener(e -> openLPhyFile(frame));
        toolButt = createToolbarButton("Cut");
        toolButt.setToolTipText("Cut the selected input in the console");
        toolbar.add(toolButt);
        toolButt.addActionListener(new DefaultEditorKit.CutAction());
        toolButt = createToolbarButton("Copy");
        toolButt.setToolTipText("Copy the selected input in the console");
        toolbar.add(toolButt);
        toolButt.addActionListener(new DefaultEditorKit.CopyAction());
        toolButt = createToolbarButton("Paste");
        toolButt.setToolTipText("Paste the content in the console");
        toolbar.add(toolButt);
        toolButt.addActionListener(new DefaultEditorKit.PasteAction());
        toolButt = createToolbarButton("Undo");
        toolButt.setToolTipText("Undo the typing in the console");
        toolbar.add(toolButt);
        toolButt.addActionListener(undoManagerHelper.undoAction);
        toolButt = createToolbarButton("Redo");
        toolButt.setToolTipText("Redo the typing in the console");
        toolbar.add(toolButt);
        toolButt.addActionListener(undoManagerHelper.redoAction);
        toolButt = createToolbarButton("Preferences");
        toolButt.setToolTipText("Preferences to show the probabilistic graphical model");
        toolbar.add(toolButt);
        toolButt.addActionListener(e -> preferencesHelper.showPrefDialog(frame, panel));
//        toolButt = createToolbarButton("View");
//        toolButt.setToolTipText("Open/close the set of views for the parameters or simulations");
//        toolbar.add(toolButt);
//        toolButt.addActionListener(e -> {
//            // Code to execute when button is clicked
//        });
        return toolbar;
    }

    private static JButton createToolbarButton(String name) {
        JButton toolButt = new JButton();
        BufferedImage icon = LPhyAppConfig.getIcon(name + "24.gif");
        if (icon != null)                     //image found
            toolButt.setIcon(new ImageIcon(icon, name));
        else                                     //no image found
            toolButt.setText(name);
        return toolButt;
    }

    private void buildToolsMenu(JMenu toolsMenu) {
        // extension manager
        JMenuItem toolMenuItem = new JMenuItem(ExtManagerApp.APP_NAME);
        toolMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, MASK));
        toolsMenu.add(toolMenuItem);
        toolMenuItem.addActionListener(e -> {
            new ExtManagerApp();
        });
        // model guide
        toolMenuItem = new JMenuItem(ModelGuideApp.APP_NAME);
        toolMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, MASK));
        toolsMenu.add(toolMenuItem);
        toolMenuItem.addActionListener(e -> {
            new ModelGuideApp();
        });
    }

    private void buildFileMenu(JMenu fileMenu) {
        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MASK));
        fileMenu.add(newMenuItem);
        newMenuItem.addActionListener(e -> {
            LinguaPhyloStudio app = new LinguaPhyloStudio(50);
        });

        JMenuItem openMenuItem = new JMenuItem("Open Script...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MASK));
        fileMenu.add(openMenuItem);
        openMenuItem.addActionListener(e -> {
            openLPhyFile(frame);
        });

        fileMenu.addSeparator();

        //Build the example's menu.
        JMenu exampleMenu = new JMenu("Example Scripts");
        fileMenu.add(exampleMenu);
        listAllFiles(exampleMenu);
        //Build the tutorial's menu.
        JMenu tutMenu = new JMenu("Tutorial Scripts");
        fileMenu.add(tutMenu);
        listAllFiles(tutMenu);

        fileMenu.addSeparator();

        // Save ...
        JMenuItem saveAsMenuItem = new JMenuItem("Save Canonical Script to File...");
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK));
        fileMenu.add(saveAsMenuItem);
        CanonicalCodeBuilder codeBuilder = new CanonicalCodeBuilder();
        saveAsMenuItem.addActionListener(e -> Utils.saveToFile(codeBuilder.getCode(parser), frame));

        JMenuItem saveModelToHTML = new JMenuItem("Save Model to HTML...");
        fileMenu.add(saveModelToHTML);
        saveModelToHTML.addActionListener(e -> exportModelToHTML());

        JMenuItem saveModelToRTF = new JMenuItem("Save Canonical Model to RTF...");
        fileMenu.add(saveModelToRTF);
        saveModelToRTF.addActionListener(e -> exportToRtf());

        JMenuItem saveLogAsMenuItem = new JMenuItem("Save VariableLog to File...");
        fileMenu.add(saveLogAsMenuItem);
        saveLogAsMenuItem.addActionListener(e -> Utils.saveToFile(
                panel.getRightPane().getVariableLog().getText(), frame));

        JMenuItem saveTreeLogAsMenuItem = new JMenuItem("Save Tree VariableLog to File...");
        fileMenu.add(saveTreeLogAsMenuItem);
        saveTreeLogAsMenuItem.addActionListener(e -> Utils.saveToFile(
                panel.getRightPane().getTreeLog().getText(), frame));

        JMenuItem saveAlignmentAsMenuItem = new JMenuItem("Save Alignments to Directory...");
        fileMenu.add(saveAlignmentAsMenuItem);
        saveAlignmentAsMenuItem.addActionListener(e -> {
            File selectedDir = Utils.getFileFromFileChooser(frame, null, JFileChooser.DIRECTORIES_ONLY, false);
            // set alignment directory
            if (selectedDir != null && selectedDir.isDirectory() && panel.getSampler() != null && panel.getSampler().getValuesMap() != null) {
                Path dir = selectedDir.toPath();
                UserDir.setAlignmentDir(dir.toString());
                LoggerUtils.log.info("Alignments saved to: " + dir);
                // save all sampled alignments
                Map<Integer, List<Value<?>>> valuesMap = panel.getSampler().getValuesMap();
                AlignmentLog alignmentLogger = new AlignmentLog(parser);
                alignmentLogger.setLogAlignment(true);
                for (int i: valuesMap.keySet()) {
                    alignmentLogger.log(i, valuesMap.get(i));
                }

            } else {
                throw new IllegalArgumentException("Should select directory rather than file for saving alignments.");
            }
        });

//        JCheckBoxMenuItem saveAlignments = new JCheckBoxMenuItem("Save Alignments");
//        saveAlignments.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, MASK));
//        saveAlignments.setState(GraphicalModelComponent.getSaveAlignments());
//        saveAlignments.addActionListener(
//                e -> component.setSaveAlignments(saveAlignments.getState()));
//        viewMenu.add(saveAlignments);
//        viewMenu.addSeparator();
        fileMenu.addSeparator();

        JMenuItem exportGraphvizMenuItem = new JMenuItem("Export to Graphviz DOT file...");
//        exportGraphvizMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK));
        exportGraphvizMenuItem.setMnemonic(KeyEvent.VK_G);
        fileMenu.add(exportGraphvizMenuItem);
        exportGraphvizMenuItem.addActionListener(e -> Utils.saveToFile(
                GraphvizDotUtils.toGraphvizDot(new ArrayList<>(parser.getModelSinks()), parser), frame));

        JMenuItem exportTikzMenuItem = new JMenuItem("Export to TikZ file...");
//        exportTikzMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, MASK));
        exportTikzMenuItem.setMnemonic(KeyEvent.VK_K);
        fileMenu.add(exportTikzMenuItem);
        exportTikzMenuItem.addActionListener(e -> Utils.saveToFile(panel.getComponent().toTikz(), frame));

        fileMenu.addSeparator();

        JMenuItem printMenu = new JMenuItem("Print Script ...");
        fileMenu.add(printMenu);
        printMenu.addActionListener(e -> printLPhyScript());

        JCheckBoxMenuItem printPreview = new JCheckBoxMenuItem(PRINT_PREVIEW);
        printPreview.setState(showPrintPreview);
        printPreview.addActionListener(e -> {
            showPrintPreview = printPreview.getState();
            preferences.putBoolean(PRINT_PREVIEW, showPrintPreview);
        });
        fileMenu.add(printPreview);

    }

    private void openLPhyFile(Component parent) {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("LPhy scripts", "lphy");
        File selectedFile = Utils.getFileFromFileChooser(parent, filter, JFileChooser.FILES_ONLY, true);

        if (selectedFile != null) {
//                panel.readScript(selectedFile);
            Path dir = selectedFile.toPath().getParent();
            readFile(selectedFile.getName(), dir.toString());
        }
    }

    /**
     * Load Lphy script from a file,
     * concatenate user.dir in front of the relative path of example file
     * @param lphyFileName  LPhy script file, if it is
     * @param dir      if not null, then concatenate to example file path.
     */
    private void readFile(String lphyFileName, String dir) {
        try {
            Utils.readFileFromDir(lphyFileName, dir, panel);
            setTitle(lphyFileName);
        } catch (IOException e) {
            setTitle(null);
            LoggerUtils.logStackTrace(e);
        }
    }

    private void setTitle(String name) {
        frame.setTitle(APP_NAME + " version " + VERSION +
                (name != null ? " - "  + name : ""));
    }

    private void listAllFiles(JMenu jMenu) {
        final String EXMP_FOLDER = "examples";
        final String TUTL_FOLDER = "tutorials";

        File dir = null;
        // check which is selected in menu
        if (jMenu.getText().toLowerCase().contains("example")) {
            // relative path not working
            dir = new File(EXMP_FOLDER).getAbsoluteFile();
        } else if (jMenu.getText().toLowerCase().contains("tutorial")) {
            dir = new File(TUTL_FOLDER).getAbsoluteFile();
        }
        LoggerUtils.log.config("Menu " + jMenu.getText() + " links to dir = " + dir);

        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            LoggerUtils.log.warning("Cannot locate dir : " + dir + " !");
        } else {
            // dir is either examples or tutorials folder
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                // change user.dir, so that the relative path in LPhy script e.g. 'readNexus' can work
//                UserDir.setUserDir(dir.toString());
                Arrays.sort(files, Comparator.comparing(File::getName));
                for (final File file : files) {
                    JMenuItem menuItem = createMenuItemForLPhyScript(file, dir);
                    if (menuItem != null) {
                        // add file name stem to JMenuItem
                        jMenu.add(menuItem);
                    } else if (file.isDirectory()) {
                        // allow 1-level sub-folder to organise LPhy scripts
                        File[] subfolderFiles = file.listFiles();
                        if (subfolderFiles != null && subfolderFiles.length > 0 &&
                                Arrays.stream(subfolderFiles).anyMatch(f -> f.getName().endsWith(LPHY_FILE_EXT))) {
                            // create menu if subfolder contains *.lphy
                            JMenu dirMenu = new JMenu(file.getName());
                            Arrays.sort(subfolderFiles, Comparator.comparing(File::getName));
                            for (final File subfFile : subfolderFiles) {
                                // loop through scripts in the subfolder, which is "file" here
                                Path parent = Paths.get(dir.getPath(), file.getName());
                                menuItem = createMenuItemForLPhyScript(subfFile, parent.toFile());
                                if (menuItem != null)
                                    dirMenu.add(menuItem);
                            }
                            jMenu.add(dirMenu);
                        }
                    }
                }
            }
        }
    }

    private void printLPhyScript() {
        JTextPane pane = panel.getCanonicalModelPane();

        if (pane.getDocument().getLength() > 0) {
            HTMLNarrative htmlNarrative = new HTMLNarrative();
            String html = htmlNarrative.codeBlock(parser, 11);

            try{
                File tempFile = File.createTempFile("tmp-lphy-", ".html");
                FileWriter fileWriter = new FileWriter(tempFile, true);
                System.out.println(tempFile.getAbsolutePath());
                BufferedWriter bw = new BufferedWriter(fileWriter);
                bw.write(html);
                bw.close();

                pane.setPage(tempFile.toURI().toURL());
                tempFile.deleteOnExit();
            }catch (IOException e){
                LoggerUtils.log.severe("I/O ERROR to print !");
                LoggerUtils.logStackTrace(e);
            }

            if (showPrintPreview) {
                final JDialog dialog = new JDialog(frame, "Print Preview");
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.add(pane, BorderLayout.CENTER);
                dialog.pack();
                dialog.setVisible(true);
            }

            try{
                pane.print(null, null, true,
                        PrintServiceLookup.lookupDefaultPrintService(), null, false);
            } catch (PrinterException e){
                LoggerUtils.log.severe("Cannot print !");
                LoggerUtils.logStackTrace(e);
            }
        } else
            JOptionPane.showMessageDialog(frame, "Nothing to print !", "Printing",
                    JOptionPane.ERROR_MESSAGE);

    }

    // add file (exclude dir) name stem to JMenuItem if it ends with ".lphy"
    // give the rest path of its parent folder after root project,
    // e.g. examples/birth-death/
    private JMenuItem createMenuItemForLPhyScript(final File file, File parentDir) {
        String name = file.getName();
        JMenuItem menuItem = null;
        if (name.endsWith(LPHY_FILE_EXT)) {
            // add file name stem to JMenuItem
            menuItem = new JMenuItem(name.substring(0, name.length() - 5));
            menuItem.addActionListener(e -> {
                // equal to cmd: -d parent_folder fn
                readFile(name, parentDir.getPath());
            });
        }
        return menuItem;
    }

    private void buildEditMenu(JMenu editMenu) {

//        Action cutAction = new DefaultEditorKit.CutAction();
//        cutAction.putValue(Action.NAME, "Cut");
        JMenuItem cutMenu = new JMenuItem("Cut");
        cutMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, MASK));
        cutMenu.addActionListener(new DefaultEditorKit.CutAction());
        editMenu.add(cutMenu);

        JMenuItem copyMenu = new JMenuItem("Copy");
        copyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, MASK));
        copyMenu.addActionListener(new DefaultEditorKit.CopyAction());
        editMenu.add(copyMenu);

        JMenuItem pasteMenu = new JMenuItem("Paste");
        pasteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, MASK));
        pasteMenu.addActionListener(new DefaultEditorKit.PasteAction());
        editMenu.add(pasteMenu);

        JMenuItem undoMenu = new JMenuItem("Undo");
        undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MASK));
        undoMenu.addActionListener(undoManagerHelper.undoAction);
        editMenu.add(undoMenu);

        //CTRL/COMMAND + SHIFT
        int modifiers = MASK + KeyEvent.SHIFT_DOWN_MASK;
        KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifiers);
        JMenuItem redoMenu = new JMenuItem("Redo");
        redoMenu.setAccelerator(redoKeyStroke);
        redoMenu.addActionListener(undoManagerHelper.redoAction);
        editMenu.add(redoMenu);

    }

    /*private void buildPreferenceMenu(JMenu prefMenu, GraphicalModelComponent component) {
        //CTRL/COMMAND + SHIFT
        int modifiers = MASK + KeyEvent.SHIFT_DOWN_MASK;

        JCheckBoxMenuItem showArgumentLabels = new JCheckBoxMenuItem("Show Argument Names");
        showArgumentLabels.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, modifiers));
        showArgumentLabels.setState(GraphicalModelComponent.getShowArgumentLabels());
        showArgumentLabels.addActionListener(
                e -> component.setShowArgumentLabels(showArgumentLabels.getState()));
        prefMenu.add(showArgumentLabels);

        JCheckBoxMenuItem showSampledValues = new JCheckBoxMenuItem("Show Sampled Values");
        showSampledValues.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, modifiers));
        showSampledValues.setState(LayeredGNode.getShowValueInNode());
        showSampledValues.addActionListener(
                e -> component.setShowValueInNode(showSampledValues.getState()));
        prefMenu.add(showSampledValues);

        JCheckBoxMenuItem useStraightEdges = new JCheckBoxMenuItem("Use Straight Edges");
        useStraightEdges.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, modifiers));
        useStraightEdges.setState(GraphicalModelComponent.getUseStraightEdges());
        useStraightEdges.addActionListener(
                e -> component.setUseStraightEdges(useStraightEdges.getState()));
        prefMenu.add(useStraightEdges);

        JCheckBoxMenuItem showTreeInAlignmentView = new JCheckBoxMenuItem("Show tree with alignment if available");
        showTreeInAlignmentView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, modifiers));
        showTreeInAlignmentView.setState(true);
        showTreeInAlignmentView.addActionListener(e -> {
            AlignmentComponent.setShowTreeInAlignmentViewerIfAvailable(showTreeInAlignmentView.getState());
            panel.repaint();
        });
        prefMenu.add(showTreeInAlignmentView);

        JCheckBoxMenuItem showErrorsInErrorAlignmentView = new JCheckBoxMenuItem("Show errors in alignment if available");
        showErrorsInErrorAlignmentView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, modifiers));
        showErrorsInErrorAlignmentView.setState(true);
        showErrorsInErrorAlignmentView.addActionListener(e -> {
            AlignmentComponent.showErrorsIfAvailable = showErrorsInErrorAlignmentView.getState();
            panel.repaint();
        });
        prefMenu.add(showErrorsInErrorAlignmentView);
    }*/

    private String getHTMLCredits() {
        return "<html><body width='%1s'><h3>LPhy developers:<br>"+
                "Alexei J. Drummond, Walter Xie, Kylie Chen & Fábio K. Mendes</h3>"+
                "<p>The Centre for Computational Evolution<br>"+
                "University of Auckland<br>"+
                "alexei@cs.auckland.ac.nz</p>"+
                "<p>Downloads & Source code:<br>"+
                "<a href=\""+LPhyAppConfig.LPHY_SOURCE+"\">"+LPhyAppConfig.LPHY_SOURCE+"</a></p>"+
                "<p>User manual, Tutorials & Developer note:<br>"+
                "<a href=\""+LPhyAppConfig.LPHY_WEB+"\">"+LPhyAppConfig.LPHY_WEB+"</a></p>"+
                "<p>Source code distributed under the GNU Lesser General Public License Version 3</p>"+
                "<p>Require Java 17, current Java version " + System.getProperty("java.version") + "</p></html>";
    }

    private void exportToRtf() {
        JTextPane textPane = panel.getCanonicalModelPane();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = textPane.getDocument();
        RTFEditorKit kit = new RTFEditorKit();
        try {
            kit.write(baos, doc, doc.getStartPosition().getOffset(), doc.getLength());
            baos.close();

            String rtfContent = baos.toString();
            {
                // replace "Monospaced" by a well-known monospace font
                rtfContent = rtfContent.replaceAll(Font.MONOSPACED, "Courier New");
                final StringBuffer rtfContentBuffer = new StringBuffer(rtfContent);
                final int endProlog = rtfContentBuffer.indexOf("\n\n");
                // set a good Line Space and no Space Before or Space After each paragraph
                rtfContentBuffer.insert(endProlog, "\n\\sl240");
                rtfContentBuffer.insert(endProlog, "\n\\sb0\\sa0");
                rtfContent = rtfContentBuffer.toString();
            }

            System.out.println(rtfContent);

            if (rtfContent.length() > 0) {
                Utils.saveToFile(rtfContent,frame);
            }

        } catch (IOException | BadLocationException e) {
            LoggerUtils.logStackTrace(e);
        }
    }

    private void exportModelToHTML() {
        JTextPane pane = panel.getCanonicalModelPane();

        if (pane.getDocument().getLength() > 0) {
            HTMLNarrative htmlNarrative = new HTMLNarrative();
            String html = htmlNarrative.codeBlock(parser, 11);

            Utils.saveToFile(html, frame);
        }
    }

//    public void quit() {
//        frame.dispose();
//    }


    public static void main(String[] args) {

        // use -d to set the working dir, so examples can be loaded properly
        LinguaPhyloStudio app = new LinguaPhyloStudio();

        String dir = null;
        String lphyFileName = null;
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i].trim())) {
                i++;
                // -d examples
                dir = args[i];
            } else if ("-seed".equals(args[i].trim())) {
                i++;
                // -seed 777
                long seed = Long.parseLong(args[i]);
                RandomUtils.setSeed(seed);
            } else if (!args[i].trim().isEmpty()) {
                // Installer adds unnecessary spaces
                lphyFileName = args[i];
            } // the rest is invalid
        }

        if (lphyFileName != null) {
            if (!lphyFileName.endsWith(".lphy"))
                LoggerUtils.log.severe("Invalid LPhy file name " + lphyFileName + " !");
            else
                app.readFile(lphyFileName, dir);
        }
    }

}
