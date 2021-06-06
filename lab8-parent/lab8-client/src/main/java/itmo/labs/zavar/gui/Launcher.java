package itmo.labs.zavar.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import itmo.labs.zavar.client.Client;
import itmo.labs.zavar.client.util.ClientState;
import itmo.labs.zavar.gui.util.GUIUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
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
	private static HashMap<String, String> invLangs = new HashMap<String, String>();
	private static Properties prop = new Properties();
	private Properties langsFile = new Properties();
	private static String home = System.getProperty("user.home");
	private static String propsPath = home + "/lab8Client/settings.properties";
	private static String mainPath = home + "/lab8Client";
	
	public static void main(String[] args) throws IOException, InterruptedException {
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
	
	public static HashMap<String, String> getInvLangs() {
		return invLangs;
	}
	
	@Override
	public void init() throws Exception {
		
		File main = new File(mainPath);
		if(!main.exists())
		{
			main.mkdirs();
			System.out.println("Created main folder");
		}
		
		File propFile = new File(propsPath);

		if(!propFile.exists())
		{
			propFile.createNewFile();
			Files.copy(getClass().getResourceAsStream("/settings.properties"), propFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Copyed properties");
		}
		
		prop.load(new FileInputStream(propsPath));
		
		client = new Client(new String[] {(String) prop.get("ip"), (String) prop.get("port")});
		
		String[] lang = ((String)prop.get("lang")).split("_");
		
		try {
			bundle = ResourceBundle.getBundle("langs/lang", new Locale(lang[0], lang[1]), new GUIUtils.UTF8Control());
		} catch(MissingResourceException e) {
			prop.setProperty("lang", "ru_RU");
			bundle = ResourceBundle.getBundle("langs/lang", new Locale("ru", "RU"), new GUIUtils.UTF8Control());
		}
		
		langsFile.load(getClass().getResourceAsStream("/langs/langs.properties"));
		
		Iterator<Object> keys = langsFile.keySet().iterator();
		
		ArrayList<String> find = new ArrayList<String>();
		while(keys.hasNext()) {
			String s = (String) keys.next();
			if(!s.contains("locale")) {
				find.add(s);
				String name = langsFile.getProperty(s);
				String locale = langsFile.getProperty(s.replace(".name", ".locale"));
				langs.put(name, locale);
				invLangs.put(locale, name);
			}
		}
		
		/*try (Stream<Path> paths = Files.walk(Paths.get(getClass().getResource("/langs").toURI()))) {
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
		}*/
		
        Platform.runLater(() -> {
    		splashStage = new Stage();
    		splashStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.png")));
            splashStage.initStyle(StageStyle.UNDECORATED);
            Scene scene = null;
			try {
				scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/Splash.fxml")), 200, 215);
			} catch (IOException e) {}
			ProgressBar bar = (ProgressBar) scene.getRoot().getChildrenUnmodifiable().get(1);
			bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
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
			if(getTheme() == 1) {
	    		getStage().getScene().getStylesheets().add("css/darkTheme.css");
	    	} else {
	    		getStage().getScene().getStylesheets().clear();
	    	}
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
			GUIUtils.showAndWaitError(e, "Error on start");
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

	public static int getTheme() {
		return Integer.parseInt((String) prop.get("theme"));
	}

	public static String getLang() {
		return (String) prop.get("lang");
	}
	
	public static void setLang(String lang) {
		prop.setProperty("lang", lang);
    	try {
			prop.store(new FileOutputStream(propsPath), null);
		} catch (IOException e) {
			GUIUtils.showAndWaitError(e);
		}
	}
	
	public static void setTheme(int theme) {
		prop.setProperty("theme", theme + "");
    	try {
			prop.store(new FileOutputStream(propsPath), null);
		} catch (IOException e) {
			GUIUtils.showAndWaitError(e);
		}
	}
}
