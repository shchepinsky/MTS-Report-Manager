package viewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // This is root node of controls hierarchy, root is usually a layout
        Parent root = FXMLLoader.load(getClass().getResource("mainscene.fxml"));

        // creating and setting Scene using root node of loaded control hierarchy
        primaryStage.setScene(new Scene(root, 32 * 16, 32 * 9));

        // show main stage with scene and it's loaded hierarchy of controls
        primaryStage.show();
    }
}
