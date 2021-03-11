package lphy.app;

import lphy.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphy.app.graphicalmodelcomponent.LayeredGNode;
import lphy.core.LPhyParser;
import lphy.core.commands.Remove;
import lphy.core.narrative.HTMLNarrative;
import lphy.graphicalModel.Command;
import lphy.graphicalModel.Utils;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.code.CanonicalCodeBuilder;
import lphy.graphicalModel.code.CodeBuilder;
import lphy.parser.REPL;
import lphy.parser.codecolorizer.DataModelToHTML;
import lphy.utils.LoggerUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static lphy.app.Utils.saveToFile;

public class LinguaPhyloStudio {

    private static String APP_NAME = "LPhy Studio";
    private static String VERSION = "0.1";

    static {
        System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "PhyloProb");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private static final int MAX_WIDTH = 1600;
    private static final int MAX_HEIGHT = 1200;


    GraphicalLPhyParser parser = createParser();
    GraphicalModelPanel panel = null;
    JFrame frame;

    File lastDirectory = null;

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
        exportGraphvizMenuItem.addActionListener(e -> saveToFile(Utils.toGraphvizDot(new ArrayList<>(parser.getModelSinks()), parser)));

        JMenuItem exportTikzMenuItem = new JMenuItem("Export to TikZ file...");
        exportTikzMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, MASK));
        fileMenu.add(exportTikzMenuItem);
        exportTikzMenuItem.addActionListener(e -> saveToFile(panel.component.toTikz()));

        //Build the example menu.
        JMenu exampleMenu = new JMenu("Examples");
        exampleMenu.setMnemonic(KeyEvent.VK_X);
        fileMenu.addSeparator();
        fileMenu.add(exampleMenu);

        File file = new File("examples");
        File[] files = file.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                if (name.endsWith(".lphy")) {
                    final File exampleFile = files[i];
                    JMenuItem exampleItem = new JMenuItem(name.substring(0, name.length() - 5));
                    exampleMenu.add(exampleItem);
                    exampleItem.addActionListener(e -> {
                        readFile(exampleFile);
                    });
                }
            }
        }

        buildViewMenu(menuBar);
        menuBar.add(panel.rightPane.getMenu());

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
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int returnValue = jfc.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                panel.readScript(selectedFile);
            }
        });

        CodeBuilder codeBuilder = new CanonicalCodeBuilder();

        saveAsMenuItem.addActionListener(e -> saveToFile(codeBuilder.getCode(parser)));

        saveTreeLogAsMenuItem.addActionListener(e -> saveToFile(panel.rightPane.treeLog.getText()));
        saveLogAsMenuItem.addActionListener(e -> saveToFile(panel.rightPane.variableLog.getText()));

        saveModelToHTML.addActionListener(e -> exportModelToHTML());
        saveModelToRTF.addActionListener(e -> exportToRtf());
    }

    public void readFile(File exampleFile) {
        String name = exampleFile.getName();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(exampleFile));
            parser.clear();
            panel.clear();
            panel.source(reader);
            parser.setName(name);
            setFileName(name);
        } catch (IOException e1) {
            setFileName(null);
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
        showArgumentLabels.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, MASK));
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

    private void setFileName(String name) {
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

        JComponent gm = panel.component;

        BufferedImage img = new BufferedImage(gm.getWidth(), gm.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = img.createGraphics();

        gm.paint(g);

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
