package viewer;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.*;
import loader.Session;
import loader.SessionDirection;
import loader.SessionType;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class MainController {
    static final String MAIN_TITLE_CLOSED = "MTS Report Viewer";
    static final String MAIN_TITLE_OPENED = "MTS Report Viewer - [ %s ]";

    ReportModel reportModel = new ReportModel();

    @FXML
    private TextField textFieldCompanyName;
    @FXML
    private TextField textFieldAccountName;
    @FXML
    private TextField textFieldReportPeriod;
    @FXML
    private ListView<String> listViewRawReportData;

    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem menuItemFileExit;
    @FXML
    private MenuItem menuItemFileOpen;
    @FXML
    private MenuItem menuItemFileClose;
    @FXML
    private MenuItem menuItemViewShowColumnButtons;
    @FXML
    private ChoiceBox<String> goToEntityChoiceBox;
    @FXML
    private TreeTableView<PersonModel> treeTableViewEntities;
    @FXML
    private TreeTableColumn<PersonModel, String> treeTableColumnNumber;
    @FXML
    private TreeTableColumn<PersonModel, String> treeTableColumnEntityName;
    @FXML
    private TreeTableColumn<PersonModel, String> treeTableColumnContract;
    @FXML
    private TreeTableColumn<PersonModel, String> treeTableColumnPlanName;
    @FXML
    private TreeTableColumn<PersonModel, Float> treeTableColumnPlanPrice;
    @FXML
    private TreeTableColumn<PersonModel, Float> treeTableColumnTotal;
    @FXML
    private TreeTableColumn<PersonModel, Float> treeTableColumnOverdraft;
    @FXML
    private TableView<Session> tableViewSessions;
    @FXML
    private TableColumn<SessionModel, LocalDate> tableColumnSessionStartDate;
    @FXML
    private TableColumn<Session, LocalTime> tableColumnSessionStartTime;
    @FXML
    private TableColumn<Session, LocalTime> tableColumnSessionDuration;
    @FXML
    private TableColumn<Session, SessionDirection> tableColumnSessionDirection;
    @FXML
    private TableColumn<Session, SessionType> tableColumnSessionType;
    @FXML
    private TableColumn<Session, String> tableColumnRemoteNumber;
    @FXML
    private TableColumn<Session, String> tableColumnRemoteVendor;

    @FXML
    void initialize() {
        setEntityTableRoot();
        setEntityTableFactories();
        setEntityTableSelectionListener();
        setSessionTableFactories();
        setReportModelReportListener();

        goToEntityChoiceBox.getSelectionModel().selectedItemProperty().addListener(c -> {
            String[] parts = goToEntityChoiceBox.getSelectionModel().getSelectedItem().split(" : ");

            listViewRawReportData.getItems().forEach(s -> {
                // iterate through items and find first match
                if (s.contains(parts[0])) {
                    listViewRawReportData.scrollTo(s);
                    return;
                }
            });
        });
    }

    private void setEntityTableRoot() {
        treeTableViewEntities.setRoot(new TreeItem<>(null));
        treeTableViewEntities.setShowRoot(false);
        treeTableViewEntities.getRoot().setExpanded(true);
    }

    private void setReportModelReportListener() {
        // set listener to reportModel data change which happens on start, load and close
        reportModel.reportProperty().addListener(observable -> {
            System.out.println("ReportModel.reportProperty InvalidationListener fired");

            // change title of stage
            Stage stage = (Stage) menuBar.getScene().getWindow();
            if (reportModel.getReport() == null) {
                stage.setTitle(MAIN_TITLE_CLOSED);
            } else {
                stage.setTitle(String.format(MAIN_TITLE_OPENED, reportModel.getFilename()));
            }

            textFieldCompanyName.setText(reportModel.getCompanyName());
            textFieldAccountName.setText(reportModel.getAccountName());
            textFieldReportPeriod.setText(reportModel.getPeriod());
            listViewRawReportData.setItems(reportModel.getRawData());

            treeTableViewEntities.getSelectionModel().clearSelection();
            treeTableViewEntities.getRoot().getChildren().clear();

            goToEntityChoiceBox.getItems().clear();

            if (reportModel.getReport() != null) {
                reportModel.getReport().getPersons().forEach((s, p) -> {
                    // populate treeViewTable with TreeItems holding PersonModel
                    TreeItem<PersonModel> item = new TreeItem<>(new PersonModel(p, reportModel.getReport()));
                    treeTableViewEntities.getRoot().getChildren().add(item);

                    // populate goToChoiceBox with persons from ReportModel
                    String name = p.getContract();
                    if (item.getValue().getEntityName() != null)
                        name += " : " + item.getValue().getEntityName();

                    goToEntityChoiceBox.getItems().add(name);
                });
            }

        });
    }

    private void setEntityTableSelectionListener() {
        // initialize listener to treeView Selection change
        ListChangeListener<TreeItem<PersonModel>> entirySelectionListener = c -> {
            // if selection size = 0 then setItems(null) else get sessions of first element selected
            tableViewSessions.setItems(c.getList().size() == 0 ? null : c.getList().get(0).getValue().getSessions());
        };

        treeTableViewEntities.getSelectionModel().getSelectedItems().addListener(entirySelectionListener);
    }

    private void setSessionTableFactories() {
        tableColumnRemoteNumber.setCellValueFactory(new PropertyValueFactory<>("RemoteNumber"));
        tableColumnRemoteVendor.setCellValueFactory(new PropertyValueFactory<>("RemoteVendor"));
        tableColumnSessionStartDate.setCellValueFactory(new PropertyValueFactory<>("Date"));
        tableColumnSessionStartTime.setCellValueFactory(new PropertyValueFactory<>("Time"));
        tableColumnSessionDuration.setCellValueFactory(new PropertyValueFactory<>("Duration"));
        tableColumnSessionDirection.setCellValueFactory(new PropertyValueFactory<>("Direction"));
        tableColumnSessionType.setCellValueFactory(new PropertyValueFactory<>("Type"));
    }

    private void setEntityTableFactories() {
        treeTableColumnNumber.setCellValueFactory(new TreeItemPropertyValueFactory<>("Number"));
        treeTableColumnEntityName.setCellValueFactory(new TreeItemPropertyValueFactory<>("EntityName"));
        treeTableColumnPlanName.setCellValueFactory(new TreeItemPropertyValueFactory<>("PlanName"));
        treeTableColumnPlanPrice.setCellValueFactory(new TreeItemPropertyValueFactory<>("PlanPrice"));
        treeTableColumnTotal.setCellValueFactory(new TreeItemPropertyValueFactory<>("Total"));
        treeTableColumnContract.setCellValueFactory(new TreeItemPropertyValueFactory<>("Contract"));
        treeTableColumnOverdraft.setCellValueFactory(new TreeItemPropertyValueFactory<>("Overdraft"));

    }

    @FXML
    void onMenuViewShowColumnButtons(ActionEvent event) {
        treeTableViewEntities.setTableMenuButtonVisible(!treeTableViewEntities.isTableMenuButtonVisible());
        tableViewSessions.setTableMenuButtonVisible(!tableViewSessions.isTableMenuButtonVisible());
    }

    @FXML
    void onMenuFileClose(ActionEvent event) {
        reportModel.close();
    }

    @FXML
    void onMenuHelpAbout(ActionEvent event) {
        // This is root node of controls hierarchy, root is usually a layout
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("aboutscene.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // creating and setting Scene using root node of loaded control hierarchy
        Stage stage = new Stage();
        stage.setScene(new Scene(root));

        // set title, modality, decorations, show stage with loaded scene with and wait
        stage.initOwner(menuBar.getScene().getWindow());
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("About");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    void onMenuFileExit(ActionEvent event) {
        // window is superclass of Stage which has hide() method.
        Window window = menuBar.getScene().getWindow();
        // hide() closes window, if no other windows are open then app will exit
        window.hide();
    }

    @FXML
    void onMenuFileOpen(ActionEvent event) {
        // prepare and open file dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV Report File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All files", "*.*"),
                new FileChooser.ExtensionFilter("CSV reports", "*.csv")
        );

        // get window of current stage and make it a parent of dialog
        Window window = menuBar.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file == null) return;

        System.out.format("report file to open: %s\n", file.getAbsolutePath());

        // create report model and load data using filename provided
        reportModel.loadFromFile(file.getPath());
        
        System.out.println("total number of entities loaded: " + reportModel.getReport().getPersons().size());
    }
}