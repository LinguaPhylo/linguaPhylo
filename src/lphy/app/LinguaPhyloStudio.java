package lphy.app;

import lphy.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphy.app.graphicalmodelcomponent.LayeredGNode;
import lphy.core.LPhyParser;
import lphy.graphicalModel.Utils;
import lphy.parser.REPL;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class LinguaPhyloStudio {

    private static String APP_NAME = "LinguaPhylo Studio";
    private static String VERSION = "0.01";

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

        JMenuItem saveLogAsMenuItem = new JMenuItem("Save Log to File...");
        fileMenu.add(saveLogAsMenuItem);

        JMenuItem saveTreeLogAsMenuItem = new JMenuItem("Save Tree Log to File...");
        fileMenu.add(saveTreeLogAsMenuItem);

        fileMenu.addSeparator();

        JMenuItem exportGraphvizMenuItem = new JMenuItem("Export to Graphviz DOT file...");
        exportGraphvizMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK));
        fileMenu.add(exportGraphvizMenuItem);
        exportGraphvizMenuItem.addActionListener(e -> saveToFile(Utils.toGraphvizDot(new ArrayList<>(parser.getSinks()))));

        //Build the example menu.
        JMenu exampleMenu = new JMenu("Examples");
        exampleMenu.setMnemonic(KeyEvent.VK_X);
        fileMenu.addSeparator();
        fileMenu.add(exampleMenu);

        File file = new File("examples");
        File[] files = file.listFiles();
        if (files != null) {
            Arrays.sort(files, (o1, o2) -> o1.getName().compareTo(o2.getName()));
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                if (files[i].getName().endsWith(".lphy")) {
                    final File exampleFile = files[i];
                    JMenuItem exampleItem = new JMenuItem(name.substring(0, name.length() - 5));
                    exampleMenu.add(exampleItem);
                    exampleItem.addActionListener(e -> {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new FileReader(exampleFile));
                            parser.clear();
                            source(reader);
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

        saveAsMenuItem.addActionListener(e -> saveToFile(LPhyParser.Utils.getCanonicalScript(parser)));
        showArgumentLabels.addActionListener(
                e -> panel.component.setShowArgumentLabels(showArgumentLabels.getState()));

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
        saveLogAsMenuItem.addActionListener(e -> saveToFile(panel.log.getText()));
    }

    private void source(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            panel.interpreter.interpretInput(line);
            line = reader.readLine();
        }
        panel.repaint();
        reader.close();
    }

    private GraphicalLPhyParser createParser() {

        return new GraphicalLPhyParser(new REPL());

//        Command sampleCommand = new Command() {
//            @Override
//            public String getName() {
//                return "sample";
//            }
//
//            public void execute(Map<String, Value> params) {
//                Value val = params.values().iterator().next();
//                if (val.value() instanceof Integer) {
//                    panel.sample((Integer) val.value());
//                }
//            }
//        };
//
//        Command quitCommand = new Command() {
//            @Override
//            public String getName() {
//                return "quit";
//            }
//
//            public void execute(Map<String, Value> params) {
//                quit();
//            }
//        };

//        parser.addCommand(sampleCommand);
//        parser.addCommand(quitCommand);
//
//        parser.addCommand(new Command() {
//            public String getName() {
//                return "clearlog";
//            }
//
//            public void execute(Map<String, Value> params) {
//                panel.log.clear();
//            }
//        });
//
//        parser.addCommand(new Command() {
//            public String getName() {
//                return "cleartrees";
//            }
//
//            public void execute(Map<String, Value> params) {
//                panel.treeLog.clear();
//            }
//        });
//
//        parser.addCommand(new Command() {
//            public String getName() {
//                return "savelog";
//            }
//
//            public void execute(Map<String, Value> params) {
//                saveToFile(panel.log.getText());
//            }
//        });
//
//        parser.addCommand(new Command() {
//            public String getName() {
//                return "savetrees";
//            }
//
//            public void execute(Map<String, Value> params) {
//                saveToFile(panel.treeLog.getText());
//            }
//        });
//
//        parser.addCommand(new Command() {
//            public String getName() {
//                return "source";
//            }
//
//            public void execute(Map<String, Value> params) {
//
//                Value fileName = params.get("0");
//                if (fileName == null) {
//                    fileName = params.get("file");
//                    if (fileName == null) {
//                        error("source command requires a file name argument (optional name 'file').", this);
//                    }
//                }
//
//                if (fileName != null) {
//
//                    if (fileName.value() instanceof String) {
//                        try {
//                            FileReader fileReader = new FileReader(new File((String)fileName.value()));
//                            BufferedReader reader = new BufferedReader(fileReader);
//                            source(reader);
//                        } catch (FileNotFoundException e) {
//                            error("File node found: " + fileName.value().toString(), this);
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            error("I/O exception will reading file: " + fileName.value().toString(), this);
//                            e.printStackTrace();
//                        }
//                    } else {
//                        error("Argument to source command must be a string!", this);
//                    }
//                }
//            }
//        });
    }

    private void quit() {
        if (frame != null) frame.dispose();
        System.exit(0);
    }

    private void saveToFile(String text) {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
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
