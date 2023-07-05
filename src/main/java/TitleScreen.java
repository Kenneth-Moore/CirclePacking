

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class TitleScreen extends Scene {

    final static StackPane root = new StackPane();

    final Button playBtn;

    final Image backgroundIm = new Image("file:images/background.jpg");
    final ImageView background = new ImageView(backgroundIm);

    final HBox controlPane = new HBox();
    final Label waterMark = new Label();

    public TitleScreen(final Button pressPlay) {
        super(root);
        playBtn = pressPlay;

        playBtn.setText("Circle Packing");

        //TODO better style than this
        playBtn.setStyle(
                "-fx-padding: 10 10 10 10;\n" +
                "-fx-base: #" + Utils.hex(Color.ANTIQUEWHITE) + "; " +
                "-fx-font-size: 2.4em;");

        waterMark.setText("Trademark Flancrest Enterprises 2018");
        waterMark.setStyle("-fx-font-size: 14;"
                + "-fx-background-color: #" + Utils.hex(Color.CADETBLUE));
        waterMark.setPadding(new Insets(5));

        //root.setBackground(new Background(new BackgroundImage(backgroundIm,
        //        BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
        //        BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));

        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());

        controlPane.getChildren().add(playBtn);
        controlPane.setAlignment(Pos.CENTER);


        root.getChildren().addAll(background, controlPane, waterMark);
        StackPane.setAlignment(waterMark, Pos.BOTTOM_RIGHT);
    }
}
