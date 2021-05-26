package itmo.labs.zavar.gui;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.HashMap;
import java.util.Scanner;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class ClientController {
	
	@FXML
	AnchorPane browser;
	@FXML
	ScrollPane scrollPane;
	
	HashMap<Integer, Circle> objectsMap = new HashMap<Integer, Circle>();
	
	@FXML
    void initialize() throws IOException 
    {
		Circle circle2 = new Circle(50, Paint.valueOf("RED"));
		circle2.setCenterX(-15);
		circle2.setCenterY(-400);
		
		Circle circle = new Circle(50, Paint.valueOf("BLUE"));
		circle.setCenterX(1500);
		circle.setCenterY(400);
		
		objectsMap.put(5, circle);
		objectsMap.put(3, circle2);
		
		Group group = new Group(circle2, circle);
		
        StackPane content = new StackPane(group);
        group.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            content.setMinWidth(newBounds.getWidth());
            content.setMinHeight(newBounds.getHeight());
        });

        scrollPane.setContent(content);
        scrollPane.setPannable(true);
        scrollPane.viewportBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            content.setPrefSize(newBounds.getWidth(), newBounds.getHeight());
        });
		

		Task<Void> task = new Task<Void>() {
        	@Override
        	protected Void call() throws Exception {
        		
        		while(!Launcher.client.isConnected()) {
        			Thread.sleep(100);
        		}
        		
                PipedInputStream pin = Launcher.client.getDataInput();
                
                Task<Void> task = new Task<Void>() {
                	@Override
                	protected Void call() throws Exception {
                		@SuppressWarnings("resource")
            			Scanner sc = new Scanner(pin);
            			while(true) {
            				//System.out.println(sc.hasNext() ? sc.next() : "");
            				if(sc.hasNext()) {
            					String a = sc.next();
            					System.out.println(a);
            					String[] res = a.split(";");
            					if(res[0].equals("DELETE")) {
            						circle.setFill(Paint.valueOf("GREEN"));
            						group.getChildren().clear();//(objectsMap.get(Integer.parseInt(res[1])));
            					}
            				}
            			}
                	}
                };
                
                new Thread(task).start();
        		
        		return null;
        	}
		};
		
		new Thread(task).start();
    }
	
	private class ObjCircle {
		Circle c;
		long id;
		
		public ObjCircle(Circle c, long id) {
			this.id = id;
			this.c = c;
		}

		public long getId() {
			return id;
		}

		public Circle getC() {
			return c;
		}
		
		
	}
}