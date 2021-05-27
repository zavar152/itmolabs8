package itmo.labs.zavar.gui;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;
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
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
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
	private AnchorPane browser;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private Label info_text;
	@FXML
	private Button show_button;
	
	private HashMap<Integer, Circle> objectsMap = new HashMap<Integer, Circle>();
	private Group objectGroup = new Group();
	
	@FXML
    void initialize() throws IOException 
    {
		
		/*Circle circle2 = new Circle(50, Paint.valueOf("RED"));
		circle2.setCenterX(-15);
		circle2.setCenterY(-400);
		
		Circle circle = new Circle(50, Paint.valueOf("BLUE"));
		circle.setCenterX(1500);
		circle.setCenterY(400);
		
		objectsMap.put(5, circle);
		objectsMap.put(3, circle2);*/
		
		
        StackPane content = new StackPane(objectGroup);
        objectGroup.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            content.setMinWidth(newBounds.getWidth());
            content.setMinHeight(newBounds.getHeight());
        });

        scrollPane.setContent(content);
        scrollPane.setPannable(true);
        scrollPane.viewportBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            content.setPrefSize(newBounds.getWidth(), newBounds.getHeight());
        });
        
		show_button.setOnAction(e -> {

			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {

					while (!Launcher.client.isConnected()) {
						Thread.sleep(100);
					}

					Launcher.client.executeCommand("login",
							new ReaderInputStream(new StringReader("zavar\n123456789"), StandardCharsets.UTF_8));

					PipedInputStream pin = Launcher.client.getDataInput();

					Task<Void> task = new Task<Void>() {
						@Override
						protected Void call() throws Exception {
							@SuppressWarnings("resource")
							Scanner sc = new Scanner(pin);
							while (true) {
								if (sc.hasNext()) {
									String a = sc.next();
									System.out.println(a);
									String[] res = a.split(";");
									if (res[0].equals("DELETE")) {
										
										Circle circle = objectsMap.get(Integer.parseInt(res[1]));
										
										ScaleTransition sctr = new ScaleTransition(Duration.millis(2000), circle);
								        sctr.setToX(0);
								        sctr.setToY(0);
								        sctr.setCycleCount(1);
								        sctr.setAutoReverse(true);
								        SequentialTransition deleteSt = new SequentialTransition();
								        deleteSt.getChildren().addAll(sctr);
										
										Platform.runLater(() -> {
											deleteSt.play();
											deleteSt.setOnFinished(e -> {
												objectGroup.getChildren().remove(circle);
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
				info_text.setText("Status: Building objects...");
				Task<Void> task2 = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						try {
						Thread.sleep(1000);
						prepareObjectBrowser();
						} catch(Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				new Thread(task2).start();

				task2.setOnSucceeded(e2 -> {
					scrollPane.setDisable(false);
					info_text.setText("Status: Ready");
				});

			});

			new Thread(task).start();

		});
    }

	private String[] generateStydyGroup(String s, String own) throws IOException {
		beanReader = new CsvBeanReader(new StringReader(s), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
		// beanReader.getHeader(true);
		StudyGroup temp;
		String[] out = null;
		while ((temp = beanReader.read(StudyGroup.class, nameMapping, getReaderProcessors())) != null) {
			stack.push(temp);
			out = new String[] {temp.getCoordinates().getX() + "", temp.getCoordinates().getY()  + "", temp.getId() + "", temp.getStudentsCount() + ""};
		}
		beanReader.close();
		return out;
	}
	
	private CellProcessor[] getReaderProcessors() {
		CellProcessor[] processors = new CellProcessor[] { new NotNull(new ParseLong()), new NotNull(),
				new NotNull(new ParseCoordinates()), new NotNull(new ParseDate("yyyy-MM-dd")),
				new NotNull(new ParseLong()), new NotNull(new ParseInt()), new NotNull(new ParseLong()),
				new NotNull(new ParseEnum(FormOfEducation.class)), new Optional(new ParsePerson()) };
		return processors;
	}
	
	private void prepareObjectBrowser() {
		ParallelTransition str = new ParallelTransition();
		try {
			Launcher.client.executeCommand("show", new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8));
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					try {
						Scanner sc = new Scanner(Launcher.client.getAnswerInput());
						while (sc.hasNextLine()) {
							String a = sc.next();
							String[] m = a.split(",");
							String own = m[m.length - 1];
							String b = a.replace("," + own, "");
							if (a.contains("/-/"))
								break;
							System.out.println("Owner: " + own);
							System.out.println(b);
							String[] ar = generateStydyGroup(b, own);
							Platform.runLater(() -> {
								byte[] l = own.getBytes();
								long seed = 0;
								for (int i = 0; i < l.length; i++)
									seed = seed + l[i];
								Random random = new Random(seed);
								int nextInt = random.nextInt(0xffffff + 1);
								Circle obj = new Circle(Float.parseFloat(ar[0]), Float.parseFloat(ar[1]), 25,
										Paint.valueOf(String.format("#%06x", nextInt)));

								ScaleTransition sctr = new ScaleTransition(Duration.millis(500), obj);
								sctr.setFromX(0);
								sctr.setFromY(0);
								sctr.setToX(1);
								sctr.setToY(1);
								sctr.setCycleCount(1);
								sctr.setAutoReverse(true);
								str.getChildren().add(sctr);

								// obj.setStyle("-fx-border-color: #000000;");
								obj.setStroke(Paint.valueOf("Black"));
								obj.setStrokeWidth(1.0);
								obj.setOnMouseClicked(e -> {
									System.out.println(own);
								});
								obj.cursorProperty().set(Cursor.DEFAULT);
								objectGroup.getChildren().add(obj);
								objectsMap.put(Integer.parseInt(ar[2]), obj);
							});
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			new Thread(task).start();

			task.setOnSucceeded(s -> {
				System.out.println("Done");
				str.play();
			});

		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();
		}
	}
}