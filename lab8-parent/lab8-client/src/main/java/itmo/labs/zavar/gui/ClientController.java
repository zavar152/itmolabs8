package itmo.labs.zavar.gui;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
	private Tab tabBrowser, tabCommands, tabSettings, tabTable, tabAccount;
	@FXML
	private AnchorPane browser, mainPane;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private Label infoText, objectIdText, objectNameText, langText, darkmodeText, loginInfoText, accountText, loginText, passwordText;
	@FXML
	private Button showButton, updateObjButton, deleteObjButton, loginButton, registerButton;
	@FXML
	private TextField objectName, objectId, loginField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private ToggleButton darkButton;
	@FXML
	private SplitPane infoPane, buttonPane;
	@FXML
	private ComboBox<String> langComboBox;
	@FXML
	private TableView<StudyGroup> objectTable;
	@FXML
	private TabPane tabPane;
	@FXML
	private TitledPane accountPane;
	
	private String login;
	private HashMap<Long, Circle> objectsMap = new HashMap<Long, Circle>();
	private Group objectGroup = new Group();
	private ResourceBundle resources;
	private TableColumn<StudyGroup, Long> idColumn = new TableColumn<>("%column.idColumn");
	private TableColumn<StudyGroup, String> owner = new TableColumn<>("%column.owner");
	private TableColumn<StudyGroup, String> nameColumn = new TableColumn<>("%column.nameColumn");
	private TableColumn<StudyGroup, Double> coordX = new TableColumn<>("%column.coordX");
	private TableColumn<StudyGroup, Float> coordY = new TableColumn<>("%column.coordY");
	private TableColumn<StudyGroup, LocalDate> date = new TableColumn<>("%column.date");
	private TableColumn<StudyGroup, Long> scCount = new TableColumn<>("%column.scCount");
	private TableColumn<StudyGroup, Integer> exCount = new TableColumn<>("%column.exCount");
	private TableColumn<StudyGroup, Long> trCount = new TableColumn<>("%column.trCount");
	private TableColumn<StudyGroup, FormOfEducation> form = new TableColumn<>("%column.form");
	private boolean isTriggersDone = false;
	
	@Override
    public void initialize(URL location, ResourceBundle res) 
    {	
		
		Launcher.getClient().getConnectedProperty().addListener(e -> {
			System.out.println(Launcher.getClient().getConnectedProperty().get());
			if(Launcher.getClient().getConnectedProperty().get()) {
				
			} else {
				try {
					Launcher.getClient().reconnect(true);
				} catch (InterruptedException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		
		objectTable.setVisible(false);
		resources = res;
		
		prepareTable();
		setupTableLanguage(resources);
		
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
        
        darkButton.setOnMouseClicked(e -> {
        	if(!darkButton.isSelected()) {
        		darkButton.setText(resources.getString("button.enableDark"));
        		Launcher.getStage().getScene().getStylesheets().remove("css/darkTheme.css");
        	} else {
        		darkButton.setText(resources.getString("button.disableDark"));
        		Launcher.getStage().getScene().getStylesheets().add("css/darkTheme.css");
        	}
        });
        
        /*deleteObjButton.setOnMouseClicked(e -> {
        	infoPane.setDisable(true);
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Launcher.getClient().executeCommand("remove_by_id " + objectId.getText(), new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8));
					return null;
				}
			};
			
			new Thread(task).start();
			
        });*/
        
        loginButton.setOnMouseClicked(e -> {

			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {

					while (!Launcher.getClient().isConnected()) {
						Thread.sleep(100);
					}

					if(loginField.getText().length() <= 4) {
						Platform.runLater(() -> {
							loginInfoText.setText("Login should be longer than 4 characters");
						});
						cancel();
					} else if(passwordField.getText().length() <= 8) {
						Platform.runLater(() -> {
							loginInfoText.setText("Password should be longer than 8 characters");
						});
						cancel();
					} else
						Launcher.getClient().executeCommand("login", new ReaderInputStream(new StringReader(loginField.getText() + "\n" + passwordField.getText()), StandardCharsets.UTF_8));

					return null;
				}
			};
			
			//Replacing
			/*
			 * 											for (int idx = 0; idx < objectTable.getItems().size(); idx++) {
											    YourData data = objectTable.getItems().get(idx);
											    if (data.getColumnOne().equals(textToCompare)) {
											    	objectTable.getItems().set(idx, someOtherData);
											       return;
											    }
											}
			 */
			
			task.setOnCancelled(e3 -> {
				infoText.setText("Status: Login failed");
			});
			
			task.setOnRunning(s -> {
				infoText.setText("Status: Connecting...");
				//scrollPane.setDisable(true);
			});

			task.setOnSucceeded(s -> {
				infoText.setText("Status: Building objects...");
				Task<Void> task2 = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						try {
							Scanner sc = new Scanner(Launcher.getClient().getAnswerInput());
							while (sc.hasNext()) {
								String f = sc.nextLine();
								if(f.equals("loginDone")) {
									Platform.runLater(() -> {
										loginInfoText.setText("");
										objectTable.getItems().clear();
										objectsMap.clear();
										objectGroup.getChildren().clear();
										login = loginField.getText();
										accountText.setText(login);
										prepareObjectBrowser();
										prepareTriggers();
										isTriggersDone = true;
									});
									break;
								} else {
									Platform.runLater(() -> {
										loginInfoText.setText(f);
									});
									cancel();
								}
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				new Thread(task2).start();

				task2.setOnSucceeded(e2 -> {
					//scrollPane.setDisable(false);
					infoText.setText("Status: Ready");
				});
				
				task2.setOnCancelled(e3 -> {
					infoText.setText("Status: Login failed");
				});
				
				task2.setOnFailed(e3 -> {
					infoText.setText("Status: Login failed");
				});

			});

			new Thread(task).start();

		});
        
        registerButton.setOnMouseClicked(e -> {

			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {

					while (!Launcher.getClient().isConnected()) {
						Thread.sleep(100);
					}

					if(loginField.getText().length() <= 4) {
						Platform.runLater(() -> {
							loginInfoText.setText("Login should be longer than 4 characters");
						});
						cancel();
					} else if(passwordField.getText().length() <= 8) {
						Platform.runLater(() -> {
							loginInfoText.setText("Password should be longer than 8 characters");
						});
						cancel();
					} else
						Launcher.getClient().executeCommand("register", new ReaderInputStream(new StringReader(loginField.getText() + "\n" + passwordField.getText()), StandardCharsets.UTF_8));

					return null;
				}
			};
        });
        
        
        
    }

	private void prepareTable() {
		
		objectTable.setRowFactory(tv -> {
		    TableRow<StudyGroup> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {

		        	StudyGroup clickedRow = row.getItem();
		            Circle c = objectsMap.get(clickedRow.getId());
		            c.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED, c.getCenterX(), c.getCenterY(), c.getCenterX(), c.getCenterY(), MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
		            
		            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		            selectionModel.select(tabBrowser);
		        }
		    });
		    return row;
		});
		
	    idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
	    owner.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getOwner()));
	    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
	    coordX.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Double>(data.getValue().getCoordinates().getX()));
	    coordY.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Float>(data.getValue().getCoordinates().getY()));
	    date.setCellValueFactory(data -> new ReadOnlyObjectWrapper<LocalDate>(data.getValue().getCreationLocalDate()));
	    scCount.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Long>(data.getValue().getStudentsCount()));
	    exCount.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Integer>(data.getValue().getExpelledStudents()));
	    trCount.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Long>(data.getValue().getTransferredStudents()));
	    form.setCellValueFactory(data -> new ReadOnlyObjectWrapper<FormOfEducation>(data.getValue().getFormOfEducation()));
	    
	    idColumn.setPrefWidth(50);
	    owner.setPrefWidth(70);
	    nameColumn.setPrefWidth(150);
		coordX.setPrefWidth(50);
		coordY.setPrefWidth(50);
		date.setPrefWidth(100);
		scCount.setPrefWidth(150);
		exCount.setPrefWidth(180);
		trCount.setPrefWidth(180);
		form.setPrefWidth(150);
	    
	    objectTable.getColumns().add(idColumn);
	    objectTable.getColumns().add(owner);
	    objectTable.getColumns().add(nameColumn);
	    objectTable.getColumns().add(coordX);
	    objectTable.getColumns().add(coordY);
	    objectTable.getColumns().add(date);
	    objectTable.getColumns().add(scCount);
	    objectTable.getColumns().add(exCount);
	    objectTable.getColumns().add(trCount);
	    objectTable.getColumns().add(form);
	    
	    objectTable.setOnMouseClicked(e -> {
	    
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
		out.setOwner(own);
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
										if(!ar.getOwner().equals(login))
											buttonPane.setDisable(true);
										else
											buttonPane.setDisable(false);
									}
								});
								obj.cursorProperty().set(Cursor.DEFAULT);
								objectGroup.getChildren().add(stack);
								objectTable.getItems().add(ar);
								objectsMap.put(ar.getId(), obj);
							});
						}
						objectTable.setVisible(true);
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
		tabTable.setText(resource.getString("tab.table"));
		tabAccount.setText(resource.getString("tab.account"));
		
		showButton.setText(resource.getString("button.show"));
		if(!darkButton.isSelected()) {
    		darkButton.setText(resources.getString("button.enableDark"));
    	} else {
    		darkButton.setText(resources.getString("button.disableDark"));
    	}
		updateObjButton.setText(resource.getString("button.update"));
		deleteObjButton.setText(resource.getString("button.delete"));
		registerButton.setText(resource.getString("button.register"));
		loginButton.setText(resource.getString("button.login"));
		
		objectIdText.setText(resource.getString("object.id"));
		objectNameText.setText(resource.getString("object.name"));
		
		langText.setText(resource.getString("text.lang"));
		darkmodeText.setText(resource.getString("text.darkmode"));
		
		accountPane.setText(resource.getString("account.text"));
		loginText.setText(resource.getString("account.login"));
		passwordText.setText(resource.getString("account.password"));
		
		setupTableLanguage(resource);
	}
	
	private void setupTableLanguage(ResourceBundle resource) {
		idColumn.setText(resource.getString("column.idColumn"));
		owner.setText(resource.getString("column.owner"));
		nameColumn.setText(resource.getString("column.nameColumn"));
		coordX.setText(resource.getString("column.coordX"));
		coordY.setText(resource.getString("column.coordY"));
		date.setText(resource.getString("column.date"));
		scCount.setText(resource.getString("column.scCount"));
		exCount.setText(resource.getString("column.exCount"));
		trCount.setText(resource.getString("column.trCount"));
		form.setText(resource.getString("column.form"));
	}
	
	private void clearInfo() {
		objectId.clear();
		objectName.clear();
	}
	
	private void prepareTriggers() {
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
								for (int idx = 0; idx < objectTable.getItems().size(); idx++) {
								    StudyGroup data = objectTable.getItems().get(idx);
								    if (data.getId().equals(Long.parseLong(res[1]))) {
								    	objectTable.getItems().remove(idx);
								       return;
								    }
								}
							});
						}
					}
				}
			}
		};

		new Thread(task).start();
	}
}