package lphy.app;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
}
