package lphystudio.app;

import lphy.core.GraphicalLPhyParser;
import lphy.parser.REPL;
import lphy.util.LoggerUtils;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelpanel.GraphicalModelPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    public static int MIN_FONT_SIZE = 8;
    public static int MAX_FONT_SIZE = 16;

    static File lastDirectory = null;

    public static void saveToFile(String text) {
        JFileChooser jfc = new JFileChooser();

        File chooserFile = new File(System.getProperty("user.dir"));

        if (lastDirectory == null) {
            jfc.setCurrentDirectory(chooserFile);
        } else {
            jfc.setCurrentDirectory(lastDirectory);
        }
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

    /**
     * Load Lphy script from a file, and set user.dir to parameter dir,
     * which makes sure the relative path in LPhy script working.
     * Equal to the command line: -d dir lphyfile.
     * @param lphyFileName  LPhy script file name
     * @param dir      if not null, then concatenate to example file path.
     * @param panel    clear panel and parser, then paint.
     */
    public static void readFileFromDir(String lphyFileName, String dir, GraphicalModelPanel panel) throws IOException {
        File lphyFile = new File(lphyFileName);
        if (dir != null) {
            // must be relative
            if (lphyFile.exists()) {
                LoggerUtils.log.warning("LPhy file path is given, " +
                        "ignoring '-d' ! " + lphyFile);
                readFile(lphyFile, panel);
            } else {
                File fdir = new File(dir);
                if (!fdir.exists())
                    throw new IOException("Dir " + dir + " does not exist !");
                // change user.dir, so that the relative path in LPhy script e.g. 'readNexus' can work
                lphy.util.IOUtils.setUserDir(dir);
                // concatenate user.dir in front of file path
                Path actualPath = Paths.get(dir, lphyFile.toString());
                readFile(actualPath.toFile(), panel);
            }
        } else
            readFile(lphyFile, panel);
    }

    /**
     * Load Lphy script from a file, and paint the GraphicalModelPanel
     * @param lphyFile    LPhy script file
     * @param panel       clear panel and parser, then paint.
     * @throws IOException
     */
    public static void readFile(File lphyFile, GraphicalModelPanel panel) throws IOException {
        // verify final file path
        if (!lphyFile.exists()) {
            LoggerUtils.log.severe("Cannot find the LPhy script : " + lphyFile +
                    " from the directory " + lphyFile.getParent() + ", set it using '-d' !");
            return;
        }

        LoggerUtils.log.info("Read LPhy script " + lphyFile + " from " + lphyFile.getParent());

        BufferedReader reader;
        reader = new BufferedReader(new FileReader(lphyFile));
        panel.clear();
        panel.getParser().setName(lphyFile.getName());
        panel.source(reader);
    }


    public static void exportToPNG(File imgFile, GraphicalModelComponent component) throws IOException,IllegalArgumentException {
        final String imgFormat = "png";
        if (!imgFile.getName().endsWith(imgFormat))
            throw new IllegalArgumentException("Expect image format " + imgFormat);
        final int W = 1600, H = 1200; // TODO setSize
        final int time = 5; // increase resolution 5 times

        // preference records the previous behaviour
        boolean show = component.getShowConstantNodes();
        component.setShowConstantNodes(false);
        component.setSize(W, H);
        // add margin on right to show labels
        BufferedImage img = new BufferedImage(W*time+10, H*time, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        // make sure text in img have the same scale
        g2d.setTransform(AffineTransform.getScaleInstance(time, time));

        component.paint(g2d);
        // back to previous
        if (show) component.setShowConstantNodes(true);

        g2d.dispose();
        boolean succ = ImageIO.write(img, imgFormat, imgFile);
        if (!succ)
            throw new IOException("Failed to save graphical model to " + imgFile);
    }


    public static GraphicalLPhyParser createParser() {

        GraphicalLPhyParser parser = new GraphicalLPhyParser(new REPL());
        return parser;

    }
}
