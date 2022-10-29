package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class OpenWindow {

    /**
     * mở cửa sổ của link fxml và title truyền vào
     * @param fxml
     * @param title
     * @return controller của fxml
     * @throws IOException
     */
    public static Object open(String fxml, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(OpenWindow.class.getResource(fxml));
        Parent parent = loader.load();
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
        return loader.getController();
    }
}
