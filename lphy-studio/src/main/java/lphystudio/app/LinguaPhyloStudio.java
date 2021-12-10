package lphystudio.app;

import lphy.core.GraphicalLPhyParser;
import lphy.graphicalModel.Utils;
import lphy.graphicalModel.code.CanonicalCodeBuilder;
import lphy.graphicalModel.code.CodeBuilder;
import lphy.parser.REPL;
import lphy.util.IOUtils;
import lphy.util.LoggerUtils;
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
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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

        //Build the example menu.
        JMenu exampleMenu = new JMenu("Examples");
        exampleMenu.setMnemonic(KeyEvent.VK_X);
        fileMenu.addSeparator();
        fileMenu.add(exampleMenu);
        // relative path not working
        File dir = new File("examples").getAbsoluteFile();
        System.out.println("Examples dir = " + dir);
        listAllFiles(exampleMenu, dir);

        //Build the tutorials menu.
        JMenu tutMenu = new JMenu("Tutorials");
        tutMenu.setMnemonic(KeyEvent.VK_U);
//        fileMenu.addSeparator();
        fileMenu.add(tutMenu);
        dir = new File("tutorials").getAbsoluteFile();
        System.out.println("Tutorials dir = " + dir);
        listAllFiles(tutMenu, dir);

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
                extManager.setTitle("LPhy Extension Manager " + VERSION);
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
                readFile(selectedFile);
            }
        });

        CodeBuilder codeBuilder = new CanonicalCodeBuilder();

        saveAsMenuItem.addActionListener(e -> lphystudio.app.Utils.saveToFile(codeBuilder.getCode(parser)));

        saveTreeLogAsMenuItem.addActionListener(e -> lphystudio.app.Utils.saveToFile(panel.rightPane.treeLog.getText()));
        saveLogAsMenuItem.addActionListener(e -> lphystudio.app.Utils.saveToFile(panel.rightPane.variableLog.getText()));

        saveModelToHTML.addActionListener(e -> exportModelToHTML());
        saveModelToRTF.addActionListener(e -> exportToRtf());

        System.out.println("LPhy studio working directory = " + IOUtils.getUserDir());
    }

    // use MANIFEST.MF to store version in jar, but use system property in development
    private static String getVersion() {
        String version = null;
        // for Java module system
        try {
            Enumeration<URL> resources = LinguaPhyloStudio.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");

            while (resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                Attributes attr = manifest.getMainAttributes();
                String name = attr.getValue("Implementation-Title");
                if ("LPhyStudio".equalsIgnoreCase(name)) {
                    version = attr.getValue("Implementation-Version");
                    break;
                }
            }
        } catch (IOException e) {
            LoggerUtils.log.severe("Cannot find lphy manifest !");
            e.printStackTrace();
        }
        // for class path
        if (version == null)
            version = LinguaPhyloStudio.class.getPackage().getImplementationVersion();
        // for IDE to get version from system property "lphy.studio.version"
        if (version == null)
            version = System.getProperty("lphy.studio.version");
        // should not reach here
        if (version == null)
            version = "DEVELOPMENT";
        return version;
    }

    private void listAllFiles(JMenu exampleMenu, File dir) {
        final String postfix = ".lphy";
        if (!dir.exists()) LoggerUtils.log.warning("Cannot locate dir : " + dir + " !");

        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                if (name.endsWith(postfix)) {
                    final File exampleFile = files[i];
                    JMenuItem exampleItem = new JMenuItem(name.substring(0, name.length() - 5));
                    exampleMenu.add(exampleItem);
                    exampleItem.addActionListener(e -> {
                        readFile(exampleFile);
                    });
                }
            }
        }
    }

    /**
     * Load Lphy script from a file,
     * and set the user.dir to the folder containing this file.
     * @param exampleFile  LPhy script file
     */
    public void readFile(File exampleFile) {
        String name = exampleFile.getName();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(exampleFile));
            // set user.dir to the folder containing example file,
            // so that the relative path given in readNexus always refers to it
            IOUtils.setUserDir(exampleFile.getParent());
            parser.clear();
            parser.setName(name);
            panel.clear();
            panel.source(reader);
            setTitle(name);
        } catch (IOException e1) {
            setTitle(null);
            // set to where LPhy is launched
            IOUtils.setUserDir(Paths.get("").toString());
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
        showArgumentLabels.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, MASK));
        showArgumentLabels.setState(GraphicalModelComponent.getUseStraightEdges());
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

        if (args.length > 0) {
            // always the last arg
            String lphyFileName = args[args.length-1];
            // examples/simpleYule.lphy
            if (!lphyFileName.endsWith(".lphy"))
                LoggerUtils.log.severe("Invalid LPhy file name " + lphyFileName + " !");
            File file = new File(lphyFileName);

            if (file.exists()) {
                app.readFile(file);
            } else
                LoggerUtils.log.severe("Cannot find LPhy file " + lphyFileName + " !");
//                throw new FileNotFoundException("Cannot find LPhy file !");
        }

    }

}
