package itmo.labs.zavar.client.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import itmo.labs.zavar.gui.Launcher;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ErrorWindow {

	public static void show(String text) {
		Alert alert = new Alert(AlertType.ERROR, text);
		alert.setTitle("Lab8 Client Error");
		alert.initOwner(Launcher.getStage());
		alert.showAndWait();
	}

	public static void show(Exception e, String... text) {
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
