package itmo.labs.zavar.gui.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import itmo.labs.zavar.gui.Launcher;
import itmo.labs.zavar.studygroup.StudyGroup;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class GUIUtils {
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
        	GUIUtils.showError(e);
        }
    }

    public static void autoFitTable(TableView<StudyGroup> tableView) {
        tableView.getItems().addListener(new ListChangeListener<Object>() {
            @Override
            public void onChanged(Change<?> c) {
                for (Object column : tableView.getColumns()) {
                    try {
                        columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                    	GUIUtils.showError(e);
                    }
                }
            }
        });
    }
    
    public static void showError(String text) {
		Alert alert = new Alert(AlertType.ERROR, text);
		alert.setTitle("Lab8 Client Error");
		alert.initOwner(Launcher.getStage());
		alert.showAndWait();
	}

	public static void showError(Exception e, String... text) {
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
