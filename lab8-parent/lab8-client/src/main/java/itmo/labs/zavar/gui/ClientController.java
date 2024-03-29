package itmo.labs.zavar.gui;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.function.Predicate;

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
import itmo.labs.zavar.exception.CommandException;
import itmo.labs.zavar.gui.util.GUIUtils;
import itmo.labs.zavar.studygroup.FormOfEducation;
import itmo.labs.zavar.studygroup.StudyGroup;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class ClientController implements Initializable {
	
	private final String[] nameMapping = new String[] { "id", "name", "coordinates", "creationDate",
			"studentsCount", "expelledStudents", "transferredStudents", "formOfEducation", "groupAdmin" };
	private ICsvBeanReader beanReader;
	private ArrayList<StudyGroup> stList = new ArrayList<StudyGroup>(); 
	
	@FXML
	private Tab tabBrowser, tabCommands, tabSettings, tabTable, tabAccount;
	@FXML
	private AnchorPane browser, mainPane;
	@FXML
	private ScrollPane scrollPane, objectScrollPane;
	@FXML
	private Label infoText, objectNameText, langText, darkmodeText, loginInfoText, accountText, loginText, passwordText,
	objectIdText, objectOwnerText, objectXText, objectYText, objectDateText, objectScText, objectEsText, objectTsText,
	objectFoEText, objectAdmNameText, objectAdmPassText, objectAdmEyeText, objectAdmHairText, objectAdmCountryText,
	objectAdmXText, objectAdmYText, objectAdmZText, objectAdmLocNameText, 
	objectFilterNameText, objectFilterIdText, objectFilterOwnerText, objectFilterXText, objectFilterYText, objectFilterDateText, objectFilterScText, objectFilterEsText, objectFilterTsText,
	objectFilterFoEText, objectFilterAdmNameText, objectFilterAdmPassText, objectFilterAdmEyeText, objectFilterAdmHairText, objectFilterAdmCountryText,
	objectFilterAdmXText, objectFilterAdmYText, objectFilterAdmZText, objectFilterAdmLocNameText, outputText;
	@FXML
	private Button updateObjButton, deleteObjButton, loginButton, registerButton, resetButton,
	clearButton, infoButton, averageOfTsButton, countGTsButton, removeByScButton, executeButton,
	addButton, addMinButton, addMaxButton;
	@FXML
	private TextField objectName, objectId, loginField, objectOwner, objectX, objectY, objectDate, objectSc, objectEs,
	objectTs, objectFoE, objectAdmName, objectAdmPass, objectAdmEye, objectAdmHair, objectAdmCountry, objectAdmX,
	objectAdmY, objectAdmZ, objectAdmLocName,
	objectFilterName, objectFilterId, objectFilterOwner, objectFilterX, objectFilterY, objectFilterDate, objectFilterSc, objectFilterEs,
	objectFilterTs, objectFilterFoE, objectFilterAdmName, objectFilterAdmPass, objectFilterAdmEye, objectFilterAdmHair, objectFilterAdmCountry, objectFilterAdmX,
	objectFilterAdmY, objectFilterAdmZ, objectFilterAdmLocName;
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
	@FXML
	private VBox objectAdminBox;
	@FXML
	private TextArea outputField;
	@FXML
	private GridPane commandsGridPane;
	
	private String login;
	private HashMap<Long, Circle> objectsMap = new HashMap<Long, Circle>();
	private FilteredList<StudyGroup> filterList;
	private ObservableList<StudyGroup> obsList;
	private Group objectGroup = new Group();
	private ResourceBundle resources;
	private TableColumn<StudyGroup, Long> idColumn = new TableColumn<>("%column.idColumn");
	private TableColumn<StudyGroup, String> ownerColumn = new TableColumn<>("%column.owner");
	private TableColumn<StudyGroup, String> nameColumn = new TableColumn<>("%column.nameColumn");
	private TableColumn<StudyGroup, Double> coordXColumn = new TableColumn<>("%column.coordX");
	private TableColumn<StudyGroup, Float> coordYColumn = new TableColumn<>("%column.coordY");
	private TableColumn<StudyGroup, String> dateColumn = new TableColumn<>("%column.date");
	private TableColumn<StudyGroup, Long> scCountColumn = new TableColumn<>("%column.scCount");
	private TableColumn<StudyGroup, Integer> exCountColumn = new TableColumn<>("%column.exCount");
	private TableColumn<StudyGroup, Long> trCountColumn = new TableColumn<>("%column.trCount");
	private TableColumn<StudyGroup, FormOfEducation> formColumn = new TableColumn<>("%column.form");
	
	private TableColumn<StudyGroup, String> adminNameColumn = new TableColumn<>("%column.admname");
	private TableColumn<StudyGroup, String> adminPassColumn = new TableColumn<>("%column.admpass");
	private TableColumn<StudyGroup, String> adminEyeColumn = new TableColumn<>("%column.admeye");
	private TableColumn<StudyGroup, String> adminHairColumn = new TableColumn<>("%column.admhair");
	private TableColumn<StudyGroup, String> adminCountryColumn = new TableColumn<>("%column.admcountry");
	private TableColumn<StudyGroup, String> adminXColumn = new TableColumn<>("%column.admx");
	private TableColumn<StudyGroup, String> adminYColumn = new TableColumn<>("%column.admy");
	private TableColumn<StudyGroup, String> adminZColumn = new TableColumn<>("%column.admz");
	private TableColumn<StudyGroup, String> adminLocNameColumn = new TableColumn<>("%column.admlocname");
	@SuppressWarnings("unused")
	private boolean isTriggersDone = false;
	private Long selected = -1L;
	@SuppressWarnings("unused")
	private NumberFormat numberFormat;
	private DateTimeFormatter dateFormat;
	
	@Override
    public void initialize(URL location, ResourceBundle res) 
    {	
		tabCommands.setDisable(true);
		objectTable.setVisible(false);
		resources = res;
		
		prepareTable();
		setupTableLanguage(resources);
		
		langComboBox.setValue(Launcher.getInvLangs().get(Launcher.getLang()));
		
		String[] locStart = Launcher.getLangs().get(langComboBox.getValue()).split("_");
		Locale lStart = new Locale(locStart[0], locStart[1]);
		dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", lStart);
		
		if(Launcher.getTheme() == 1) {
    		darkButton.setText(resources.getString("button.disableDark"));
    		darkButton.setSelected(true);
    	} else {
    		darkButton.setText(resources.getString("button.enableDark"));
    	}
		
		Launcher.getClient().getConnectedProperty().addListener(e -> {
			System.out.println(Launcher.getClient().getConnectedProperty().get());
			if(Launcher.getClient().getConnectedProperty().get()) {
				
			} else {
				if (!Launcher.isStop()) {
					mainPane.setDisable(true);

					Platform.runLater(() -> {
						GUIUtils.showError("Server connection is lost! You should wait.");
						login = "";
						accountText.setText("");
						if(obsList != null)
							obsList.clear();
						stList.clear();
						objectsMap.clear();
						objectGroup.getChildren().clear();
					});

					try {
						Launcher.getClient().reconnect(true);
						mainPane.setDisable(false);
					} catch (InterruptedException | IOException e1) {
						GUIUtils.showAndWaitError(e1);
					}
				}
			}
		});
		
		langComboBox.getItems().addAll(Launcher.getLangs().keySet());
		
		langComboBox.setOnAction(e -> {
			String[] locale = Launcher.getLangs().get(langComboBox.getValue()).split("_");
			Locale l = new Locale(locale[0], locale[1]);
			numberFormat = NumberFormat.getInstance(l);
			dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", l);
			resources = ResourceBundle.getBundle("langs/lang", l, new GUIUtils.UTF8Control());
			Launcher.setLang(locale[0] + "_" + locale[1]);
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
        		Launcher.getStage().getScene().getStylesheets().clear();
        		Launcher.setTheme(0);
        	} else {
        		darkButton.setText(resources.getString("button.disableDark"));
        		Launcher.getStage().getScene().getStylesheets().add("css/darkTheme.css");
        		Launcher.setTheme(1);
        	}
        });
        
        
        updateObjButton.setOnMouseClicked(e -> {
			Task<Void> updateTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					StudyGroup st = stList.stream().filter(t -> t.getId().equals(selected)).findAny().get();
					String res;
					if(st.getGroupAdmin() == null) {
						res = "1\n" + objectName.getText() + "\n" + objectX.getText() + "\n" + objectY.getText() + "\n" + objectSc.getText() + "\n" +
								objectEs.getText() + "\n" + objectTs.getText() + "\n" + objectFoE.getText() + "\nNO\n";
					} else {
						res = "1\n" + objectName.getText() + "\n" + objectX.getText() + "\n" + objectY.getText() + "\n" + objectSc.getText() + "\n" +
								objectEs.getText() + "\n" + objectTs.getText() + "\n" + objectFoE.getText() + "\nYES\n" + objectAdmName.getText()
								+ "\n" + objectAdmPass.getText() + "\n" + objectAdmEye.getText() + "\n" + objectAdmHair.getText() + "\n" +
								objectAdmCountry.getText() + "\n" + objectAdmLocName.getText() + "\n" + objectAdmX.getText()
								 + "\n" + objectAdmY.getText() + "\n" + objectAdmZ.getText();
					}
						
					Launcher.getClient().executeCommand("update " + objectId.getText(), new ReaderInputStream(new StringReader(res), StandardCharsets.UTF_8), System.out);
					return null;
				}
			};
			
			new Thread(updateTask).start();
			
			updateTask.setOnSucceeded(e1 -> {
				Task<Void> updateAnswerTask = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						try {
							Scanner sc = new Scanner(Launcher.getClient().getAnswerInput());
							while (sc.hasNextLine()) {
								String f = sc.nextLine();
								System.out.println("s_" + f);
								if(f.equals("****"))
									break;
								if (!f.isEmpty()) {
									Platform.runLater(() -> {
										infoText.setText("Status: " + f);
									});
								}
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				new Thread(updateAnswerTask).start();
			});
        });
        
        
        deleteObjButton.setOnMouseClicked(e -> {
        	infoPane.setDisable(true);
			Task<Void> removeTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Launcher.getClient().executeCommand("remove_by_id " + objectId.getText(), new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8), System.out);
					return null;
				}
			};
			
			new Thread(removeTask).start();
			
			removeTask.setOnSucceeded(e1 -> {
				Task<Void> removeAnswerTask = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						try {
							Scanner sc = new Scanner(Launcher.getClient().getAnswerInput());
							while (sc.hasNextLine()) {
								String f = sc.nextLine();
								System.out.println(f);
								if(f.equals("****"))
									break;
								if (!f.isEmpty()) {
									Platform.runLater(() -> {
										infoText.setText("Status: " + f);
									});
								}
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				new Thread(removeAnswerTask).start();
			});
			
        });
        
        loginButton.setOnMouseClicked(e -> {

			Task<Void> loginTask = new Task<Void>() {
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
						Launcher.getClient().executeCommand("login", new ReaderInputStream(new StringReader(loginField.getText() + "\n" + passwordField.getText()), StandardCharsets.UTF_8), System.out);

					return null;
				}
			};
			
			loginTask.setOnCancelled(e3 -> {
				infoText.setText("Status: Login failed");
			});
			
			loginTask.setOnRunning(s -> {
				infoText.setText("Status: Connecting...");
			});

			loginTask.setOnSucceeded(s -> {
				infoText.setText("Status: Building objects...");
				Task<Void> loginAnswerTask = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						try {
							Scanner sc = new Scanner(Launcher.getClient().getAnswerInput());
							while (sc.hasNextLine()) {
								String f = sc.nextLine();
								if(f.equals("loginDone")) {
									Platform.runLater(() -> {
										loginInfoText.setText("");
										if(obsList != null)
											obsList.clear();
										stList.clear();
										objectsMap.clear();
										objectGroup.getChildren().clear();
										login = loginField.getText();
										accountText.setText(login);
										prepareObjectBrowser();
										prepareTriggers();
										isTriggersDone = true;
										tabCommands.setDisable(false);
									});
									break;
								} else {
									Platform.runLater(() -> {
										if(!f.equals("****")) {
											loginInfoText.setText(f);
										}
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
				new Thread(loginAnswerTask).start();

				loginAnswerTask.setOnSucceeded(e2 -> {
					infoText.setText("Status: Ready");
				});
				
				loginAnswerTask.setOnCancelled(e3 -> {
					infoText.setText("Status: Login failed");
				});
				
				loginAnswerTask.setOnFailed(e3 -> {
					infoText.setText("Status: Login failed");
				});

			});

			new Thread(loginTask).start();

		});
        
        registerButton.setOnMouseClicked(e -> {

			Task<Void> registerTask = new Task<Void>() {
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
						Launcher.getClient().executeCommand("register", new ReaderInputStream(new StringReader(loginField.getText() + "\n" + passwordField.getText()), StandardCharsets.UTF_8), System.out);

					return null;
				}
			};
			
			new Thread(registerTask).start();
			
			registerTask.setOnSucceeded(e1 -> {
				Task<Void> registerAnswerTask = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						try {
							Scanner sc = new Scanner(Launcher.getClient().getAnswerInput());
							while (sc.hasNextLine()) {
								String f = sc.nextLine();
								if(f.equals("****"))
									break;
								Platform.runLater(() -> {
									loginInfoText.setText(f);
								});
								
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				new Thread(registerAnswerTask).start();
			});
        });
        
        setDefaultCommandsButton(averageOfTsButton, "average_of_transferred_students");
        setDefaultCommandsButton(infoButton, "info");
        setArgsCommandsButton(countGTsButton, "count_greater_than_transferred_students");
        setArgsCommandsButton(removeByScButton, "remove_any_by_students_count");
        setWarningCommandButton(clearButton, "clear");
        setExecuteCommandButton();
        setAddCommandButton(addButton, "add");
        setAddCommandButton(addMaxButton, "add_if_max");
        setAddCommandButton(addMinButton, "add_if_min");
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
	    ownerColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getOwner()));
	    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
	    coordXColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Double>(data.getValue().getCoordinates().getX()));
	    coordYColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Float>(data.getValue().getCoordinates().getY()));
	    dateColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getCreationLocalDate().format(dateFormat)));
	    scCountColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Long>(data.getValue().getStudentsCount()));
	    exCountColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Integer>(data.getValue().getExpelledStudents()));
	    trCountColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<Long>(data.getValue().getTransferredStudents()));
	    formColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<FormOfEducation>(data.getValue().getFormOfEducation()));
	    
	    adminNameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getGroupAdmin() != null ? data.getValue().getGroupAdmin().getName() : ""));
	    adminPassColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getGroupAdmin() != null ? data.getValue().getGroupAdmin().getPassportID() : ""));
	    adminEyeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getGroupAdmin() != null ? data.getValue().getGroupAdmin().getEyeColor().toString() : ""));
	    adminHairColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getGroupAdmin() != null ? data.getValue().getGroupAdmin().getHairColor().toString() : ""));
	    adminCountryColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getGroupAdmin() != null ? data.getValue().getGroupAdmin().getNationality().toString() : ""));
	    adminXColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getGroupAdmin() != null ? data.getValue().getGroupAdmin().getLocation().getX() + "" : ""));
	    adminYColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getGroupAdmin() != null ? data.getValue().getGroupAdmin().getLocation().getY() + ""  : ""));
	    adminZColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getGroupAdmin() != null ? data.getValue().getGroupAdmin().getLocation().getZ() + ""  : ""));
	    adminLocNameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getGroupAdmin() != null ? data.getValue().getGroupAdmin().getLocation().getName() : ""));
	    
	    idColumn.setPrefWidth(50);
	    ownerColumn.setPrefWidth(70);
	    nameColumn.setPrefWidth(150);
		coordXColumn.setPrefWidth(50);
		coordYColumn.setPrefWidth(50);
		dateColumn.setPrefWidth(150);
		scCountColumn.setPrefWidth(150);
		exCountColumn.setPrefWidth(180);
		trCountColumn.setPrefWidth(180);
		formColumn.setPrefWidth(150);
		adminNameColumn.setPrefWidth(100);
		adminPassColumn.setPrefWidth(100);
	    adminEyeColumn.setPrefWidth(100);
	    adminHairColumn.setPrefWidth(100);
	    adminCountryColumn.setPrefWidth(100);
	    adminXColumn.setPrefWidth(150);
	    adminYColumn.setPrefWidth(150);
	    adminZColumn.setPrefWidth(150);
	    adminLocNameColumn.setPrefWidth(150);

		
		
	    objectTable.getColumns().add(idColumn);
	    objectTable.getColumns().add(ownerColumn);
	    objectTable.getColumns().add(nameColumn);
	    objectTable.getColumns().add(coordXColumn);
	    objectTable.getColumns().add(coordYColumn);
	    objectTable.getColumns().add(dateColumn);
	    objectTable.getColumns().add(scCountColumn);
	    objectTable.getColumns().add(exCountColumn);
	    objectTable.getColumns().add(trCountColumn);
	    objectTable.getColumns().add(formColumn);
	    
	    objectTable.getColumns().add(adminNameColumn);
	    objectTable.getColumns().add(adminPassColumn);
	    objectTable.getColumns().add(adminEyeColumn);
	    objectTable.getColumns().add(adminHairColumn);
	    objectTable.getColumns().add(adminCountryColumn);
	    objectTable.getColumns().add(adminXColumn);
	    objectTable.getColumns().add(adminYColumn);
	    objectTable.getColumns().add(adminZColumn);
	    objectTable.getColumns().add(adminLocNameColumn);
	}

	private StudyGroup generateStydyGroup(String s, String own) throws IOException {
		beanReader = new CsvBeanReader(new StringReader(s), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
		StudyGroup temp;
		StudyGroup out = null;
		while ((temp = beanReader.read(StudyGroup.class, nameMapping, getReaderProcessors())) != null) {
			stList.add(temp);
			out = temp;
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
		
		resetButton.setOnMouseClicked(e -> {
			objectFilterId.clear();
			objectFilterName.clear();
			objectFilterOwner.clear();
			objectFilterX.clear();
			objectFilterY.clear();
			objectFilterDate.clear();
			objectFilterSc.clear();
			objectFilterEs.clear();
			objectFilterTs.clear();
			objectFilterFoE.clear();
			objectFilterAdmName.clear();
			objectFilterAdmPass.clear();
			objectFilterAdmEye.clear();
			objectFilterAdmHair.clear();
			objectFilterAdmCountry.clear();
			objectFilterAdmX.clear();
			objectFilterAdmY.clear();
			objectFilterAdmZ.clear();
			objectFilterAdmLocName.clear();
		});
		
		ParallelTransition parAnimation = new ParallelTransition();
		try {
			try {
				Launcher.getClient().executeCommand("show", new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8), System.out);
			} catch (CommandException e1) {
				GUIUtils.showAndWaitError(e1);
			}
			Task<Void> loadBrowserTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					try {
						Scanner sc = new Scanner(Launcher.getClient().getAnswerInput());
						while (sc.hasNextLine()) {
							String line = sc.next();
							String[] m = line.split(",");
							String own = m[m.length - 1];
							String b = line.replace("," + own, "");
							if (line.contains("/-/") || line.contains("****"))
								break;
							System.out.println("Owner: " + own);
							System.out.println(b);
							StudyGroup studyGroup = generateStydyGroup(b, own);
							createObject(studyGroup, parAnimation);
						}
						obsList = FXCollections.observableArrayList(stList);
						filterList = new FilteredList<StudyGroup>(obsList);
						objectTable.setItems(filterList);
						Predicate<StudyGroup> stFilter = new Predicate<StudyGroup>() {
							@Override
							public boolean test(StudyGroup t) {
								return new String(t.getId() + "").contains(objectFilterId.getText()) && t.getName().contains(objectFilterName.getText()) && t.getOwner().contains(objectFilterOwner.getText()) 
										&& new String(t.getCoordinates().getX() + "").contains(objectFilterX.getText()) && new String(t.getCoordinates().getY() + "").contains(objectFilterY.getText())
										&& t.getCreationLocalDate().format(dateFormat).contains(objectFilterDate.getText()) && new String(t.getStudentsCount() + "").contains(objectFilterSc.getText())
										&& new String(t.getExpelledStudents() + "").contains(objectFilterEs.getText()) && new String(t.getTransferredStudents() + "").contains(objectFilterTs.getText())
										&& new String(t.getFormOfEducation() + "").contains(objectFilterFoE.getText()) && (t.getGroupAdmin() == null ? true : (
												t.getGroupAdmin().getName().contains(objectFilterAdmName.getText()) && t.getGroupAdmin().getPassportID().contains(objectFilterAdmPass.getText()) 
												&& t.getGroupAdmin().getEyeColor().toString().contains(objectFilterAdmEye.getText()) && t.getGroupAdmin().getHairColor().toString().contains(objectFilterAdmHair.getText())
												&& t.getGroupAdmin().getNationality().toString().contains(objectFilterAdmCountry.getText()) && (t.getGroupAdmin().getLocation().getX() + "").contains(objectFilterAdmX.getText())
												&& (t.getGroupAdmin().getLocation().getY() + "").contains(objectFilterAdmY.getText()) && (t.getGroupAdmin().getLocation().getZ() + "").contains(objectFilterAdmZ.getText())
												&& t.getGroupAdmin().getLocation().getName().contains(objectFilterAdmLocName.getText())));
							}
						};
						filterList.predicateProperty().bind(Bindings.createObjectBinding(() -> stFilter, objectFilterId.textProperty(), objectFilterName.textProperty(), objectFilterOwner.textProperty(), 
								objectFilterX.textProperty(), objectFilterY.textProperty(), objectFilterDate.textProperty(),
								objectFilterSc.textProperty(), objectFilterEs.textProperty(), objectFilterTs.textProperty(), objectFilterFoE.textProperty(), objectFilterAdmName.textProperty(), 
								objectFilterAdmPass.textProperty(), objectFilterAdmEye.textProperty(), objectFilterAdmHair.textProperty(), objectFilterAdmCountry.textProperty(), objectFilterAdmX.textProperty(),
								objectFilterAdmY.textProperty(), objectFilterAdmZ.textProperty(), objectFilterAdmLocName.textProperty()));
						objectTable.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			new Thread(loadBrowserTask).start();

			loadBrowserTask.setOnSucceeded(s -> {
				System.out.println("Done");
				parAnimation.play();
			});

		} catch (InterruptedException | IOException e1) {
			GUIUtils.showAndWaitError(e1);
		}
	}
	
	private void setupLanguage(ResourceBundle resource) {
		
		objectTable.getColumns().clear();
		dateColumn = new TableColumn<>("%column.date");
		dateColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<String>(data.getValue().getCreationLocalDate().format(dateFormat)));
	    objectTable.getColumns().add(idColumn);
	    objectTable.getColumns().add(ownerColumn);
	    objectTable.getColumns().add(nameColumn);
	    objectTable.getColumns().add(coordXColumn);
	    objectTable.getColumns().add(coordYColumn);
	    objectTable.getColumns().add(dateColumn);
	    objectTable.getColumns().add(scCountColumn);
	    objectTable.getColumns().add(exCountColumn);
	    objectTable.getColumns().add(trCountColumn);
	    objectTable.getColumns().add(formColumn);
	    
	    objectTable.getColumns().add(adminNameColumn);
	    objectTable.getColumns().add(adminPassColumn);
	    objectTable.getColumns().add(adminEyeColumn);
	    objectTable.getColumns().add(adminHairColumn);
	    objectTable.getColumns().add(adminCountryColumn);
	    objectTable.getColumns().add(adminXColumn);
	    objectTable.getColumns().add(adminYColumn);
	    objectTable.getColumns().add(adminZColumn);
	    objectTable.getColumns().add(adminLocNameColumn);
		
		if(selected != -1L)
		{
			Circle c = objectsMap.get(selected);
        	c.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED, c.getCenterX(), c.getCenterY(), c.getCenterX(), c.getCenterY(), MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
		}
		
		tabBrowser.setText(resource.getString("tab.browser"));
		tabCommands.setText(resource.getString("tab.commands"));
		tabSettings.setText(resource.getString("tab.settings"));
		tabTable.setText(resource.getString("tab.table"));
		tabAccount.setText(resource.getString("tab.account"));
		
		if(!darkButton.isSelected()) {
    		darkButton.setText(resources.getString("button.enableDark"));
    	} else {
    		darkButton.setText(resources.getString("button.disableDark"));
    	}
		updateObjButton.setText(resource.getString("button.update"));
		deleteObjButton.setText(resource.getString("button.delete"));
		registerButton.setText(resource.getString("button.register"));
		loginButton.setText(resource.getString("button.login"));
		resetButton.setText(resource.getString("button.reset"));
		
		objectIdText.setText(resource.getString("object.id"));
		objectNameText.setText(resource.getString("object.name"));
		objectOwnerText.setText(resource.getString("object.owner"));
		objectXText.setText(resource.getString("object.x"));
		objectYText.setText(resource.getString("object.y"));
		objectScText.setText(resource.getString("object.sc"));
		objectEsText.setText(resource.getString("object.es"));
		objectTsText.setText(resource.getString("object.ts"));
		objectFoEText.setText(resource.getString("object.foe"));
		objectDateText.setText(resource.getString("object.date"));
		objectAdmNameText.setText(resource.getString("object.admname"));
		objectAdmCountryText.setText(resource.getString("object.admcountry"));
		objectAdmEyeText.setText(resource.getString("object.admeye"));
		objectAdmHairText.setText(resource.getString("object.admhair"));
		objectAdmXText.setText(resource.getString("object.admx"));
		objectAdmYText.setText(resource.getString("object.admy"));
		objectAdmZText.setText(resource.getString("object.admz"));
		objectAdmPassText.setText(resource.getString("object.admpass"));
		objectAdmLocNameText.setText(resource.getString("object.admlocname"));
		
		objectFilterIdText.setText(resource.getString("object.id"));
		objectFilterNameText.setText(resource.getString("object.name"));
		objectFilterOwnerText.setText(resource.getString("object.owner"));
		objectFilterXText.setText(resource.getString("object.x"));
		objectFilterYText.setText(resource.getString("object.y"));
		objectFilterScText.setText(resource.getString("object.sc"));
		objectFilterEsText.setText(resource.getString("object.es"));
		objectFilterTsText.setText(resource.getString("object.ts"));
		objectFilterFoEText.setText(resource.getString("object.foe"));
		objectFilterDateText.setText(resource.getString("object.date"));
		objectFilterAdmNameText.setText(resource.getString("object.admname"));
		objectFilterAdmCountryText.setText(resource.getString("object.admcountry"));
		objectFilterAdmEyeText.setText(resource.getString("object.admeye"));
		objectFilterAdmHairText.setText(resource.getString("object.admhair"));
		objectFilterAdmXText.setText(resource.getString("object.admx"));
		objectFilterAdmYText.setText(resource.getString("object.admy"));
		objectFilterAdmZText.setText(resource.getString("object.admz"));
		objectFilterAdmPassText.setText(resource.getString("object.admpass"));
		objectFilterAdmLocNameText.setText(resource.getString("object.admlocname"));
		
		langText.setText(resource.getString("text.lang"));
		darkmodeText.setText(resource.getString("text.darkmode"));
		
		accountPane.setText(resource.getString("account.text"));
		loginText.setText(resource.getString("account.login"));
		passwordText.setText(resource.getString("account.password"));
		outputText.setText(resource.getString("output"));
		
		setupTableLanguage(resource);
	}
	
	private void setupTableLanguage(ResourceBundle resource) {
		idColumn.setText(resource.getString("column.idColumn"));
		ownerColumn.setText(resource.getString("column.owner"));
		nameColumn.setText(resource.getString("column.nameColumn"));
		coordXColumn.setText(resource.getString("column.coordX"));
		coordYColumn.setText(resource.getString("column.coordY"));
		dateColumn.setText(resource.getString("column.date"));
		scCountColumn.setText(resource.getString("column.scCount"));
		exCountColumn.setText(resource.getString("column.exCount"));
		trCountColumn.setText(resource.getString("column.trCount"));
		formColumn.setText(resource.getString("column.form"));
	    adminNameColumn.setText(resource.getString("column.admname"));
	    adminPassColumn.setText(resource.getString("column.admpass"));
	    adminEyeColumn.setText(resource.getString("column.admeye"));
	    adminHairColumn.setText(resource.getString("column.admhair"));
	    adminXColumn.setText(resource.getString("column.admx"));
	    adminYColumn.setText(resource.getString("column.admy"));
	    adminZColumn.setText(resource.getString("column.admz"));
	    adminLocNameColumn.setText(resource.getString("column.admlocname"));
	    adminCountryColumn.setText(resource.getString("column.admcountry"));
	}
	
	private void prepareTriggers() {
		ParallelTransition parAnimation = new ParallelTransition();
		PipedInputStream pin = Launcher.getClient().getDataInput();

		Task<Void> triggersTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
				@SuppressWarnings("resource")
				Scanner sc = new Scanner(pin);
				while (true) {
					if (sc.hasNextLine()) {
						String line = sc.next();
						if(line.equals("****"))
							continue;
						System.out.println(line);
						String[] request = line.split(";");
						if (request[0].equals("DELETE")) {
							
							Circle circle = null;
							while(circle == null)
								circle = objectsMap.get(Long.parseLong(request[1]));
							
							ScaleTransition circleScale = new ScaleTransition(Duration.millis(2000), circle.getParent());
					        circleScale.setToX(0);
					        circleScale.setToY(0);
					        circleScale.setFromX(1);
					        circleScale.setFromY(1);
					        circleScale.setCycleCount(1);
					        circleScale.setAutoReverse(true);
					        SequentialTransition deleteSt = new SequentialTransition();
					        deleteSt.getChildren().addAll(circleScale);
							
					        Circle locCircle = circle;
							Platform.runLater(() -> {
								if(selected.equals(Long.parseLong(request[1]))) {
									infoPane.setDisable(true);
									for (Circle c : objectsMap.values()) {
										c.setStroke(Paint.valueOf("Black"));
									}
									clearObjectInfo();
								}
								deleteSt.play();
								deleteSt.setOnFinished(e -> {
									objectGroup.getChildren().remove(locCircle.getParent());
								});
								for (int idx = 0; idx < obsList.size(); idx++) {
								    StudyGroup data = obsList.get(idx);
								    if (data.getId().equals(Long.parseLong(request[1]))) {
								    	obsList.remove(idx);
								    	objectsMap.remove(Long.parseLong(request[1]));
								    	return;
								    }
								}
							});
						} else if(request[0].equals("INSERT")) {								
							String line2 = line.replace("INSERT;", "");
							String[] m = line2.split(",");
							String own = m[m.length - 1];
							String b = line2.replace("," + own, "");
							StudyGroup st = generateStydyGroup(b, own);
							createObject(st, parAnimation);
							obsList.add(st);
						} else if(request[0].equals("UPDATE")) {
							String line2 = line.replace("UPDATE;", "");
							String[] m = line2.split(",");
							String own = m[m.length - 1];
							String b = line2.replace("," + own, "");
							StudyGroup st = generateStydyGroup(b, own);
							StudyGroup replace = obsList.stream().filter(g -> g.getId().equals(st.getId())).findAny().get();
							if(selected.equals(st.getId())) {
								updateObjectInfo(st);
							}
							obsList.remove(replace);
							Circle cir = objectsMap.get(st.getId());
							cir.getParent().setLayoutX(st.getCoordinates().getX());
							cir.getParent().setLayoutY(st.getCoordinates().getY());
							obsList.add(st);
						} else if(request[0].equals("TRUNCATE")) {
							
							Iterator<Long> iter = objectsMap.keySet().iterator();
							
								while (iter.hasNext()) {
									Long id = iter.next();
									Circle circle = null;
									while (circle == null)
										circle = objectsMap.get(id);

									ScaleTransition circleScale = new ScaleTransition(Duration.millis(2000), circle.getParent());
									circleScale.setToX(0);
									circleScale.setToY(0);
									circleScale.setFromX(1);
									circleScale.setFromY(1);
									circleScale.setCycleCount(1);
									circleScale.setAutoReverse(true);
									SequentialTransition deleteSt = new SequentialTransition();
									deleteSt.getChildren().addAll(circleScale);

									Circle locCircle = circle;
									Platform.runLater(() -> {
										if (selected.equals(id)) {
											infoPane.setDisable(true);
											for (Circle c : objectsMap.values()) {
												c.setStroke(Paint.valueOf("Black"));
											}
											clearObjectInfo();
										}
										deleteSt.play();
										deleteSt.setOnFinished(e -> {
											objectGroup.getChildren().remove(locCircle.getParent());
										});
										for (int idx = 0; idx < obsList.size(); idx++) {
											StudyGroup data = obsList.get(idx);
											if (data.getId().equals(id)) {
												obsList.remove(idx);
												return;
											}
										}
									});
								}
								objectsMap.clear();
						}
					}
				}
				} catch(Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};

		new Thread(triggersTask).start();
	}
	
	private void createObject(StudyGroup studyGroup, ParallelTransition parAnimation) {
		Platform.runLater(() -> {
			byte[] ownBytes = studyGroup.getOwner().getBytes();
			long seed = 0;
			for (int i = 0; i < ownBytes.length; i++)
				seed = seed + ownBytes[i];
			Random random = new Random(seed);
			int nextInt = random.nextInt(0xffffff + 1);
			Circle circle = new Circle();

			circle.setFill(Paint.valueOf(String.format("#%06x", nextInt)));
			circle.setRadius(25);
			
			Text idText = new Text(studyGroup.getId() + "");
			idText.setBoundsType(TextBoundsType.VISUAL); 
			idText.setMouseTransparent(true);
			StackPane stackObject = new StackPane();
			stackObject.getChildren().addAll(circle, idText);
			
			stackObject.setLayoutX(studyGroup.getCoordinates().getX());
			stackObject.setLayoutY(studyGroup.getCoordinates().getY());
			
			ScaleTransition circleScale = new ScaleTransition(Duration.millis(500), stackObject);
			circleScale.setFromX(0);
			circleScale.setFromY(0);
			circleScale.setToX(1);
			circleScale.setToY(1);
			circleScale.setCycleCount(1);
			circleScale.setAutoReverse(true);
			parAnimation.getChildren().clear();
			parAnimation.getChildren().add(circleScale);

			circle.setStroke(Paint.valueOf("Black"));
			circle.setStrokeWidth(1.0);
			circle.setOnMousePressed(event -> {
				if(event.getButton().equals(MouseButton.PRIMARY)) {
					selected = studyGroup.getId();
		        	for (Circle c : objectsMap.values()) {
						c.setStroke(Paint.valueOf("Black"));
					}
					circle.setStroke(Paint.valueOf("Red"));
					objectScrollPane.setVvalue(0);
					objectId.setText(studyGroup.getId() + "");
					objectName.setText(studyGroup.getName());
					objectOwner.setText(studyGroup.getOwner());
					objectX.setText(studyGroup.getCoordinates().getX() + "");
					objectY.setText(studyGroup.getCoordinates().getY() + "");
					objectSc.setText(studyGroup.getStudentsCount() + "");
					objectEs.setText(studyGroup.getExpelledStudents() + "");
					objectTs.setText(studyGroup.getTransferredStudents() + "");
					objectFoE.setText(studyGroup.getFormOfEducation().toString());
					objectDate.setText(studyGroup.getCreationLocalDate().format(dateFormat));
					if(studyGroup.getGroupAdmin() != null) {
						objectAdminBox.setVisible(true);
						objectAdmName.setText(studyGroup.getGroupAdmin().getName());
						objectAdmCountry.setText(studyGroup.getGroupAdmin().getNationality() + "");
						objectAdmEye.setText(studyGroup.getGroupAdmin().getEyeColor() + "");
						objectAdmHair.setText(studyGroup.getGroupAdmin().getHairColor() + "");
						objectAdmX.setText(studyGroup.getGroupAdmin().getLocation().getX() + "");
						objectAdmY.setText(studyGroup.getGroupAdmin().getLocation().getY() + "");
						objectAdmZ.setText(studyGroup.getGroupAdmin().getLocation().getZ() + "");
						objectAdmPass.setText(studyGroup.getGroupAdmin().getPassportID());
						objectAdmLocName.setText(studyGroup.getGroupAdmin().getLocation().getName() + "");
					} else {
						objectAdminBox.setVisible(false);
					}
					infoPane.setDisable(false);
					if(!studyGroup.getOwner().equals(login))
						buttonPane.setDisable(true);
					else
						buttonPane.setDisable(false);
				}
			});
			circle.cursorProperty().set(Cursor.DEFAULT);
			objectGroup.getChildren().add(stackObject);
			objectsMap.put(studyGroup.getId(), circle);
			parAnimation.play();
		});
	}
	
	private void clearObjectInfo() {
		Platform.runLater(() -> {
			objectId.clear();
			objectName.clear();
			objectOwner.clear();
			objectX.clear();
			objectY.clear();
			objectSc.clear();
			objectEs.clear();
			objectTs.clear();
			objectFoE.clear();
			objectDate.clear();
			objectAdmName.clear();
			objectAdmCountry.clear();
			objectAdmEye.clear();
			objectAdmHair.clear();
			objectAdmX.clear();
			objectAdmY.clear();
			objectAdmZ.clear();
			objectAdmPass.clear();
			objectAdmLocName.clear();
		});
	}
	
	private void updateObjectInfo(StudyGroup studyGroup) {
		Platform.runLater(() -> {
			objectId.setText(studyGroup.getId() + "");
			objectName.setText(studyGroup.getName());
			objectOwner.setText(studyGroup.getOwner());
			objectX.setText(studyGroup.getCoordinates().getX() + "");
			objectY.setText(studyGroup.getCoordinates().getY() + "");
			objectSc.setText(studyGroup.getStudentsCount() + "");
			objectEs.setText(studyGroup.getExpelledStudents() + "");
			objectTs.setText(studyGroup.getTransferredStudents() + "");
			objectFoE.setText(studyGroup.getFormOfEducation().toString());
			objectDate.setText(studyGroup.getCreationLocalDate().toString());
			if(studyGroup.getGroupAdmin() != null) {
				objectAdminBox.setVisible(true);
				objectAdmName.setText(studyGroup.getGroupAdmin().getName());
				objectAdmCountry.setText(studyGroup.getGroupAdmin().getNationality() + "");
				objectAdmEye.setText(studyGroup.getGroupAdmin().getEyeColor() + "");
				objectAdmHair.setText(studyGroup.getGroupAdmin().getHairColor() + "");
				objectAdmX.setText(studyGroup.getGroupAdmin().getLocation().getX() + "");
				objectAdmY.setText(studyGroup.getGroupAdmin().getLocation().getY() + "");
				objectAdmZ.setText(studyGroup.getGroupAdmin().getLocation().getZ() + "");
				objectAdmPass.setText(studyGroup.getGroupAdmin().getPassportID());
				objectAdmLocName.setText(studyGroup.getGroupAdmin().getLocation().getName() + "");
			} else {
				objectAdminBox.setVisible(false);
			}
		});
	}
	
	private Task<Void> getOutputTask() {
		return new Task<Void>() {
			@SuppressWarnings("resource")
			@Override
			protected Void call() throws Exception {
				Scanner sc = new Scanner(Launcher.getClient().getAnswerInput());
				while (sc.hasNextLine()) {
					String f = sc.nextLine();
					if(f.equals("****"))
						break;
					Platform.runLater(() -> {
						outputField.appendText(f + "\n");
					});
				}
				return null;
			}
		};
	}
	
	private void setDefaultCommandsButton(Button but, String com) {
		but.setOnMouseClicked(e -> {
        	
        	commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(true));
        	
        	Task<Void> comTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Launcher.getClient().executeCommand(com, new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8), System.out);
					return null;
				}
			};
			
			new Thread(comTask).start();
			
			comTask.setOnSucceeded(e1 -> {
				Task<Void> outputTask = getOutputTask();
				new Thread(outputTask).start();
				outputTask.setOnSucceeded(t -> {
					commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
				});
			});
			comTask.setOnFailed(e1 -> {
				Platform.runLater(() -> {
					outputField.appendText(Launcher.getClient().getLastError() + "\n");
				});
				commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
			});
        });
	}
	
	private void setArgsCommandsButton(Button but, String com) {
		
		but.setOnMouseClicked(e -> {

			String res = "";
			TextInputDialog dialog = new TextInputDialog();
			dialog.initOwner(Launcher.getStage());
	        dialog.setTitle(resources.getString("argument"));
	        dialog.setHeaderText(resources.getString("argument.enter"));

	        java.util.Optional<String> result = dialog.showAndWait();
	        if (result.isPresent()) {
	            res = result.get();
	        } else {
	        	return;
	        }
			
			commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(true));

			String locRes = res;
			Task<Void> commandTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Launcher.getClient().executeCommand(com + " " + locRes, new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8), System.out);
					return null;
				}
			};

			new Thread(commandTask).start();

			commandTask.setOnSucceeded(e1 -> {
				Task<Void> outputTask = getOutputTask();
				new Thread(outputTask).start();
				outputTask.setOnSucceeded(t -> {
					commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
				});
			});
			commandTask.setOnFailed(e1 -> {
				Platform.runLater(() -> {
					outputField.appendText(Launcher.getClient().getLastError() + "\n");
				});
				commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
			});
		});
	}
	
	private void setWarningCommandButton(Button but, String com) {
		but.setOnMouseClicked(e -> {

			Alert alert = new Alert(AlertType.WARNING, resources.getString("clear.text"), ButtonType.YES, ButtonType.NO);
			alert.setTitle(resources.getString("clear"));
			alert.initOwner(Launcher.getStage());
			alert.showAndWait();

			if (alert.getResult() == ButtonType.YES) {

				commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(true));

				Task<Void> commandTask = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						Launcher.getClient().executeCommand(com, new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8), System.out);
						return null;
					}
				};

				new Thread(commandTask).start();

				commandTask.setOnSucceeded(e1 -> {
					Task<Void> outputTask = getOutputTask();
					new Thread(outputTask).start();
					outputTask.setOnSucceeded(t -> {
						commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
					});
				});
				commandTask.setOnFailed(e1 -> {
					Platform.runLater(() -> {
						outputField.appendText(Launcher.getClient().getLastError() + "\n");
					});
					commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
				});
			}
		});
		
	}
	
	private void setExecuteCommandButton() {
		executeButton.setOnMouseClicked(e -> {

			FileChooser fileChooser = new FileChooser();
			File selectedFile = fileChooser.showOpenDialog(Launcher.getStage());

			if (selectedFile != null) {

				commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(true));

				Task<Void> commandTask = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						System.out.println("execute_script " + selectedFile.getAbsolutePath());
						Launcher.getClient().executeCommand("execute_script " + selectedFile.getAbsolutePath(), new ReaderInputStream(new StringReader(""), StandardCharsets.UTF_8), System.out);
						return null;
					}
				};

				new Thread(commandTask).start();

				commandTask.setOnSucceeded(e1 -> {
					Task<Void> outputTask = getOutputTask();
					new Thread(outputTask).start();
					outputTask.setOnSucceeded(t -> {
						commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
					});
				});
				commandTask.setOnFailed(e1 -> {
					Platform.runLater(() -> {
						outputField.appendText(Launcher.getClient().getLastError() + "\n");
					});
					commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
				});
			}
		});
	}
	
	private void setAddCommandButton(Button but, String com) {
		but.setOnMouseClicked(e -> {

			String[] lang = {"object.name", "object.x", "object.y", "object.sc",
					"object.es", "object.ts", "object.foe", "argument.admin", 
					"object.admname", "object.admpass", "object.admeye", "object.admhair",
					"object.admcountry", "object.admlocname", "object.admx", "object.admy", "object.admz"};
			String res = "";
			for(int i = 0; i < 17; i++) {
				TextInputDialog dialog = new TextInputDialog();
				dialog.initOwner(Launcher.getStage());
		        dialog.setTitle(resources.getString("argument") + " " + i);
		        dialog.setHeaderText(resources.getString(lang[i]));
	
		        java.util.Optional<String> result = dialog.showAndWait();
		        if (result.isPresent()) {
		        	String s = result.get();
					if (i == 7 && s.equals("NO")) {
						res = res + s + "\n";
						break;
					}
		            res = res + s + "\n";
		        } else {
		        	return;
		        }
			}
			
			commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(true));

			String locRes = res;
			Task<Void> commandTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Launcher.getClient().executeCommand(com, new ReaderInputStream(new StringReader(locRes), StandardCharsets.UTF_8), 
							new PrintStream(new GUIUtils.StreamCapturer(new GUIUtils.Consumer() {
								@Override
								public void appendText(String text) {
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											outputField.appendText(text);
										}
									});
								}
							}, System.out)));
					return null;
				}
			};

			new Thread(commandTask).start();

			commandTask.setOnSucceeded(e1 -> {
				Task<Void> outputTask = getOutputTask();
				new Thread(outputTask).start();
				outputTask.setOnSucceeded(t -> {
					commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
				});
			});
			commandTask.setOnFailed(e1 -> {
				Platform.runLater(() -> {
					outputField.appendText(Launcher.getClient().getLastError() + "\n");
				});
				commandsGridPane.getChildren().stream().forEach(b -> b.setDisable(false));
			});
		});
	}
}