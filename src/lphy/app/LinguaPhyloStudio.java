package lphy.app;

import lphy.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphy.app.graphicalmodelcomponent.LayeredGNode;
import lphy.core.LPhyParser;
import lphy.core.commands.Remove;
import lphy.graphicalModel.Command;
import lphy.graphicalModel.Utils;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.code.CanonicalCodeBuilder;
import lphy.graphicalModel.code.CodeBuilder;
import lphy.parser.REPL;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

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

        //JMenuItem saveModelToHTML = new JMenuItem("Save Model to HTML...");
        //Menu.add(saveModelToHTML);

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
                if (files[i].getName().endsWith(".lphy")) {
                    final File exampleFile = files[i];
                    JMenuItem exampleItem = new JMenuItem(name.substring(0, name.length() - 5));
                    exampleMenu.add(exampleItem);
                    exampleItem.addActionListener(e -> {
                        BufferedReader reader;
                        try {
                            reader = new BufferedReader(new FileReader(exampleFile));
                            parser.clear();
                            panel.treeLog.clear();
                            panel.variableLog.clear();
                            panel.dataInterpreter.clear();
                            panel.modelInterpreter.clear();
                            panel.source(reader);
                            setFileName(name);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    });
                }
            }
        }

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

        saveTreeLogAsMenuItem.addActionListener(e -> saveToFile(panel.treeLog.getText()));
        saveLogAsMenuItem.addActionListener(e -> saveToFile(panel.variableLog.getText()));

        //saveModelToHTML.addActionListener(e -> exportModelToHTML());
        saveModelToRTF.addActionListener(e -> exportToRtf());
    }

    private void setFileName(String name) {
        frame.setTitle(APP_NAME + " version " + VERSION +
                (name != null ? " - "  + name : ""));
    }

    private void exportToRtf() {
        JTextPane textPane = panel.canonicalModelPanel.pane;


        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = textPane.getDocument();
        RTFEditorKit kit = new RTFEditorKit();
        try {
            kit.write(baos, doc, doc.getStartPosition().getOffset(), doc.getLength());
            baos.close();


            String rtfContent = baos.toString();
            {
                // replace "Monospaced" by a well-known monospace font
                rtfContent = rtfContent.replaceAll("monospaced", "Courier New");
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

        JTextPane pane = panel.canonicalModelPanel.pane;

        if (pane.getDocument().getLength() > 0) {

            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);

            int option = chooser.showSaveDialog(frame);

            if (option == JFileChooser.APPROVE_OPTION) {

                StyledDocument doc = (StyledDocument) pane.getDocument();

                HTMLEditorKit kit = new HTMLEditorKit();

                BufferedOutputStream out;

                try {
                    out = new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile().getAbsoluteFile()));

                    kit.write(out, doc, doc.getStartPosition().getOffset(), doc.getLength());

                } catch (FileNotFoundException e) {

                } catch (IOException e) {

                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private GraphicalLPhyParser createParser() {

        GraphicalLPhyParser parser = new GraphicalLPhyParser(new REPL());

        parser.addCommand(new SampleCommand(this));
        parser.addCommand(new Remove(parser));

        Command quitCommand = new Command() {
            @Override
            public String getName() {
                return "quit";
            }

            public void execute(Map<String, Value<?>> params) {
                quit();
            }
        };

        parser.addCommand(quitCommand);

        return parser;

    }

    private void quit() {
        if (frame != null) frame.dispose();
        System.exit(0);
    }

    private void saveToFile(String text) {
        JFileChooser jfc = new JFileChooser();

        File chooserFile = new File(System.getProperty("user.dir"));

        jfc.setCurrentDirectory(chooserFile);
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new FileWriter(selectedFile));
                writer.write(text);
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            lastDirectory = selectedFile.getParentFile();
        }
    }

    public static void main(String[] args) {

        LinguaPhyloStudio app = new LinguaPhyloStudio();
    }
}
