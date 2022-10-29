import dao.ContactDAO;
import dao.GroupDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;


public class Main extends Application{
    @Override
    public void init() throws Exception {
        ContactDAO.getInstance().loadContactsFromFile();
        GroupDAO.getInstance().loadGroupsFromFile();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ui/contact.fxml")));
        primaryStage.setTitle("Contact Management System");
        primaryStage.setScene(new Scene(root, 700, 575));
        primaryStage.show();

        // đóng tất cả các cửa sổ khi cửa sổ chính này đóng
        primaryStage.setOnHiding(windowEvent -> {
            Platform.exit();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}