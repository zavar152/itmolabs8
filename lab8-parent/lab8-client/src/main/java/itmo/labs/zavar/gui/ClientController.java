package itmo.labs.zavar.gui;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import org.apache.commons.io.input.ReaderInputStream;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseEnum;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import itmo.labs.zavar.client.ParseCoordinates;
import itmo.labs.zavar.client.ParsePerson;
import itmo.labs.zavar.studygroup.FormOfEducation;
import itmo.labs.zavar.studygroup.StudyGroup;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class ClientController {
	
	private final String[] nameMapping = new String[] { "id", "name", "coordinates", "creationDate",
			"studentsCount", "expelledStudents", "transferredStudents", "formOfEducation", "groupAdmin" };
	private ICsvBeanReader beanReader;
	private Stack<StudyGroup> stack = new Stack<StudyGroup>(); 
	
	@FXML
	AnchorPane browser;
	@FXML
	ScrollPane scrollPane;
	@FXML
	Label info_text;
	@FXML
	Button show_button;
	
	HashMap<Integer, Circle> objectsMap = new HashMap<Integer, Circle>();
	
	@FXML
    void initialize() throws IOException 
    {
		
		show_button.setOnAction(e -> {
			try {
				//BufferedOutputStream bout = new BufferedOutputStream(pout);
				//Writer pwriter = new OutputStreamWriter(pout, StandardCharsets.US_ASCII);
				Launcher.client.executeCommand("show", new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8));
				Task<Void> task = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						Scanner sc = new Scanner(Launcher.client.getAnswerInput());
						while (sc.hasNextLine()) {
							String a = sc.next();
							String[] m = a.split(",");
							String b = a.replace(","+m[m.length-1], "");
							System.out.println("Owner: " + m[m.length-1]);
							System.out.println(b);
							if(a.contains("/-/"))
								break;
							generateStydyGroup(b);
							System.out.println("Stack: "+ stack.size());
						}
						System.out.println();
						return null;
					}
				};
				new Thread(task).start();
				
				task.setOnRunning(s -> {
					show_button.setDisable(true);
				});
				
				task.setOnSucceeded(s -> {
					System.out.println("Done");
					show_button.setDisable(false);
				});
    			
			} catch (InterruptedException | IOException e1) {
				e1.printStackTrace();
			}
		});
		
		Circle circle2 = new Circle(50, Paint.valueOf("RED"));
		circle2.setCenterX(-15);
		circle2.setCenterY(-400);
		
		Circle circle = new Circle(50, Paint.valueOf("BLUE"));
		circle.setCenterX(1500);
		circle.setCenterY(400);
		
		objectsMap.put(5, circle);
		objectsMap.put(3, circle2);
		
		Group group = new Group(circle, circle2);
		
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
		

		ScaleTransition sctr = new ScaleTransition(Duration.millis(2000), circle);
        sctr.setToX(0);
        sctr.setToY(0);
        sctr.setCycleCount(1);
        sctr.setAutoReverse(true);
        SequentialTransition str = new SequentialTransition();
        str.getChildren().addAll(sctr);
        
		Task<Void> task = new Task<Void>() {
        	@Override
        	protected Void call() throws Exception {
        		
        		while(!Launcher.client.isConnected()) {
        			Thread.sleep(100);
        		}
        		
        		Launcher.client.executeCommand("login", new ReaderInputStream(new StringReader("zavar\n123456789"), StandardCharsets.UTF_8));
        		
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
            						//circle.setFill(Paint.valueOf("GREEN"));
            						//group.getChildren().clear();//(objectsMap.get(Integer.parseInt(res[1])));
            						//((Group)((StackPane)scrollPane.contentProperty().get()).getChildren().get(0)).getChildren().remove(0);
            						Platform.runLater(() -> {
            					        str.play();
            					        str.setOnFinished(e -> {
            					        	group.getChildren().remove(circle);
            					        });
            						}); 
            					}
            				}
            			}
                	}
                };
                
                new Thread(task).start();
        		
        		return null;
        	}
		};
		
		task.setOnRunning(s -> {
			info_text.setText("Status: Connecting...");
			scrollPane.setDisable(true);
		});
		
		task.setOnSucceeded(s -> {
			info_text.setText("Status: Connected");
			scrollPane.setDisable(false);
		});
		
		
		
		new Thread(task).start();
    }

	private void generateStydyGroup(String s) {
		beanReader = new CsvBeanReader(new StringReader(s), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
			//beanReader.getHeader(true);
		StudyGroup temp;
		try {
			while ((temp = beanReader.read(StudyGroup.class, nameMapping, getReaderProcessors())) != null) {
					stack.push(temp);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			beanReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private CellProcessor[] getReaderProcessors() {
		CellProcessor[] processors = new CellProcessor[] { new NotNull(new ParseLong()), new NotNull(),
				new NotNull(new ParseCoordinates()), new NotNull(new ParseDate("yyyy-MM-dd")),
				new NotNull(new ParseLong()), new NotNull(new ParseInt()), new NotNull(new ParseLong()),
				new NotNull(new ParseEnum(FormOfEducation.class)), new Optional(new ParsePerson()) };
		return processors;
	}
}