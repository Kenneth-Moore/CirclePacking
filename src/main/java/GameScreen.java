

import graphbits.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

// this is where you actually play the game.
public class GameScreen extends Scene {

	public static final int WIDTH = 600;
	public static final int HEIGHT = 600;
	
	public static final int mathcircrad = 300;
	
	final static double dist = WIDTH/2;
	final static double slope = 1/ Math.sqrt(3);
	final static double offset = 1/(2*slope);
	
    final static HBox root = new HBox();
    
    final Color gridcol = Color.WHEAT;
    
    final VBox inputbox = new VBox();
    final VBox outptbox = new VBox();
    
    final ImageView inbg = renderColor(Color.WHITE);
    final ImageView otbg = renderColor(Color.WHITE);
    
    final ImageView clickspace = renderColor(Color.TRANSPARENT);
    
    final StackPane workstack = new StackPane();
    final StackPane donestack = new StackPane();
    
    final Slider dotsize = new Slider();
    final Button drawbtn = new Button();
    final HBox controls = new HBox();
    
    final TextField iters = new TextField();
    final Label validlabel = new Label();
    final HBox outcontrols = new HBox();
    
    final ToggleGroup inputgroup = new ToggleGroup();
    final RadioButton automabtn = new RadioButton();
    final RadioButton directbtn = new RadioButton();
    
    final Button calcbtn = new Button();
    final Button printbtn = new Button();
    
    final StackPane dotStack = new StackPane();
    final StackPane lineStack = new StackPane();
    final StackPane gridStack = new StackPane();
    
    final StackPane solstack = new StackPane();
    
    final Hexblob dots = new Hexblob();
    
    double zoom = 2;

	double currentX = 0;
	double currentY = 0;
    
    boolean Drawing = true;
    
    public GameScreen() {
        super(root);
    	root.getChildren().addAll(inputbox, outptbox);
    	root.setPadding(new Insets(10));
    	root.setSpacing(10);
    	
        dotsize.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
            	
            	int val;
            	try {
            		val = Integer.valueOf(iters.getText());
            	} catch (Exception e) {
            		System.out.println("Number not formatted correctly");
            		return;
            	}
            	if (val > 300) {
            		iters.setText("" + 300);
            	}
            	
            	zoom = dotsize.getValue();
               	setinteriordots();

        		renderScreen();
            }
        });
        
    	dotsize.setValue(2);
    	dotsize.setMax(2);
    	dotsize.setMin(0.3);
    	dotsize.setPrefWidth(320);
    	dotsize.setTooltip(new Tooltip("Zoom in and out of the control space."));
    	
    	calcbtn.setText("Go");
    	calcbtn.setTooltip(new Tooltip("Run the iteration with the set amount of iters."));
    	calcbtn.setOnAction(event -> {
    		setinteriordots();
    		renderScreen();
    	});
    	iters.setText("120");
    	iters.setPrefWidth(100);
    	iters.setOnAction(event -> {
           	setinteriordots();
    		renderScreen();
    	});
    	
    	iters.setTooltip(new Tooltip("The number of iterations to use. If the "
    			+ "diagram looks off, typically increasing this helps!"));
    	
    	validlabel.setText("Iterations:");
    	validlabel.setTooltip(new Tooltip("The number of iterations to use. If the "
    			+ "diagram looks off, typically increasing this helps!"));
    	
    	drawbtn.setText("Drawing");
    	drawbtn.setDisable(true);
    	drawbtn.setTooltip(new Tooltip("With 'Place Circs' selected, you can toggle between drawing or erasing circs with this."));
    	drawbtn.setPrefWidth(80);
    	Utils.colorButton(drawbtn, Color.CORNSILK, Color.GOLD);
    	drawbtn.setOnAction(event -> {
    		Drawing = !Drawing;
    		if (Drawing) drawbtn.setText("Drawing");
    		else         drawbtn.setText("Erasing");
    	});
    	
    	
    	printbtn.setText("Print");
    	printbtn.setTooltip(new Tooltip("Print all the result circles '(x coord, y coord), radius' in the console."));
    	printbtn.setOnAction(event -> {
    		// print all the circles locations and radii
    		
    		for (Hexdot dot : dots.getdots()) {
    			System.out.println("(" + dot.getXpos() + "," + dot.getYpos() + "), " + dot.getRadius());
    		}
    	});
    	
    	controls.getChildren().addAll(dotsize, drawbtn, automabtn, directbtn);
    	controls.setSpacing(10);
    	controls.setAlignment(Pos.CENTER);
    	
    	outcontrols.getChildren().addAll(validlabel, iters, calcbtn, printbtn);
    	outcontrols.setSpacing(10);
    	outcontrols.setAlignment(Pos.CENTER);
    	
    	inputbox.setPrefSize(WIDTH, HEIGHT);
    	inputbox.setSpacing(10);
    	outptbox.setPrefSize(WIDTH, HEIGHT);
    	outptbox.setSpacing(10);
    	   
    	inputgroup.getToggles().addAll(automabtn, directbtn);
    	automabtn.setText("Draw Curve");
    	automabtn.setSelected(true);
    	automabtn.setTooltip(new Tooltip("Control the input by drawing a closed curve and autofilling with circles."));

    	directbtn.setText("Place Circs");
    	directbtn.setTooltip(new Tooltip("Control the input by placing circles directly."));
    	
    	automabtn.setOnAction(event -> {
    		drawbtn.setDisable(true);
    		
        	dotStack.getChildren().clear();
        	gridStack.getChildren().clear();
        	solstack.getChildren().clear();
        	lineStack.getChildren().clear();

        	dots.clear();
        	renderScreen();
        	
        	clickspace.setOnMouseDragged(dragevent -> {});
    	});
    	
    	directbtn.setOnAction(event -> {
    		drawbtn.setDisable(false);
    		
        	dotStack.getChildren().clear();
        	gridStack.getChildren().clear();
        	solstack.getChildren().clear();
        	lineStack.getChildren().clear();
        	
        	dots.clear();
        	renderScreen();

        	clickspace.setOnMouseDragged(dragevent -> {
        		if (directbtn.isSelected()) {
        			dots.set(dragevent.getX()-mathcircrad, dragevent.getY()-mathcircrad, zoom, Drawing);
        			renderScreen();
        			return;
        		}
        	});
        	clickspace.setOnMouseReleased(relevent -> {});

    	});
    	
    	
    	clickspace.setPickOnBounds(true);
    	
    	clickspace.setOnMousePressed(pressevent -> {
    		lineStack.getChildren().clear();
    		final double startX = pressevent.getX();
    		final double startY = pressevent.getY();
    		
    		if (directbtn.isSelected()) {
    			dots.set(startX-mathcircrad, startY-mathcircrad, zoom, Drawing);
    			renderScreen();
    			return;
    		}
    		
    		currentX = pressevent.getX();
    		currentY = pressevent.getY();
    		//dots.set(pressevent.getX()-dist, pressevent.getY()-dist, zoom, Drawing);
    		//renderScreen();
    		
        	clickspace.setOnMouseDragged(dragevent -> {
        		
        		if (dist(currentX, currentY, dragevent.getX(), dragevent.getY()) > 10) {
        			final Line lin = new Line(currentX, currentY, dragevent.getX(), dragevent.getY());
        			lineStack.getChildren().add(0, lin);
        			lineStack.getChildren().get(0).setTranslateX((currentX + dragevent.getX())/2 - dist);
        			lineStack.getChildren().get(0).setTranslateY((currentY + dragevent.getY())/2 - dist);

        			
            		currentX = dragevent.getX();
            		currentY = dragevent.getY();
        		}
        		
        		//dots.set(dragevent.getX()-dist, dragevent.getY()-dist, zoom, Drawing);
        	});
        	
        	clickspace.setOnMouseReleased(dropevent -> {
    			lineStack.getChildren().add(0, 
    					new Line(currentX, currentY, startX, startY));
    			lineStack.getChildren().get(0).setTranslateX((currentX + startX)/2 - dist);
    			lineStack.getChildren().get(0).setTranslateY((currentY + startY)/2 - dist);
    			
               	clickspace.setOnMouseDragged(dragevent -> {});
               	
               	setinteriordots();
               	
        		renderScreen();
        	});
    	});
    	

    	
    	workstack.getChildren().addAll(inbg, gridStack, dotStack, lineStack, clickspace);

    	final Circle Mathcirc = new Circle(mathcircrad);
    	Mathcirc.setFill(Color.TRANSPARENT);
    	Mathcirc.setStroke(Color.BLACK);
    	donestack.getChildren().addAll(otbg, solstack, Mathcirc);
    	
    	inputbox.getChildren().addAll(workstack, controls);
    	outptbox.getChildren().addAll(donestack, outcontrols);

    	renderScreen();
    	
    }

    // this redraws the screen. It's also used to refresh things since it clears all lists.
    public void renderScreen() {
    	dotStack.getChildren().clear();
    	gridStack.getChildren().clear();
    	solstack.getChildren().clear();
    	
    	try {
			dots.makeDots(Integer.valueOf(iters.getText())); 
			makeDots();
		} catch (NumberFormatException e) {
			System.out.println("Did not format number correctly");
		}
    	
    	renderLines();
    	System.out.println(" - ");
    }
    
    private void setinteriordots() {
    	
    	if (lineStack.getChildren().size() < 4) {
    		return;
    	}
    	
    	for(int i = 20-Hexblob.blobradius; i < Hexblob.blobradius-20; i++) {
			final double Px = Hexblob.topixX(i, zoom) + mathcircrad;
			
    		for (int j = 20-Hexblob.blobradius; j < Hexblob.blobradius-20; j++) {
    			final double Py = Hexblob.topixY(i, j, zoom) + mathcircrad;
    			
    			int hits = 0;
    			// check how many times the ray hits a line 
        		for (Node linode : lineStack.getChildren()) {
        			Line lin = (Line) linode;
        			
        			final double Ax = lin.getStartX();
        			final double Bx = lin.getEndX();
        			
        			final double Ay = lin.getStartY();
        			final double By = lin.getEndY();
        			
        			boolean crossY = (Ay >= Py && By < Py) || (Ay <= Py && By > Py);
        			
        			// The segments are so small, may as well just not count it if the point
        			// is so near to outside that this next check makes a difference:
        			boolean crossX = (Ax + Bx)/2 > Px;
        			
        			if (crossY && crossX) hits++;
        		}
        		
        		hits = hits % 2;
				dots.hexset(i, j, hits == 1);
    		}
    	}
    }
    
    private static double dist(final double x1, final double y1, final double x2, final double y2) {
    	
    	return Math.sqrt(((x1 - x2)*(x1 - x2)) + ((y1 - y2)*(y1 - y2)));
    }
    
    public void makeDots() {
    	int numsol = 0;
    	for (int i = -Hexblob.blobradius; i < Hexblob.blobradius; i++) {
        	for (int j = -Hexblob.blobradius; j < Hexblob.blobradius; j++) {
        		if (dots.get(i,j)) {
		        	
		        	if (dots.isplaced(i, j)) {
		        		numsol++; // numsol should be equal to the number of dots...
		        		final Circle solcirc = dots.solvisu(i, j);
			        	solstack.getChildren().add(0, solcirc);
			        	solstack.getChildren().get(0).setTranslateX(solcirc.getCenterX());
			        	solstack.getChildren().get(0).setTranslateY(solcirc.getCenterY());
		        	}
		        	
		    		final Circle circ = dots.visu(i, j, zoom);
		        	dotStack.getChildren().add(0, circ);
		        	dotStack.getChildren().get(0).setTranslateX(circ.getCenterX());
		        	dotStack.getChildren().get(0).setTranslateY(circ.getCenterY());
		        	

        		}
        	}
    	}
    	System.out.println("Number in solve stack = " + numsol);
    }
    
    
    public void renderLines() {
    	
    	double jump = 20*zoom;
    	while (jump < 18) jump += 20*zoom;

    	for(double i = 0; i < dist; i += jump*offset) {
    		addline(i, -dist , i, dist, -1);
    		if (i == 0) continue;
    		addline(-i, -dist , -i, dist, -1);
    	}
    	
    	for(double i = 0; i < dist+(slope*dist); i += jump) {
    		addline(-dist, i - slope*dist , dist, i + slope*dist, 0);
    		addline(-dist, i + slope*dist , dist, i - slope*dist, 1);
    		if (i == 0) continue;
    		addline(-dist, -i - slope*dist , dist, -i + slope*dist, 2);
    		addline(-dist, -i + slope*dist , dist, -i - slope*dist, 3);
    	}
    }
    
    private void addline(double sx, double sy, double ex, double ey, int quad) {
    	
    		switch(quad) {
    			case 0: // going to bottom left
    				ey = Math.min(dist, ey);
    				ex = sx - (1/slope) * (sy - ey);
    				break;
    			case 1: // going to bottom right
    				sy = Math.min(dist, sy);
    				sx = ex + (1/slope) * (ey - sy);
    				break;
    			case 2: // going to top right
    				sy = Math.max(-dist, sy);
    				sx = ex - (1/slope) * (ey - sy);
    				break;
    			case 3:  // going to top left
    				ey = Math.max(-dist, ey);
    				ex = sx + (1/slope) * (sy - ey);
    		}
			
			Line gridline = new Line(sx, sy, ex, ey);
			gridline.setStroke(gridcol);
			gridStack.getChildren().add(0, gridline);
		    final double transX = (gridline.getStartX() + gridline.getEndX()) / 2;
		    final double transY = (gridline.getStartY() + gridline.getEndY()) / 2;
		    gridStack.getChildren().get(0).setTranslateX(transX);
		    gridStack.getChildren().get(0).setTranslateY(transY);
    }
    
    private static ImageView renderColor(final Color color) {
        final WritableImage image = new WritableImage(WIDTH, HEIGHT);

        setImageColor(image, color);

        final ImageView imageView = new ImageView(image);

        return imageView;
    }
    
    private static void setImageColor(final WritableImage image, final Color color) {
        final PixelWriter pixelWriter = image.getPixelWriter();

        for (int pixelX = 0; pixelX < WIDTH; pixelX += 1) {
            for (int pixelY = 0; pixelY < HEIGHT; pixelY += 1) {
                pixelWriter.setColor(pixelX, pixelY, color);
            }
        }
    }
 

}



