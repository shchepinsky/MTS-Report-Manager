package viewer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Window;

public class AboutController {
    @FXML
    private Button buttonClose;

    @FXML
    void initialize() {
        assert buttonClose != null : "fx:id=\"buttonClose\" was not injected: check your FXML file 'aboutscene.fxml'.";

    }

    @FXML
    void onButtonClose(ActionEvent event) {
        // window is superclass of Stage which has hide() method.
        Window window = buttonClose.getScene().getWindow();
        // hide() closes window, if no other windows are open then app will exit
        window.hide();
    }
}