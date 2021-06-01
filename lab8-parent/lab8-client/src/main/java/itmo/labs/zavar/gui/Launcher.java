package itmo.labs.zavar.gui;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Locale;
import java.util.ResourceBundle;

import itmo.labs.zavar.client.Client;
import itmo.labs.zavar.client.util.ClientState;
import itmo.labs.zavar.client.util.ErrorWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Launcher extends Application{

	private static Client client;
	private Task<Void> clientInit;
	private Stage splashStage;
	private static Stage mainStage;
	private ResourceBundle bundle;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length != 2) {
			System.out.println("You should enter ip and port!");
			System.exit(0);
		}
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
	public void init() throws Exception {
		
		bundle = ResourceBundle.getBundle("langs/lang", new Locale("ru", "RU"));
		
        Platform.runLater(() -> {
    		splashStage = new Stage();
    		splashStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.png")));
            splashStage.initStyle(StageStyle.UNDECORATED);
            Scene scene = null;
			try {
				scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/Splash.fxml")), 200, 215);
			} catch (IOException e) {}
            splashStage.setScene(scene);
        });
		
		clientInit = new Task<Void>() {
        	@Override
        	protected Void call() throws Exception {
        		client.connect();
        		while(!client.isConnected()) {
        			if(client.getClientState() == ClientState.ERROR || client.getClientState() == ClientState.HOST_ERROR) {
        				throw new ConnectException();
        			}
        		}
        		return null;
        	}
		};
		
		clientInit.setOnRunning(e -> {
        	splashStage.show();
		});
		
		clientInit.setOnSucceeded(e -> {
			splashStage.hide();
		});
		
		clientInit.setOnFailed(e -> {
			ErrorWindow.show("Connection failed");
			try {
				stop();
			} catch (Exception e1) {}
		});
		
		new Thread(clientInit).start();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			mainStage = primaryStage;
			Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/App.fxml"), bundle), 650, 400);
			mainStage.setScene(scene);
			mainStage.setMinHeight(400);
			mainStage.setMinWidth(600);
			mainStage.setResizable(true);
			mainStage.setTitle("Lab8 Client");
			mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.png")));
			if (client.isConnected()) {
				mainStage.show();
			} else {
				clientInit.setOnSucceeded(e -> {
					splashStage.hide();
					mainStage.show();
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErrorWindow.show(e, "Error on start");
		}
	}
	
	public static Client getClient() {
		return client;
	}

	public static Stage getStage() {
		return mainStage;
	}
}
