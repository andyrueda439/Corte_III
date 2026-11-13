package ni.edu.uam.ejercicioc3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.ejercicioc3.database.JPAUtil;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("laboratorio-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Control de equipos de laboratorio");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        JPAUtil.cerrar();
    }
}