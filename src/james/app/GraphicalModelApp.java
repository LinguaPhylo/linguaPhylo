package james.app;

import james.graphicalModel.GraphicalModelNode;
import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

//    static String[] lines = {
//            "α = [5.0,5.0,5.0,5.0];",
//            "α_r = [2.0,4.0,2.0,4.0,2.0,2.0];",
//            "r ~ Dirichlet(concentration=α_r);",
//            "freq ~ Dirichlet(concentration=α);",
//            "L = 50;",
//            "μ = 0.01;",
//            "n = 20;",
//            "M = 3.0;",
//            "S = 1.0;",
//            "Θ ~ LogNormal(meanlog=M, sdlog=S);",
//            "Q = gtr(rates=r, freq=freq);",
//            "ψ ~ Coalescent(n=n, theta=Θ);",
//            "y0 = 0.0;",
//            "σ2 = 0.01;",
//            "ncat = 4;",
//            "shape = 0.75;",
//            "siteRates ~ DiscretizedGamma(shape=shape, ncat=ncat, reps=L);",
//            "D ~ PhyloCTMC(siteRates=siteRates, mu=μ, Q=Q, tree=ψ);",
//            "y ~ PhyloBrownian(diffusionRate=σ2, y0=y0, tree=ψ);"};

//    static String[] lines = {
//            "L = 50;",
//            "μ = 0.01;",
//            "lM = 3.0;",
//            "lS = 1.0;",
//            "alpha = 0.01;",
//            "beta = 0.01;",
//            "lambda ~ LogNormal(meanlog=lM, sdlog=lS);",
//            "Q = binaryCTMC(lambda=lambda);",
//            "mytree = \"((A:1,B:1):1,(C:0.5, D:0.5):1.5):0.0;\";",
//            "ψ = newick(str=mytree);",
//            "ncat = 4;",
//            "shape = 0.75;",
//            "siteRates ~ DiscretizedGamma(shape=shape, ncat=ncat, reps=L);",
//            "S ~ PhyloCTMC(siteRates=siteRates, mu=μ, Q=Q, tree=ψ);",
//            "D ~ ErrorModel(alpha=alpha, beta=beta, alignment=S);"};

    static String[] lines = {
            "L = 50;",
            "μ = 0.01;",
            "lM = 3.0;",
            "lS = 1.0;",
            "alpha = 0.01;",
            "beta = 0.01;",
            "λ ~ LogNormal(meanlog=lM, sdlog=lS);",
            "Q = binaryCTMC(lambda=λ);",
            "birthRate = 10.0;",
            "n = 20;",
            "ψ ~ Yule(birthRate=birthRate, n=n);",
            "ncat = 4;",
            "shape = 0.75;",
            "siteRates ~ DiscretizedGamma(shape=shape, ncat=ncat, reps=L);",
            "S ~ PhyloCTMC(siteRates=siteRates, mu=μ, Q=Q, tree=ψ);",
            "D ~ ErrorModel(alpha=alpha, beta=beta, alignment=S);"};

    GraphicalModelParser parser = new GraphicalModelParser();

    File lastDirectory = null;

    public GraphicalModelApp() {
        JMenuBar menuBar;
        JMenu menu;

        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem openMenuItem = new JMenuItem("Open Script...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MASK));

        menu.add(openMenuItem);

        JMenuItem saveAsMenuItem = new JMenuItem("Save Canonical Script As...");
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK));

        menu.add(saveAsMenuItem);

        menu.addSeparator();

        JMenuItem exportGraphvizMenuItem = new JMenuItem("Export to Graphviz DOT file...");
        exportGraphvizMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, MASK));


        //Build the second menu.
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);

        JCheckBoxMenuItem showArgumentLabels = new JCheckBoxMenuItem("Argument Names");
        showArgumentLabels.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, MASK));
        showArgumentLabels.setState(false);
        viewMenu.add(showArgumentLabels);

        menu.add(exportGraphvizMenuItem);
        exportGraphvizMenuItem.addActionListener(e -> {
            List<GraphicalModelNode> nodes = new ArrayList<>(parser.getRoots());

            String graphvizString = Utils.toGraphvizDot(nodes);

            System.out.println(graphvizString);

            JFileChooser jfc = null;
            if (lastDirectory != null) {
                 jfc = new JFileChooser(lastDirectory);
            } else {
                jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            }
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int returnValue = jfc.showSaveDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(new FileWriter(selectedFile));
                    writer.write(graphvizString);
                    writer.flush();
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                lastDirectory = selectedFile.getParentFile();
            }
        });

        parser.parseLines(lines);

        GraphicalModelPanel panel = new GraphicalModelPanel(parser);
        //panel.setPreferredSize(new Dimension(1200, 800));

        JFrame frame = new JFrame("Phylogenetic Graphical Models");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println(e);
            }
        });
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(dim.width*8/10, dim.height*8/10);
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);


        frame.setJMenuBar(menuBar);
        frame.setVisible(true);

        openMenuItem.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int returnValue = jfc.showOpenDialog(null);
            // int returnValue = jfc.showSaveDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                panel.readScript(selectedFile);
            }
        });

        saveAsMenuItem.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            jfc.setMultiSelectionEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int returnValue = jfc.showSaveDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                writeCanonicalToFile(selectedFile);
            }
        });

        showArgumentLabels.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.component.setShowArgumentLabels(showArgumentLabels.getState());
            }
        });
    }

    private void writeCanonicalToFile(File file) {

    }

    public static void main(String[] args) {

        GraphicalModelApp app = new GraphicalModelApp();
    }
}
