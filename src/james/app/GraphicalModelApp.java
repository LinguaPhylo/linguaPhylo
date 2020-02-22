package james.app;

import james.graphicalModel.Command;
import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.Utils;
import james.graphicalModel.Value;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class GraphicalModelApp {

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

    static String[] totalEvidenceExample = {
            "α = [5.0,5.0,5.0,5.0];",
            "α_r = [2.0,4.0,2.0,4.0,2.0,2.0];",
            "r ~ Dirichlet(concentration=α_r);",
            "freq ~ Dirichlet(concentration=α);",
            "L = 50;",
            "μ = 0.01;",
            "n = 20;",
            "M = 3.0;",
            "S = 1.0;",
            "Θ ~ LogNormal(meanlog=M, sdlog=S);",
            "Q = gtr(rates=r, freq=freq);",
            "ψ ~ Coalescent(n=n, theta=Θ);",
            "y0 = 0.0;",
            "σ2 = 0.01;",
            "ncat = 4;",
            "shape = 0.75;",
            "siteRates ~ DiscretizedGamma(shape=shape, ncat=ncat, reps=L);",
            "D ~ PhyloCTMC(siteRates=siteRates, mu=μ, Q=Q, tree=ψ);",
            "y ~ PhyloBrownian(diffusionRate=σ2, y0=y0, tree=ψ);"};

    static String[] errorModel1ExampleCode = {
            "L = 50;",
            "μ = 0.01;",
            "lM = 3.0;",
            "lS = 1.0;",
            "alpha = 0.01;",
            "beta = 0.01;",
            "lambda ~ LogNormal(meanlog=lM, sdlog=lS);",
            "Q = binaryRateMatrix(lambda=lambda);",
            "ψ = newick(\"((A:1,B:1):1,(C:0.5, D:0.5):1.5):0.0;\");",
            "ncat = 4;",
            "shape = 0.75;",
            "siteRates ~ DiscretizedGamma(shape=shape, ncat=ncat, reps=L);",
            "S ~ PhyloCTMC(siteRates=siteRates, mu=μ, Q=Q, tree=ψ);",
            "D ~ ErrorModel(alpha=alpha, beta=beta, alignment=S);"};

    static String[] errorModel2ExampleCode = {
            "λ ~ LogNormal(meanlog=3.0, sdlog=1.0);",
            "ψ ~ Yule(birthRate=10.0, n=100);",
            "Q=binaryRateMatrix(lambda=λ);",
            "S ~ PhyloCTMC(L=1000, Q=Q, tree=ψ);",
            "D ~ ErrorModel(alpha=0.01, beta=0.01, alignment=S);",
//            "sample(1000);",
//            "quit();"
    };

    GraphicalModelParser parser = new GraphicalModelParser();
    GraphicalModelPanel panel = null;
    JFrame frame;

    File lastDirectory = null;

    public GraphicalModelApp() {

        initParser();
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
        exportGraphvizMenuItem.addActionListener(e -> saveToFile(Utils.toGraphvizDot(new ArrayList<>(parser.getRoots()))));

        //Build the example menu.
        JMenu exampleMenu = new JMenu("Examples");
        exampleMenu.setMnemonic(KeyEvent.VK_X);
        fileMenu.addSeparator();
        fileMenu.add(exampleMenu);

        JMenuItem totalEvidenceExampleItem = new JMenuItem("Total Evidence");
        exampleMenu.add(totalEvidenceExampleItem);
        totalEvidenceExampleItem.addActionListener(e -> {
            parser.clear();
            source(totalEvidenceExample);
        });

        JMenuItem errorModelExample = new JMenuItem("Error Model 1");
        exampleMenu.add(errorModelExample);
        errorModelExample.addActionListener(e -> {
            parser.clear();
            source(errorModel1ExampleCode);
        });

        JMenuItem errorModel2Example = new JMenuItem("Error Model 2");
        errorModel2Example.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, MASK));
        exampleMenu.add(errorModel2Example);
        errorModel2Example.addActionListener(e -> {
            parser.clear();
            source(errorModel2ExampleCode);
        });


        //Build the second menu.
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);

        JCheckBoxMenuItem showArgumentLabels = new JCheckBoxMenuItem("Argument Names");
        showArgumentLabels.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, MASK));
        showArgumentLabels.setState(false);
        viewMenu.add(showArgumentLabels);

        JCheckBoxMenuItem showTreeInAlignmentView = new JCheckBoxMenuItem("Show tree with alignment if available");
        showTreeInAlignmentView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, MASK));
        showTreeInAlignmentView.setState(true);
        viewMenu.add(showTreeInAlignmentView);

        JCheckBoxMenuItem showErrorsInErrorAlignmentView = new JCheckBoxMenuItem("Show errors in alignment if available");
        showErrorsInErrorAlignmentView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MASK));
        showErrorsInErrorAlignmentView.setState(true);
        viewMenu.add(showErrorsInErrorAlignmentView);


        frame = new JFrame("Phylogenetic Graphical Models");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(dim.width * 8 / 10, dim.height * 8 / 10);
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

        saveAsMenuItem.addActionListener(e -> saveToFile(parser.getCanonicalScript()));
        showArgumentLabels.addActionListener(e -> panel.component.setShowArgumentLabels(showArgumentLabels.getState()));
        showTreeInAlignmentView.addActionListener(e -> {
            AlignmentComponent.showTreeIfAvailable = showTreeInAlignmentView.getState();
            panel.repaint();
        });
        showErrorsInErrorAlignmentView.addActionListener(e -> {
            AlignmentComponent.showErrorsIfAvailable = showErrorsInErrorAlignmentView.getState();
            panel.repaint();
        });

        saveTreeLogAsMenuItem.addActionListener(e -> saveToFile(panel.treeLog.getText()));
        saveLogAsMenuItem.addActionListener(e -> saveToFile(panel.log.getText()));
    }

    private void source(String[] source) {
        for (int i =0; i < source.length; i++) {
            panel.interpreter.interpretInput(source[i]);
        }
    }

    private void initParser() {

        Command sampleCommand = new Command() {
            @Override
            public String getName() {
                return "sample";
            }

            public void execute(Map<String, Value> params) {
                Value val = params.values().iterator().next();
                if (val.value() instanceof Integer) {
                    panel.sample((Integer) val.value());
                }
            }
        };

        Command quitCommand = new Command() {
            @Override
            public String getName() {
                return "quit";
            }

            public void execute(Map<String, Value> params) {
                quit();
            }
        };

        parser.addCommand(sampleCommand);
        parser.addCommand(quitCommand);

        parser.addCommand(new Command() {
            @Override
            public String getName() {
                return "log.clear";
            }

            public void execute(Map<String, Value> params) {
                panel.log.clear();
            }
        });
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

        GraphicalModelApp app = new GraphicalModelApp();
    }
}
