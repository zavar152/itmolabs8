package itmo.labs.zavar.gui;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
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

import itmo.labs.zavar.client.util.ParseCoordinates;
import itmo.labs.zavar.client.util.ParsePerson;
import itmo.labs.zavar.studygroup.FormOfEducation;
import itmo.labs.zavar.studygroup.StudyGroup;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.util.Duration;

public class ClientController implements Initializable {
	
	private final String[] nameMapping = new String[] { "id", "name", "coordinates", "creationDate",
			"studentsCount", "expelledStudents", "transferredStudents", "formOfEducation", "groupAdmin" };
	private ICsvBeanReader beanReader;
	private Stack<StudyGroup> stack = new Stack<StudyGroup>(); 
	
	@FXML
	private Tab tabBrowser, tabCommands, tabSettings;
	@FXML
	private AnchorPane browser;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private Label infoText, objectIdText, objectNameText, langText, darkmodeText;
	@FXML
	private Button showButton, updateObjButton, deleteObjButton;
	@FXML
	private TextField objectName, objectId;
	@FXML
	private ToggleButton darkButton;
	@FXML
	private SplitPane infoPane;
	@FXML
	private ComboBox<String> langComboBox;
	
	private HashMap<Long, Circle> objectsMap = new HashMap<Long, Circle>();
	private Group objectGroup = new Group();
	private ResourceBundle resources;
	
	@Override
    public void initialize(URL location, ResourceBundle res) 
    {	
		resources = res;
		
		langComboBox.getItems().addAll(Launcher.getLangs().keySet());
		
		langComboBox.setOnAction(e -> {
			String[] locale = Launcher.getLangs().get(langComboBox.getValue()).split("_");
			resources = ResourceBundle.getBundle("langs/lang", new Locale(locale[0], locale[1]));
			setupLanguage(resources);
		});
		
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
        
        //ResourceBundle bundle = ResourceBundle.getBundle("langs/lang", new Locale("en", "US"));
        //showButton.setText(bundle.getString("button.show"));
        
        darkButton.setOnMouseClicked(e -> {
        	if(!darkButton.isSelected()) {
        		darkButton.setText(resources.getString("button.enableDark"));
        		Launcher.getStage().getScene().getStylesheets().remove("css/darkTheme.css");
        	} else {
        		darkButton.setText(resources.getString("button.disableDark"));
        		Launcher.getStage().getScene().getStylesheets().add("css/darkTheme.css");
        	}
        });
        
		showButton.setOnMouseClicked(e -> {

			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {

					while (!Launcher.getClient().isConnected()) {
						Thread.sleep(100);
					}

					Launcher.getClient().executeCommand("login",
							new ReaderInputStream(new StringReader("zavar\n123456789"), StandardCharsets.UTF_8));

					PipedInputStream pin = Launcher.getClient().getDataInput();

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
										
										Circle circle = objectsMap.get(Long.parseLong(res[1]));
										
										ScaleTransition sctr = new ScaleTransition(Duration.millis(2000), circle.getParent());
								        sctr.setToX(0);
								        sctr.setToY(0);
								        sctr.setFromX(1);
								        sctr.setFromY(1);
								        sctr.setCycleCount(1);
								        sctr.setAutoReverse(true);
								        SequentialTransition deleteSt = new SequentialTransition();
								        deleteSt.getChildren().addAll(sctr);
										
										Platform.runLater(() -> {
											infoPane.setDisable(true);
											clearInfo();
											deleteSt.play();
											deleteSt.setOnFinished(e -> {
												objectGroup.getChildren().remove(circle.getParent());
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
				infoText.setText("Status: Connecting...");
				scrollPane.setDisable(true);
			});

			task.setOnSucceeded(s -> {
				infoText.setText("Status: Building objects...");
				Task<Void> task2 = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						try {
							Scanner sc = new Scanner(Launcher.getClient().getAnswerInput());
							while (sc.hasNext()) {
								if(sc.nextLine().equals("loginDone"))
									break;
							}
						Platform.runLater(() -> {
							prepareObjectBrowser();
						});
						} catch(Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				new Thread(task2).start();

				task2.setOnSucceeded(e2 -> {
					scrollPane.setDisable(false);
					infoText.setText("Status: Ready");
				});

			});

			new Thread(task).start();

		});
    }

	private StudyGroup generateStydyGroup(String s, String own) throws IOException {
		beanReader = new CsvBeanReader(new StringReader(s), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
		// beanReader.getHeader(true);
		StudyGroup temp;
		StudyGroup out = null;
		while ((temp = beanReader.read(StudyGroup.class, nameMapping, getReaderProcessors())) != null) {
			stack.push(temp);
			out = temp;//new String[] {temp.getCoordinates().getX() + "", temp.getCoordinates().getY()  + "", temp.getId() + "", temp.getStudentsCount() + ""};
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
			Launcher.getClient().executeCommand("show", new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8));
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					try {
						Scanner sc = new Scanner(Launcher.getClient().getAnswerInput());
						while (sc.hasNextLine()) {
							String a = sc.next();
							String[] m = a.split(",");
							String own = m[m.length - 1];
							String b = a.replace("," + own, "");
							if (a.contains("/-/"))
								break;
							System.out.println("Owner: " + own);
							System.out.println(b);
							StudyGroup ar = generateStydyGroup(b, own);
							Platform.runLater(() -> {
								byte[] l = own.getBytes();
								long seed = 0;
								for (int i = 0; i < l.length; i++)
									seed = seed + l[i];
								Random random = new Random(seed);
								int nextInt = random.nextInt(0xffffff + 1);
								Circle obj = new Circle();

								obj.setFill(Paint.valueOf(String.format("#%06x", nextInt)));
								obj.setRadius(25);
								
								Text text = new Text(ar.getId() + "");
								text.setBoundsType(TextBoundsType.VISUAL); 
								text.setMouseTransparent(true);
								StackPane stack = new StackPane();
								stack.getChildren().addAll(obj, text);
								
								stack.setLayoutX(ar.getCoordinates().getX());
								stack.setLayoutY(ar.getCoordinates().getY());
								
								ScaleTransition sctr = new ScaleTransition(Duration.millis(500), stack);
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
								obj.setOnMousePressed(event -> {
									if(event.getButton().equals(MouseButton.PRIMARY)) {
							        	for (Circle c : objectsMap.values()) {
											c.setStroke(Paint.valueOf("Black"));
										}
										obj.setStroke(Paint.valueOf("Red"));
										objectId.setText(ar.getId() + "");
										objectName.setText(ar.getName());
										infoPane.setDisable(false);
									}
								});
								obj.cursorProperty().set(Cursor.DEFAULT);
								objectGroup.getChildren().add(stack);
								objectsMap.put(ar.getId(), obj);
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
	
	private void setupLanguage(ResourceBundle resource) {
		tabBrowser.setText(resource.getString("tab.browser"));
		tabCommands.setText(resource.getString("tab.commands"));
		tabSettings.setText(resource.getString("tab.settings"));
		
		showButton.setText(resource.getString("button.show"));
		if(!darkButton.isSelected()) {
    		darkButton.setText(resources.getString("button.enableDark"));
    	} else {
    		darkButton.setText(resources.getString("button.disableDark"));
    	}
		updateObjButton.setText(resource.getString("button.update"));
		deleteObjButton.setText(resource.getString("button.delete"));
		
		objectIdText.setText(resource.getString("object.id"));
		objectNameText.setText(resource.getString("object.name"));
		
		langText.setText(resource.getString("text.lang"));
		darkmodeText.setText(resource.getString("text.darkmode"));
	}
	
	private void clearInfo() {
		objectId.clear();
		objectName.clear();
	}
}