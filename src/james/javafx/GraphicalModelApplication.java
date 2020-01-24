package james.javafx;

import james.Coalescent;
import james.TimeTree;
import james.core.distributions.LogNormal;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Random;

public class GraphicalModelApplication extends Application {

    Stage stage;
    Scene startScene;
    static int WIDTH = 800;
    static int HEIGHT = 800;

    public GraphicalModelApplication() {

        Random random = new Random();

        Value<Double> thetaM = new Value<>("M", 3.0);
        Value<Double> thetaS = new Value<>("S", 1.0);
        LogNormal logNormal = new LogNormal(thetaM, thetaS, random);

        RandomVariable<Double> theta = logNormal.sample("\u0398");
        Value<Integer> n = new Value<>("n", 20);

        Coalescent coalescent = new Coalescent(theta, n, random);

        RandomVariable<TimeTree> g = coalescent.sample();

        GraphicalModelPane start = new GraphicalModelPane(g);

        startScene = new Scene(start, WIDTH, HEIGHT);


        String stylesheet = fileToStylesheetString( new File("css/graphicalModel.css") );
        startScene.getStylesheets().add(stylesheet);
    }

    public String fileToStylesheetString ( File stylesheetFile ) {
        try {
            return stylesheetFile.toURI().toURL().toString();
        } catch (MalformedURLException e) {
            return null;
        }
    }

        @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.setTitle("Graphical Model Stage");
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.setScene(startScene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(GraphicalModelApplication.class, args);
    }
}
