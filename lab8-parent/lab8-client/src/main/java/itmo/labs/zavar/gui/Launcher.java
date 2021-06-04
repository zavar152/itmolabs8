package itmo.labs.zavar.gui;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import itmo.labs.zavar.client.Client;
import itmo.labs.zavar.client.util.ClientState;
import itmo.labs.zavar.gui.util.GUIUtils;
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
	private static boolean stopFlag = false;
	private Task<Void> clientInit;
	private Stage splashStage;
	private static Stage mainStage;
	private ResourceBundle bundle;
	private static HashMap<String, String> langs = new HashMap<String, String>();
	private Properties langsFile = new Properties();
	
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
		stopFlag = true;
		client.close();
		Platform.exit();
		System.exit(0);
	}
	
	public static HashMap<String, String> getLangs() {
		return langs;
	}
	
	@Override
	public void init() throws Exception {
		
		bundle = ResourceBundle.getBundle("langs/lang", new Locale("ru", "RU"));
		
		langsFile.load(getClass().getResourceAsStream("/langs/langs.properties"));
		
		Iterator<Object> keys = langsFile.keySet().iterator();
		
		while(keys.hasNext()) {
			String name = langsFile.getProperty((String) keys.next());
			String locale = langsFile.getProperty((String) keys.next());
			langs.put(name, locale);
		}
		
		try (Stream<Path> paths = Files.walk(Paths.get(getClass().getResource("/langs").toURI()))) {
		    paths.filter(Files::isRegularFile).forEach(e -> {
		    	if(!e.getFileName().toString().equals("langs.properties")) {
		    		String loc = e.getFileName().toString().replaceAll("lang_", "").replaceAll(".properties", "");
		    		if(!langsFile.values().contains(loc)) {
		    			GUIUtils.showAndWaitError("Check your langs files!");
		    			try {
		    				stop();
		    			} catch (Exception e1) {}
		    		}
		    	}
		    });
		}
		
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
			GUIUtils.showAndWaitError("Connection failed");
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
			Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/App.fxml"), bundle), 730, 420);
			mainStage.setScene(scene);
			mainStage.setMinHeight(400);
			mainStage.setMinWidth(600);
			mainStage.setResizable(false);
			mainStage.setTitle("Lab8 Client");
			mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.png")));
			//mainStage.getScene().getStylesheets().add("css/darkTheme.css");
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
			GUIUtils.showError(e, "Error on start");
		}
	}
	
	public static Client getClient() {
		return client;
	}

	public static Stage getStage() {
		return mainStage;
	}

	public static boolean isStop() {
		return stopFlag;
	}
}
