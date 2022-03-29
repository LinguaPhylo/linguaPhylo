package lphystudio.app;

import lphy.core.GraphicalLPhyParser;
import lphy.graphicalModel.Utils;
import lphy.graphicalModel.code.CanonicalCodeBuilder;
import lphy.graphicalModel.code.CodeBuilder;
import lphy.parser.REPL;
import lphy.util.IOUtils;
import lphy.util.LoggerUtils;
import lphyext.manager.DependencyUtils;
import lphyext.manager.ExtManagerDialog;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelcomponent.LayeredGNode;
import lphystudio.core.narrative.HTMLNarrative;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class LinguaPhyloStudio {

    private static String APP_NAME = "LPhy Studio";
    public static String VERSION;

    static {
        System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "PhyloProb");
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

        JMenuItem saveAsMenuItem = new JMenuItem("Save Canonical Script to File...");
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK));
        fileMenu.add(saveAsMenuItem);

        JMenuItem saveLogAsMenuItem = new JMenuItem("Save VariableLog to File...");
        fileMenu.add(saveLogAsMenuItem);

        JMenuItem saveTreeLogAsMenuItem = new JMenuItem("Save Tree VariableLog to File...");
        fileMenu.add(saveTreeLogAsMenuItem);

        JMenuItem saveModelToHTML = new JMenuItem("Save Model to HTML...");
        fileMenu.add(saveModelToHTML);

        JMenuItem saveModelToRTF = new JMenuItem("Save Canonical Model to RTF...");
        fileMenu.add(saveModelToRTF);

        fileMenu.addSeparator();

        JMenuItem exportGraphvizMenuItem = new JMenuItem("Export to Graphviz DOT file...");
        exportGraphvizMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK));
        fileMenu.add(exportGraphvizMenuItem);
        exportGraphvizMenuItem.addActionListener(e -> lphystudio.app.Utils.saveToFile(Utils.toGraphvizDot(new ArrayList<>(parser.getModelSinks()), parser)));

        JMenuItem exportTikzMenuItem = new JMenuItem("Export to TikZ file...");
        exportTikzMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, MASK));
        fileMenu.add(exportTikzMenuItem);
        exportTikzMenuItem.addActionListener(e -> lphystudio.app.Utils.saveToFile(panel.component.toTikz()));

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
        menuBar.add(panel.rightPane.getMenu());

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

        CodeBuilder codeBuilder = new CanonicalCodeBuilder();

        saveAsMenuItem.addActionListener(e -> lphystudio.app.Utils.saveToFile(codeBuilder.getCode(parser)));

        saveTreeLogAsMenuItem.addActionListener(e -> lphystudio.app.Utils.saveToFile(panel.rightPane.treeLog.getText()));
        saveLogAsMenuItem.addActionListener(e -> lphystudio.app.Utils.saveToFile(panel.rightPane.variableLog.getText()));

        saveModelToHTML.addActionListener(e -> exportModelToHTML());
        saveModelToRTF.addActionListener(e -> exportToRtf());
//        System.out.println("LPhy studio working directory = " + IOUtils.getUserDir());
    }

    private void listAllFiles(JMenu jMenu) {
        final String EXMP = "examples";
        final String TUTL = "tutorials";
//        String wd = System.getProperty(IOUtils.USER_DIR);

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

    /**
     * Load Lphy script from a file,
     * concatenate user.dir in front of the relative path of example file
     * @param lphyFile  LPhy script file, if it is
     * @param dir      if not null, then concatenate to example file path.
     */
    public void readFile(File lphyFile, Path dir) {
        Path filePath = lphyFile.toPath();
        if (dir != null) {
            // must be relative
            if (lphyFile.isAbsolute())
                LoggerUtils.log.warning("LPhy script is an absolute file path, ignoring '-d' ! " + lphyFile);
            else {
                // change user.dir, so that the relative path in LPhy script e.g. 'readNexus' can work
                IOUtils.setUserDir(dir.toAbsolutePath().toString());
                // concatenate user.dir in front of file path
                filePath = Paths.get(dir.toString(), filePath.toString());
            }
        }
        // verify final file path
        if (!filePath.toFile().exists()) {
            LoggerUtils.log.severe("Cannot find the LPhy script : " + filePath +
                    " from the directory " + dir + ", set it using '-d' !");
            return;
        }
        String name = lphyFile.getName();
        LoggerUtils.log.info("Read LPhy script " + name + " from " + filePath);

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath.toFile()));
            parser.clear();
            panel.clear();
            parser.setName(name);
            panel.source(reader);
            setTitle(name);
        } catch (IOException e1) {
            setTitle(null);
            e1.printStackTrace();
        }
    }

    private void buildViewMenu(JMenuBar menuBar) {
        //Build the second menu.
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);

        JCheckBoxMenuItem showArgumentLabels = new JCheckBoxMenuItem("Show Argument Names");
        showArgumentLabels.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, MASK));
        showArgumentLabels.setState(GraphicalModelComponent.getShowArgumentLabels());
        viewMenu.add(showArgumentLabels);

        JCheckBoxMenuItem showSampledValues = new JCheckBoxMenuItem("Show Sampled Values");
        showSampledValues.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, MASK));
        showSampledValues.setState(LayeredGNode.getShowValueInNode());
        viewMenu.add(showSampledValues);

        JCheckBoxMenuItem useStraightEdges = new JCheckBoxMenuItem("Use Straight Edges");
        useStraightEdges.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, MASK));
        useStraightEdges.setState(GraphicalModelComponent.getUseStraightEdges());
        viewMenu.add(useStraightEdges);


        JCheckBoxMenuItem showTreeInAlignmentView = new JCheckBoxMenuItem("Show tree with alignment if available");
        showTreeInAlignmentView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, MASK));
        showTreeInAlignmentView.setState(true);
        viewMenu.add(showTreeInAlignmentView);

        JCheckBoxMenuItem showErrorsInErrorAlignmentView = new JCheckBoxMenuItem("Show errors in alignment if available");
        showErrorsInErrorAlignmentView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MASK));
        showErrorsInErrorAlignmentView.setState(true);
        viewMenu.add(showErrorsInErrorAlignmentView);

        showArgumentLabels.addActionListener(
                e -> panel.component.setShowArgumentLabels(showArgumentLabels.getState()));

        useStraightEdges.addActionListener(
                e -> panel.component.setUseStraightEdges(useStraightEdges.getState()));


        showSampledValues.addActionListener(
                e -> panel.component.setShowValueInNode(showSampledValues.getState()));

        showTreeInAlignmentView.addActionListener(e -> {
            AlignmentComponent.setShowTreeInAlignmentViewerIfAvailable(showTreeInAlignmentView.getState());
            panel.repaint();
        });
        showErrorsInErrorAlignmentView.addActionListener(e -> {
            AlignmentComponent.showErrorsIfAvailable = showErrorsInErrorAlignmentView.getState();
            panel.repaint();
        });

    }

    private void setTitle(String name) {
        frame.setTitle(APP_NAME + " version " + VERSION +
                (name != null ? " - "  + name : ""));
    }

    private void exportToRtf() {
        JTextPane textPane = panel.rightPane.canonicalModelPanel.pane;

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

        JTextPane pane = panel.rightPane.canonicalModelPanel.pane;

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

    public File exportToPNG(String filePath) throws IOException {
        final String imgFormat = "png";
        if (!filePath.endsWith(imgFormat))
            throw new IllegalArgumentException("Expect image format " + imgFormat);

        GraphicalModelComponent gm = panel.component;
        // preference records the previous behaviour
        boolean prevAction = gm.getShowConstantNodes();
        gm.setShowConstantNodes(false);

        BufferedImage img = new BufferedImage(gm.getWidth(), gm.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = img.createGraphics();

        gm.paint(g);
        // back to previous
        if (prevAction) gm.setShowConstantNodes(true);

        File imgF = new File(filePath);
        if (ImageIO.write(img, imgFormat, imgF))
            return imgF;
        else
            throw new IOException("Failed to save graphical model to " + filePath);

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
