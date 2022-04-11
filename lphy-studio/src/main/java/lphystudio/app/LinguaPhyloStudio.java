package lphystudio.app;

import lphy.core.GraphicalLPhyParser;
import lphy.graphicalModel.code.CanonicalCodeBuilder;
import lphy.graphicalModel.code.CodeBuilder;
import lphy.parser.REPL;
import lphy.util.IOUtils;
import lphy.util.LoggerUtils;
import lphyext.manager.DependencyUtils;
import lphyext.manager.ExtManagerDialog;
import lphystudio.app.alignmentcomponent.AlignmentComponent;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelpanel.GraphicalModelPanel;
import lphystudio.app.narrative.HTMLNarrative;
import lphystudio.core.layeredgraph.LayeredGNode;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class LinguaPhyloStudio {

    public static String VERSION;

    private static String APP_NAME = "LPhy Studio";
    private static String WEB = "https://linguaphylo.github.io";
    private static String SOURCE = "https://github.com/LinguaPhylo/linguaPhylo";

    static {
        System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        // use MANIFEST.MF to store version in jar, but use system property in development
        VERSION = getVersion();
    }

    private static String getVersion() {
        // in dev, if system property has no "lphy.studio.version", then VERSION = "DEVELOPMENT"
        return DependencyUtils.getVersion(LinguaPhyloStudio.class, "lphy.studio.version");
    }

    private static final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private static final int MAX_WIDTH = 1600;
    private static final int MAX_HEIGHT = 1200;

    GraphicalLPhyParser parser = createParser();
    GraphicalModelPanel panel = null;
    JFrame frame;

    File lastDirectory = null;//TODO Alexei: is it still used?

    public LinguaPhyloStudio() {

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
            // use "user.dir" instead of FileSystemView.getFileSystemView().getHomeDirectory()
            JFileChooser jfc = new JFileChooser(IOUtils.getUserDir().toFile());

            FileNameExtensionFilter filter = new FileNameExtensionFilter("LPhy scripts", "lphy");
            jfc.setFileFilter(filter);
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int returnValue = jfc.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
//                panel.readScript(selectedFile);
                Path dir = selectedFile.toPath().getParent();
                readFile(selectedFile, dir);
            }
        });

        buildSaveMenu(fileMenu);
        fileMenu.addSeparator();

        JMenuItem exportGraphvizMenuItem = new JMenuItem("Export to Graphviz DOT file...");
        exportGraphvizMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK));
        fileMenu.add(exportGraphvizMenuItem);
        exportGraphvizMenuItem.addActionListener(e -> Utils.saveToFile(lphy.graphicalModel.Utils.toGraphvizDot(new ArrayList<>(parser.getModelSinks()), parser)));

        JMenuItem exportTikzMenuItem = new JMenuItem("Export to TikZ file...");
        exportTikzMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, MASK));
        fileMenu.add(exportTikzMenuItem);
        exportTikzMenuItem.addActionListener(e -> Utils.saveToFile(panel.getComponent().toTikz()));

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
        JMenu toolMenu = new JMenu("Tools");
        menuBar.add(toolMenu);
        // extension manager
        JMenuItem extManMenuItem = new JMenuItem("Extension manager");
        extManMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, MASK));
        toolMenu.add(extManMenuItem);
        extManMenuItem.addActionListener(e -> {
            ExtManagerDialog extManager = null;
            try {
                extManager = new ExtManagerDialog(frame);
                extManager.setVisible(true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // deal with About menu
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            desktop.setAboutHandler(e ->
                    buildAboutDialog(frame)
            );
//TODO            desktop.setPreferencesHandler(e ->
//                    JOptionPane.showMessageDialog(frame, "Preferences dialog")
//            );
//            desktop.setQuitHandler((e,r) -> {
//                        JOptionPane.showMessageDialog(frame, "Quit dialog");
//                        System.exit(0);
//                    }
//            );
        } else {
            JMenu helpMenu = new JMenu("Help");
            menuBar.add(helpMenu);
            helpMenu.setMnemonic('H');
            helpMenu.add(new ActionAbout());
        }

        // main frame
        frame = new JFrame(APP_NAME + " version " + VERSION);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        int width = Math.min(MAX_WIDTH, dim.width * 9 / 10);
        int height = Math.min(MAX_HEIGHT, dim.height * 9 / 10);

        frame.setSize(width, height);
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);

        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
//        System.out.println("LPhy studio working directory = " + Utils.getUserDir());
    }

    /**
     * Load Lphy script from a file,
     * concatenate user.dir in front of the relative path of example file
     * @param lphyFile  LPhy script file, if it is
     * @param dir      if not null, then concatenate to example file path.
     */
    public void readFile(File lphyFile, Path dir) {
        try {
            Utils.readFile(lphyFile, dir, panel);
            setTitle(lphyFile.getName());
        } catch (IOException e1) {
            setTitle(null);
            e1.printStackTrace();
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
        System.out.println("Menu " + jMenu.getText() + " refer to dir = " + dir);

        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            LoggerUtils.log.warning("Cannot locate dir : " + dir + " !");
        } else {
            final String postfix = ".lphy";
            File[] files = dir.listFiles();
            if (files != null) {
                // change user.dir, so that the relative path in LPhy script e.g. 'readNexus' can work
                IOUtils.setUserDir(dir.toString());

                Arrays.sort(files, Comparator.comparing(File::getName));
                for (final File file : files) {
                    Path parent = file.getParentFile().toPath();
                    String name = file.getName();
                    File fn = new File(name);
                    if (name.endsWith(postfix)) {
                        JMenuItem menuItem = new JMenuItem(name.substring(0, name.length() - 5));
                        jMenu.add(menuItem);
                        menuItem.addActionListener(e -> {
                            // readFile concatenates fn in front of parent path
                            readFile(fn, parent);
                        });
                    }
                }
            }
        }
    }

    private void buildSaveMenu(JMenu fileMenu) {
        JMenuItem saveAsMenuItem = new JMenuItem("Save Canonical Script to File...");
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK));
        fileMenu.add(saveAsMenuItem);
        CodeBuilder codeBuilder = new CanonicalCodeBuilder();
        saveAsMenuItem.addActionListener(e -> Utils.saveToFile(codeBuilder.getCode(parser)));

        JMenuItem saveLogAsMenuItem = new JMenuItem("Save VariableLog to File...");
        fileMenu.add(saveLogAsMenuItem);
        saveLogAsMenuItem.addActionListener(e -> Utils.saveToFile(panel.getRightPane().getVariableLog().getText()));

        JMenuItem saveTreeLogAsMenuItem = new JMenuItem("Save Tree VariableLog to File...");
        fileMenu.add(saveTreeLogAsMenuItem);
        saveTreeLogAsMenuItem.addActionListener(e -> Utils.saveToFile(panel.getRightPane().getTreeLog().getText()));

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
//        viewMenu.add(showArgumentLabels);//TODO issue 169

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
                "<a href=\""+SOURCE+"\">"+SOURCE+"</a></p>"+
                "<p>User manual, Tutorials & Developer note:<br>"+
                "<a href=\""+WEB+"\">"+WEB+"</a></p>"+
                "<p>Source code distributed under the GNU Lesser General Public License Version 3</p>"+
                "<p>Java version " + System.getProperty("java.version") + "</p></html>";
    }

    private void buildAboutDialog(Component parentComponent) {
        final JTextPane textPane = new JTextPane();
        textPane.setEditorKit(JTextPane.createEditorKitForContentType("text/html"));
        textPane.setText(getHTMLCredits());
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

        JOptionPane.showMessageDialog(parentComponent, textPane,
                APP_NAME + " v " + VERSION, JOptionPane.PLAIN_MESSAGE, null);
    }

    class ActionAbout extends AbstractAction {
        public ActionAbout() {
            super("About", null);
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            buildAboutDialog(frame);
        }
    } // non Mac About

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

                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);

                int option = chooser.showSaveDialog(frame);

                if (option == JFileChooser.APPROVE_OPTION) {

                    BufferedOutputStream out;

                    try {
                        FileWriter writer = new FileWriter(chooser.getSelectedFile().getAbsoluteFile());

                        writer.write(rtfContent);
                        writer.close();

                    } catch (FileNotFoundException e) {

                    } catch (IOException e) {

                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void exportModelToHTML() {

        JTextPane pane = panel.getCanonicalModelPane();

        if (pane.getDocument().getLength() > 0) {

            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);

            int option = chooser.showSaveDialog(frame);

            if (option == JFileChooser.APPROVE_OPTION) {

                HTMLNarrative htmlNarrative = new HTMLNarrative();
                String html = htmlNarrative.codeBlock(parser, 11);

//                if (html.length() > 0) {
                try {
                    FileWriter writer = new FileWriter(chooser.getSelectedFile().getAbsoluteFile());
                    writer.write(html);
                    writer.close();

                } catch (FileNotFoundException e) {

                } catch (IOException e) {

                }
//                }
            }
        }
    }

    private GraphicalLPhyParser createParser() {

        GraphicalLPhyParser parser = new GraphicalLPhyParser(new REPL());
        return parser;

    }

    public void quit() {
        frame.dispose();
    }


    public static void main(String[] args) {

        // use -Duser.dir= to set the working dir, so examples can be loaded properly
        LinguaPhyloStudio app = new LinguaPhyloStudio();

        Path dir = null;
        String lphyFileName = null;
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i])) {
                i++;
                // -d examples
                dir = Path.of(args[i]);
            } else {
                // the rest is input file
                lphyFileName = args[i];
            }
        }

        if (lphyFileName != null) {
            File file = new File(lphyFileName);

            if (!lphyFileName.endsWith(".lphy"))
                LoggerUtils.log.severe("Invalid LPhy file name " + lphyFileName + " !");
            else
                app.readFile(file, dir);
        }
    }

}
