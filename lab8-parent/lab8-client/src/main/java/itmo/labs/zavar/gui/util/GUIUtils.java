package itmo.labs.zavar.gui.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import itmo.labs.zavar.gui.Launcher;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class GUIUtils {
    
	public static class UTF8Control extends ResourceBundle.Control {
	    public ResourceBundle newBundle (String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException
	    {
	        // The below is a copy of the default implementation.
	        String bundleName = toBundleName(baseName, locale);
	        String resourceName = toResourceName(bundleName, "properties");
	        ResourceBundle bundle = null;
	        InputStream stream = null;
	        if (reload) {
	            URL url = loader.getResource(resourceName);
	            if (url != null) {
	                URLConnection connection = url.openConnection();
	                if (connection != null) {
	                    connection.setUseCaches(false);
	                    stream = connection.getInputStream();
	                }
	            }
	        } else {
	            stream = loader.getResourceAsStream(resourceName);
	        }
	        if (stream != null) {
	            try {
	                // Only this line is changed to make it to read properties files as UTF-8.
	                bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
	            } finally {
	                stream.close();
	            }
	        }
	        return bundle;
	    }
	}
	
    public static void showAndWaitError(String text) {
		Alert alert = new Alert(AlertType.ERROR, text);
		alert.setTitle("Lab8 Client Error");
		alert.initOwner(Launcher.getStage());
		alert.showAndWait();
	}

    public static void showError(String text) {
		Alert alert = new Alert(AlertType.ERROR, text);
		alert.setTitle("Lab8 Client Error");
		alert.initOwner(Launcher.getStage());
		alert.show();
	}
    
	public static void showAndWaitError(Exception e, String... text) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Lab8 Client Error");
		alert.initOwner(Launcher.getStage());
		if (text.length > 0) {
			alert.setHeaderText(text[0]);
		} else {
			alert.setHeaderText("Exception");
		}
		alert.setContentText(e.getLocalizedMessage());
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionText = sw.toString();
		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(textArea, 0, 1);
		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();
	}
}
