
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.control.Button;


public class CirclePacking extends Application {

	
	// this is the playBtn, xBtn, title, and described for 

	
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
    	
    	final Button butt = new Button();
    	
    	final TitleScreen titleScene = new TitleScreen(butt);
        final GameScreen gamescreen = new GameScreen();
        
        butt.setOnAction(event -> primaryStage.setScene(gamescreen));
        
        primaryStage.setScene(titleScene);
        primaryStage.setTitle("Circle Packer");
        primaryStage.setHeight(700);
        primaryStage.setWidth(1235);
        primaryStage.show();
    }
}
