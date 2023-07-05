

import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class Utils {
	
	private static final double OFFSET = 0.00000000005;

	public static final String[] defaultEdge = 
		{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", 
		 "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
	
	public static final String[] defaultVert = 
		{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", 
		 "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
	
	public static final String delim = "////";
	
    public static String hex(final Color color) {
        final long rd = Math.round(color.getRed() * 255);
        final long gr = Math.round(color.getGreen() * 255);
        final long bl = Math.round(color.getBlue() * 255);

        final String hex = String.format("%02x%02x%02x", rd, gr, bl);

        return hex;
    }

    public static void colorButton(final Node button, final Color color, final Color clicked) {
        button.setStyle("-fx-base: #" + hex(color));
        button.addEventHandler(
                MouseEvent.MOUSE_PRESSED, e -> button.setStyle("-fx-base: #" + hex(clicked)));
        button.addEventHandler(
                MouseEvent.MOUSE_RELEASED, e -> button.setStyle("-fx-base: #" + hex(color)));
    }

    public static ImageView renderColor(final Color color, final int wid, final int hei) {
        final WritableImage image = new WritableImage(wid, hei);

        setImageColor(image, color, wid, hei);

        final ImageView imageView = new ImageView(image);

        return imageView;
    }

    private static void setImageColor(final WritableImage image, final Color color, final int wid, final int hei) {
        final PixelWriter pixelWriter = image.getPixelWriter();

        for (int pixelX = 0; pixelX < wid; pixelX += 1) {
            for (int pixelY = 0; pixelY < hei; pixelY += 1) {
                pixelWriter.setColor(pixelX, pixelY, color);
            }
        }
    }

    public static void mainControlStyleBtn(final Button btn, final String text) {
        //TODO make it a cool circular button and such
        btn.setStyle("-fx-background-radius: 5em;"
                + "-fx-font-size: 16;"
                + "-fx-base: #" + hex(Color.ANTIQUEWHITE) ) ;
        btn.setText(text);
    }

    public static void gameStyleBtn(final ToggleButton btn, final String text) {
        //TODO make it a cool circular button and such
        btn.setStyle("-fx-background-radius: 8em;"
                + "-fx-font-size: 14;"
                + "-fx-base: #" + hex(Color.ANTIQUEWHITE) ) ;
        btn.setPrefHeight(80);
        btn.setPrefWidth(80);

        btn.setText(text);
    }

    public static void zoomStyleBtn(final Button btn, final String text) {
        //TODO make it a cool circular button and such
        btn.setStyle("-fx-background-radius: 8em;"
                + "-fx-font-size: 12;"
                + "-fx-base: #" + hex(Color.CADETBLUE) ) ;
        btn.setPrefHeight(30);
        btn.setPrefWidth(30);

        btn.setText(text);
    }

    public static void BufferHBox(final HBox box, final Priority priority,
                                  boolean thickMid, boolean ends, final int max) {
        final ObservableList<Node> kids = box.getChildren();

        final HBox result = new HBox(box.getSpacing());

        if (ends) {
            addGrowingPaneH(result, priority, max);
        }

        while (!kids.isEmpty()) {

            result.getChildren().add(kids.get(0));

            if (!kids.isEmpty() || ends) {
                addGrowingPaneH(result, priority, max);
            }
            if (!kids.isEmpty() && thickMid) addGrowingPaneH(result, priority, max);
        }
        box.getChildren().addAll(result.getChildren());
    }

    public static void addGrowingPaneH(final HBox box, final Priority priority, final int max) {
        final Pane start = new Pane();
        start.setMaxWidth(max);
        HBox.setHgrow(start, priority);
        box.getChildren().add(start);
    }

    public static void BufferVBox(final VBox box, final Priority priority, boolean thickMid) {
        final ObservableList<Node> kids = box.getChildren();

        final VBox result = new VBox(box.getSpacing());

        addGrowingPaneV(result, priority);

        while (!kids.isEmpty()) {

            result.getChildren().add(kids.get(0));

            addGrowingPaneV(result, priority);
            if (!kids.isEmpty() && thickMid) addGrowingPaneV(result, priority);
        }
        box.getChildren().addAll(result.getChildren());
    }
    
    // the list pr (problem list) is in the order:
    // start is too left, start is too right, start is too low, start is too high,
    // end is too left, end is too right, end is too low, end is too high
    public static Optional<Line> smartLine(final double sX, final double sY, final double eX, final double eY, 
    		final int width, final int height) {
    	final boolean[] pr = {sX < 0, sX > width, sY < 0, sY > height, eX < 0, eX > width, eY < 0, eY > height};
    	
    	if (!pr[0] && !pr[1] && !pr[2] && !pr[3] && !pr[4] && !pr[5] && !pr[6] && !pr[7]) {
    		// the line is entirely inside
    		return Optional.of(new Line(sX, sY, eX, eY));
    	} else if ((pr[0] && pr[4]) || (pr[1] && pr[5]) || (pr[2] && pr[6]) || (pr[3] && pr[7])) {
    		// the line is completely outside (the line can be completely outside without satisfying
    		// this condition!)
			return Optional.empty();
		} else if ((!pr[0] && !pr[1] && !pr[2] && !pr[3])) {
			// the start is inside
			final Vector2 direct = Vector2.normal(Vector2.create(sX - eX, sY - eY));
			final Vector2 start = Vector2.create(sX, sY);
			final Vector2 lineEnd = onePointOffLine(start, direct, width, height);
			
			return Optional.of(new Line(sX, sY, lineEnd.x, lineEnd.y));
			
		} else if ((!pr[4] && !pr[5] && !pr[6] && !pr[7])) {
			// the end is inside
			final Vector2 direct = Vector2.normal(Vector2.create(eX - sX, eY - sY));
			final Vector2 start = Vector2.create(eX, eY);
			final Vector2 lineStart = onePointOffLine(start, direct, width, height);
			
			return Optional.of(new Line(lineStart.x, lineStart.y, eX, eY));
		} 
    	
    	//here we attempt to solve when a line has both ends outside but is partially inside
    	
    	
    	return Optional.empty();
    }
    
    // if you have a line which has one end on screen, you can use this to find the onscreen part
    // of that line
    private static Vector2 onePointOffLine(final Vector2 start, final Vector2 direct, 
    		final int width, final int height) {
    	final Vector2 end;

		final double angle = Math.atan2(direct.y, direct.x);
		
		if (Math.abs(angle - Math.PI) < OFFSET || Math.abs(angle + Math.PI) < OFFSET) {
			end = Vector2.create(width, start.y);
			
    	} else if (Math.abs(angle - Math.PI / 2) < OFFSET) {
    		end = Vector2.create(start.x, 0);
    		
    	} else if (Math.abs(angle) < OFFSET) {
    		end = Vector2.create(0, start.y);
    		
    	} else if (Math.abs(angle + Math.PI / 2) < OFFSET) {
    		end = Vector2.create(start.x, height);
    		
    	} else if (-Math.PI < angle && angle < -Math.PI / 2) {
    		final double arg0 = (height - start.y) / Math.sin(angle);
    	    final double arg1 = (width - start.x) / Math.cos(angle);
			final double len = trueMin(arg0, arg1);
			end = start.add(direct.scale(len));

		} else if (-Math.PI / 2 < angle && angle < 0) {
			final double arg0 = (height - start.y) / Math.sin(angle);
    	    final double arg1 = start.x / Math.cos(angle);
			final double len = -trueMin(Math.abs(arg0), Math.abs(arg1));
			end = start.add(direct.scale(len));

		} else if (0 < angle && angle < Math.PI / 2) { 
			final double arg0 = start.y / Math.sin(angle);
    	    final double arg1 = start.x / Math.cos(angle);
			final double len = -trueMin(Math.abs(arg0), Math.abs(arg1));
			end = start.add(direct.scale(len));

		} else if (Math.PI / 2 < angle && angle < Math.PI) {
			final double arg0 = start.y / Math.sin(angle);
    	    final double arg1 = (width - start.x) / Math.cos(angle);
			final double len = -trueMin(Math.abs(arg0), Math.abs(arg1));
			end = start.add(direct.scale(len));

		} else {
			throw new RuntimeException("Something went wrong in 'onScreenLine' method");
		}

		return end;
    }
    
    public static double trueMin(double a, double b) {
    	final double result;
    	if (Math.abs(a) < Math.abs(b)) {
    		result = a;
    	} else {
    		result = b;
    	}
    	return result;
    }
    
    public static void addGrowingPaneV(final VBox box, final Priority priority) {
        final Pane start = new Pane();
        VBox.setVgrow(start, priority);
        box.getChildren().add(start);
    }
    
    public static void PicBuffer(final VBox box, int coef) {
    	final Node node = box.getChildren().get(0);
    	
    	box.getChildren().clear();
    	addGrowingPaneV(box, Priority.SOMETIMES);
    	box.getChildren().add(node);
	
        final Pane start = new Pane();
        start.setMinHeight(coef);
        VBox.setVgrow(start, Priority.SOMETIMES);
        box.getChildren().add(start);

        addGrowingPaneV(box, Priority.SOMETIMES);
    }
}
