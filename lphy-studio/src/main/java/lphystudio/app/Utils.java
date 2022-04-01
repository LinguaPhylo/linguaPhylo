package lphystudio.app;

import lphy.util.LoggerUtils;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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
     * Load Lphy script from a file,
     * concatenate user.dir in front of the relative path of example file
     * @param lphyFile  LPhy script file, if it is
     * @param dir      if not null, then concatenate to example file path.
     * @param panel    clear panel and parser, then paint.
     */
    public static void readFile(File lphyFile, Path dir, GraphicalModelPanel panel) throws IOException {
        Path actualPath = lphyFile.toPath();
        if (dir != null) {
            // must be relative
            if (lphyFile.isAbsolute())
                LoggerUtils.log.warning("LPhy script is an absolute file path, " +
                        "ignoring '-d' if it is provided ! " + lphyFile);
            else {
                // change user.dir, so that the relative path in LPhy script e.g. 'readNexus' can work
                lphy.util.IOUtils.setUserDir(dir.toAbsolutePath().toString());
                // concatenate user.dir in front of file path
                actualPath = Paths.get(dir.toString(), actualPath.toString());
            }
        }
        // verify final file path
        if (!actualPath.toFile().exists()) {
            LoggerUtils.log.severe("Cannot find the LPhy script : " + actualPath +
                    " from the directory " + dir + ", set it using '-d' !");
            return;
        }

        LoggerUtils.log.info("Read LPhy script " + lphyFile + " from " + actualPath.getParent());

        BufferedReader reader;
        reader = new BufferedReader(new FileReader(actualPath.toFile()));
        panel.clear();
        panel.parser.setName(lphyFile.getName());
        panel.source(reader);
    }


    public static void exportToPNG(File imgFile, GraphicalModelPanel panel) throws IOException,IllegalArgumentException {
        final String imgFormat = "png";
        if (!imgFile.getName().endsWith(imgFormat))
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

        boolean succ = ImageIO.write(img, imgFormat, imgFile);
        if (!succ)
            throw new IOException("Failed to save graphical model to " + imgFile);
    }

}
