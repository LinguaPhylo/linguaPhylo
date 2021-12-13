package lphyext.app;

import lphyext.manager.ExtManagerDialog;

import java.io.IOException;

/**
 * @author Walter Xie
 */
public class ExtManagerApp {

    public ExtManagerApp() {

        try {
            ExtManagerDialog extManager = new ExtManagerDialog(null);
            extManager.setVisible(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


    public static void main(String[] args) {
        ExtManagerApp managerApp = new ExtManagerApp();

    }

}
