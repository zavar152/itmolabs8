package itmo.labs.zavar.gui;

import java.io.IOException;
import java.io.PipedInputStream;

import itmo.labs.zavar.client.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Launcher extends Application{

	public static Client client;
	public static PipedInputStream pin;
	
	public static void main(String[] args) throws IOException, InterruptedException {
        /*Task<Void> task = new Task<Void>() {
        	@Override
        	protected Void call() throws Exception {
				client = new Client(args);
				return null;
        	}
        };
        new Thread(task).start();*/
		client = new Client(args);
		launch(args);
	}

	@Override
	public void stop() throws Exception {
		client.close();
		Platform.exit();
		System.exit(0);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/app.fxml")), 600, 400);
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(400);
		primaryStage.setMinWidth(600);
		primaryStage.setResizable(true);
		primaryStage.setTitle("Lab8 Client");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.png")));
		primaryStage.show();
		
		Task<Void> task = new Task<Void>() {
        	@Override
        	protected Void call() throws Exception {
        		client.connect();
        		return null;
        	}
		};
		
		new Thread(task).start();
		 /*
       	// установка надписи
        Text text = new Text("Hello from JavaFX!");
        text.setLayoutY(80);    // установка положения надписи по оси Y
        text.setLayoutX(100);   // установка положения надписи по оси X
        
        Text text2 = new Text("Connected!");
        text2.setLayoutY(130);    // установка положения надписи по оси Y
        text2.setLayoutX(100);   // установка положения надписи по оси X
         
        Button b = new Button("Login");
        
        b.setOnAction(e -> {
        	
            Task<Void> task = new Task<Void>() {
            	@Override
            	protected Void call() throws Exception {
                	try {
                		b.setDisable(true);
                		String res = client.executeCommand("login", new ReaderInputStream(new StringReader("zavar\n123456789"), StandardCharsets.UTF_8));
                		if(res.equals("cfail")) {
                			String buf = text2.getText();
                			text2.setText("Reconnecting...");
                			client.reconnect();
                			text2.setText(buf);
                		}
                		b.setDisable(false);
        			} catch (InterruptedException | IOException e1) {
        				// TODO Auto-generated catch block
        				e1.printStackTrace();
        			} 
                	return null;
            	}
            };
            new Thread(task).start();
        });
        
        
        
		/*Platform.runLater(() -> {
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(pin);
			while(true) {
				text.setText(sc.hasNext() ? sc.next() : "");
			}
		});
        
        Task<Void> task2 = new Task<Void>() {
        	@Override
        	protected Void call() throws Exception {
        		if(!client.isConnected()) {
        			text2.setText("Connecting...");
        			while(!client.isConnected());
        		}
        		
        		text2.setText("Connected!");
        		
                PipedInputStream pin = client.getDataInput();
                
                Task<Void> task = new Task<Void>() {
                	@Override
                	protected Void call() throws Exception {
                		@SuppressWarnings("resource")
            			Scanner sc = new Scanner(pin);
            			while(true) {
            				text.setText(sc.hasNext() ? sc.next() : "");
            			}
                	}
                };
                
                new Thread(task).start();
        		
				return null;
        	}
        };
        new Thread(task2).start();
        
        Group group = new Group(b, text, text2);
         
        Scene scene = new Scene(group);
        primaryStage.setScene(scene);
        primaryStage.setTitle("First Application");
        primaryStage.setWidth(300);
        primaryStage.setHeight(250);
        primaryStage.show();*/
	}

}
