package Application;


import java.awt.Image;
import java.awt.event.MouseEvent;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * @author pepper
 */

public class main extends Application {

    @Override
    public void start(Stage stage)  throws Exception{
            
        try {
            stage.setTitle("密碼管理");
            Parent root = FXMLLoader.load(getClass().getResource("scene_login.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        
        launch(args);
    }

}
