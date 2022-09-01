package lphystudio.app;

import lphy.core.GraphicalLPhyParser;
import lphy.graphicalModel.code.CanonicalCodeBuilder;
import lphy.graphicalModel.code.CodeBuilder;
import lphy.util.LoggerUtils;
import lphystudio.app.alignmentcomponent.AlignmentComponent;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelpanel.GraphicalModelPanel;
import lphystudio.app.manager.DependencyUtils;
import lphystudio.app.narrative.HTMLNarrative;
import lphystudio.core.awt.AboutMenuHelper;
import lphystudio.core.layeredgraph.LayeredGNode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class LinguaPhyloStudio {
    static final String LPHY_FILE_EXT = ".lphy";
    private static final String APP_NAME = "LPhy Studio";
    static {
        LPhyAppConfig.setupEcoSys(APP_NAME);
    }

    private final int MASK = LPhyAppConfig.MASK;
    private final String VERSION;

    GraphicalLPhyParser parser = Utils.createParser();
    GraphicalModelPanel panel;
    JFrame frame;

    public LinguaPhyloStudio() {
        // use MANIFEST.MF to store version in jar, or use system property in development,
        // otherwise VERSION = "DEVELOPMENT"
        VERSION = DependencyUtils.getVersion(LinguaPhyloStudio.class, "lphy.studio.version");

        panel = new GraphicalModelPanel(parser);

        JMenuBar menuBar;
        JMenu fileMenu;

        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem openMenuItem = new JMenuItem("Open Script...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MASK));
        fileMenu.add(openMenuItem);
        openMenuItem.addActionListener(e -> {
            FileNameExtensionFilter filter = new FileNameExtensionFilter("LPhy scripts", "lphy");
            File selectedFile = Utils.getFileFromFileChooser(frame, filter, JFileChooser.FILES_ONLY, true);

            if (selectedFile != null) {
//                panel.readScript(selectedFile);
                Path dir = selectedFile.toPath().getParent();
                readFile(selectedFile.getName(), dir.toString());
            }
        });

        buildSaveMenu(fileMenu);
        fileMenu.addSeparator();

        JMenuItem exportGraphvizMenuItem = new JMenuItem("Export to Graphviz DOT file...");
        exportGraphvizMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK));
        fileMenu.add(exportGraphvizMenuItem);
        exportGraphvizMenuItem.addActionListener(e -> Utils.saveToFile(
                lphy.graphicalModel.Utils.toGraphvizDot(new ArrayList<>(parser.getModelSinks()), parser), frame));

        JMenuItem exportTikzMenuItem = new JMenuItem("Export to TikZ file...");
        exportTikzMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, MASK));
        fileMenu.add(exportTikzMenuItem);
        exportTikzMenuItem.addActionListener(e -> Utils.saveToFile(panel.getComponent().toTikz(), frame));

        //Build the example's menu.
        JMenu exampleMenu = new JMenu("Examples");
        exampleMenu.setMnemonic(KeyEvent.VK_X);
        fileMenu.addSeparator();
        fileMenu.add(exampleMenu);
        listAllFiles(exampleMenu);

        //Build the tutorial's menu.
        JMenu tutMenu = new JMenu("Tutorials");
        tutMenu.setMnemonic(KeyEvent.VK_U);
//        fileMenu.addSeparator();
        fileMenu.add(tutMenu);
        listAllFiles(tutMenu);

        buildViewMenu(menuBar);
        menuBar.add(panel.getRightPane().getMenu());

        // Tools
        JMenu toolsMenu = new JMenu("Tools");
        menuBar.add(toolsMenu);
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

        AboutMenuHelper aboutMenuHelper =
                new AboutMenuHelper(frame, APP_NAME + " v " + VERSION,
                        getHTMLCredits(), menuBar);

//TODO            desktop.setPreferencesHandler(e ->
//                    JOptionPane.showMessageDialog(frame, "Preferences dialog")
//            );
//            desktop.setQuitHandler((e,r) -> {
//                        JOptionPane.showMessageDialog(frame, "Quit dialog");
//                        System.exit(0);
//                    }
//            );


        // main frame
        frame = new JFrame(APP_NAME + " version " + VERSION);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        final int MAX_WIDTH = 1600;
        final int MAX_HEIGHT = 1200;
        LPhyAppConfig.setFrameLocation(frame, MAX_WIDTH, MAX_HEIGHT);

        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
//        System.out.println("LPhy studio working directory = " + Utils.getUserDir());
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
        final String EXMP = "examples";
        final String TUTL = "tutorials";

        File dir = null;
        if (jMenu.getText().equalsIgnoreCase(EXMP)) {
            // relative path not working
            dir = new File(EXMP).getAbsoluteFile();
        } else if (jMenu.getText().equalsIgnoreCase(TUTL)) {
            dir = new File(TUTL).getAbsoluteFile();
        }
        LoggerUtils.log.info("Menu " + jMenu.getText() + " links to dir = " + dir);

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

    private void buildSaveMenu(JMenu fileMenu) {
        JMenuItem saveAsMenuItem = new JMenuItem("Save Canonical Script to File...");
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK));
        fileMenu.add(saveAsMenuItem);
        CodeBuilder codeBuilder = new CanonicalCodeBuilder();
        saveAsMenuItem.addActionListener(e -> Utils.saveToFile(codeBuilder.getCode(parser), frame));

        JMenuItem saveLogAsMenuItem = new JMenuItem("Save VariableLog to File...");
        fileMenu.add(saveLogAsMenuItem);
        saveLogAsMenuItem.addActionListener(e -> Utils.saveToFile(
                panel.getRightPane().getVariableLog().getText(), frame));

        JMenuItem saveTreeLogAsMenuItem = new JMenuItem("Save Tree VariableLog to File...");
        fileMenu.add(saveTreeLogAsMenuItem);
        saveTreeLogAsMenuItem.addActionListener(e -> Utils.saveToFile(
                panel.getRightPane().getTreeLog().getText(), frame));

        JMenuItem saveModelToHTML = new JMenuItem("Save Model to HTML...");
        fileMenu.add(saveModelToHTML);
        saveModelToHTML.addActionListener(e -> exportModelToHTML());

        JMenuItem saveModelToRTF = new JMenuItem("Save Canonical Model to RTF...");
        fileMenu.add(saveModelToRTF);
        saveModelToRTF.addActionListener(e -> exportToRtf());
    }

    private void buildViewMenu(JMenuBar menuBar) {
        GraphicalModelComponent component = panel.getComponent();
        //Build the second menu.
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);

        JCheckBoxMenuItem showArgumentLabels = new JCheckBoxMenuItem("Show Argument Names");
        showArgumentLabels.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, MASK));
        showArgumentLabels.setState(GraphicalModelComponent.getShowArgumentLabels());
        showArgumentLabels.addActionListener(
                e -> component.setShowArgumentLabels(showArgumentLabels.getState()));
        viewMenu.add(showArgumentLabels);

        JCheckBoxMenuItem showSampledValues = new JCheckBoxMenuItem("Show Sampled Values");
        showSampledValues.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, MASK));
        showSampledValues.setState(LayeredGNode.getShowValueInNode());
        showSampledValues.addActionListener(
                e -> component.setShowValueInNode(showSampledValues.getState()));
        viewMenu.add(showSampledValues);

        JCheckBoxMenuItem useStraightEdges = new JCheckBoxMenuItem("Use Straight Edges");
        useStraightEdges.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, MASK));
        useStraightEdges.setState(GraphicalModelComponent.getUseStraightEdges());
        useStraightEdges.addActionListener(
                e -> component.setUseStraightEdges(useStraightEdges.getState()));
        viewMenu.add(useStraightEdges);

        JCheckBoxMenuItem showTreeInAlignmentView = new JCheckBoxMenuItem("Show tree with alignment if available");
        showTreeInAlignmentView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, MASK));
        showTreeInAlignmentView.setState(true);
        showTreeInAlignmentView.addActionListener(e -> {
            AlignmentComponent.setShowTreeInAlignmentViewerIfAvailable(showTreeInAlignmentView.getState());
            panel.repaint();
        });
        viewMenu.add(showTreeInAlignmentView);

        JCheckBoxMenuItem showErrorsInErrorAlignmentView = new JCheckBoxMenuItem("Show errors in alignment if available");
        showErrorsInErrorAlignmentView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MASK));
        showErrorsInErrorAlignmentView.setState(true);
        showErrorsInErrorAlignmentView.addActionListener(e -> {
            AlignmentComponent.showErrorsIfAvailable = showErrorsInErrorAlignmentView.getState();
            panel.repaint();
        });
        viewMenu.add(showErrorsInErrorAlignmentView);
    }

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

    public void quit() {
        frame.dispose();
    }


    public static void main(String[] args) {

        // use -d to set the working dir, so examples can be loaded properly
        LinguaPhyloStudio app = new LinguaPhyloStudio();

        String dir = null;
        String lphyFileName = null;
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i])) {
                i++;
                // -d examples
                dir = args[i];
            } else // the rest is input file
                lphyFileName = args[i];
        }

        if (lphyFileName != null) {
            if (!lphyFileName.endsWith(".lphy"))
                LoggerUtils.log.severe("Invalid LPhy file name " + lphyFileName + " !");
            else
                app.readFile(lphyFileName, dir);
        }
    }

}
