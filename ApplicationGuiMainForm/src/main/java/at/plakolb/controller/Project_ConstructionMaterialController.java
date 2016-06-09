/*	HTL Leonding	*/
package at.plakolb.controller;

import at.plakolb.calculationlogic.util.Logging;
import at.plakolb.calculationlogic.db.controller.AssemblyController;
import at.plakolb.calculationlogic.db.controller.ComponentController;
import at.plakolb.calculationlogic.db.controller.ProductController;
import at.plakolb.calculationlogic.db.entity.Assembly;
import at.plakolb.calculationlogic.db.entity.Component;
import at.plakolb.calculationlogic.db.entity.Product;
import at.plakolb.calculationlogic.eunmeration.ProductType;
import at.plakolb.calculationlogic.util.UtilityFormat;
import at.plakolb.edit.AssemblyValueCell;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author User
 */
public class Project_ConstructionMaterialController implements Initializable {

    private static Project_ConstructionMaterialController instance;
    @FXML
    private ComboBox<Product> cb_Product;
    @FXML
    private TextField tf_Amount;
    @FXML
    private TextField tf_Price;
    @FXML
    private ComboBox<Component> cb_Component;
    @FXML
    private TableView<Assembly> tv_Assembly;
    @FXML
    private TableColumn<Assembly, String> tc_Product;
    @FXML
    private TableColumn<Assembly, String> tc_Amount;
    @FXML
    private TableColumn<Assembly, String> tc_Price;
    @FXML
    private TableColumn<Assembly, String> tc_Component;
    @FXML
    private TableColumn<Assembly, String> tc_TotalPrice;
    @FXML
    private TableColumn tc_Button;
    @FXML
    private Label lb_TotalCosts;

    private List<Assembly> assemblyList;
    private List<Component> componentList;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        assemblyList = new LinkedList<>();
        componentList = new LinkedList<>();

        if (ProjectViewController.getOpenedProject() != null && ProjectViewController.getOpenedProject().getId() != null) {
            assemblyList = new AssemblyController().findAssembliesByProjectId(ProjectViewController.getOpenedProject().getId());
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

        tv_Assembly.setEditable(true);

        tf_Amount.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            tf_Amount.setText(tf_Amount.getText().replaceAll(",", ".").replaceAll("[^\\d.]", ""));
            tf_Amount.setText(UtilityFormat.removeUnnecessaryCommas(tf_Amount.getText()));
        });
        tc_Amount.setCellFactory((TableColumn<Assembly, String> param) -> new AssemblyValueCell());
        tc_Amount.setOnEditCommit((TableColumn.CellEditEvent<Assembly, String> event) -> {
            Assembly assembly = ((Assembly) event.getTableView().getItems().get(event.getTablePosition().getRow()));
            if (!event.getNewValue().equals("")) {
                assembly.setNumberOfComponents(Double.parseDouble(event.getNewValue()));
            } else {
                assembly.setNumberOfComponents(null);
            }
            refreshListView();
        });

        tf_Price.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            tf_Price.setText(tf_Price.getText().replaceAll(",", ".").replaceAll("[^\\d.]", ""));
            tf_Price.setText(UtilityFormat.removeUnnecessaryCommas(tf_Price.getText()));
        });

        tc_Product.setCellValueFactory(new PropertyValueFactory<>("product"));
        
        tc_Amount.setCellValueFactory((TableColumn.CellDataFeatures<Assembly, String> param) -> {
            if (param.getValue().getNumberOfComponents() != null) {
                return new ReadOnlyObjectWrapper<>(decimalFormat.format(param.getValue().getNumberOfComponents()));
            } else {
                return new ReadOnlyObjectWrapper<>("");
            }
        });
        
        tc_Component.setCellValueFactory(new PropertyValueFactory<>("component"));

        tc_Price.setCellValueFactory((TableColumn.CellDataFeatures<Assembly, String> param) -> {
            if (param.getValue().getPrice() != null) {
                return new ReadOnlyObjectWrapper<>(decimalFormat.format(param.getValue().getPrice()));
            } else {
                return new ReadOnlyObjectWrapper<>("");
            }
        });
        tc_Price.setCellFactory((TableColumn<Assembly, String> param) -> new AssemblyValueCell());
        tc_Price.setOnEditCommit((TableColumn.CellEditEvent<Assembly, String> event) -> {
            Assembly assembly = ((Assembly) event.getTableView().getItems().get(event.getTablePosition().getRow()));
            if (!event.getNewValue().equals("")) {
                assembly.setPrice(Double.parseDouble(event.getNewValue()));
            } else {
                assembly.setPrice(null);
            }
            refreshListView();
        });

        tc_TotalPrice.setCellValueFactory((TableColumn.CellDataFeatures<Assembly, String> param)
                -> new ReadOnlyObjectWrapper<>(decimalFormat.format(param.getValue().getPrice() * param.getValue().getNumberOfComponents()) + " €")
        );

        tc_Button.setCellFactory(new Callback<TableColumn<Component, String>, TableCell<Component, String>>() {
            @Override
            public TableCell<Component, String> call(TableColumn<Component, String> param) {
                TableCell<Component, String> cell = new TableCell<Component, String>() {

                    Label deletionLabel = new Label();

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            deletionLabel.setId("delete");
                            deletionLabel.setTooltip(new Tooltip("Material löschen"));

                            deletionLabel.setOnMouseClicked(event -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sind Sie sicher, dass sie dieses Material entgültig löschen möchten? Vorsicht, der Löschvorgang kann nicht mehr rückgängig gemacht werden.",
                                        ButtonType.YES, ButtonType.CANCEL);
                                alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
                                alert.showAndWait();
                                if (alert.getResult() == ButtonType.YES) {
                                    try {
                                        assemblyList.remove(tv_Assembly.getSelectionModel().getSelectedItem());
                                        if (tv_Assembly.getSelectionModel().getSelectedItem().getId() != null) {
                                            new ComponentController().destroy(tv_Assembly.getSelectionModel().getSelectedItem().getId());
                                        }
                                        ModifyController.getInstance().setProject_constructionMaterial(Boolean.TRUE);
                                    } catch (Exception ex) {
                                        Logging.getLogger().log(Level.SEVERE, "Couldn't delete material.", ex);
                                    } finally {
                                        refreshListView();
                                    }
                                }
                            });
                            setGraphic(deletionLabel);
                        }
                    }
                };
                return cell;
            }
        });

        cb_Product.setItems(FXCollections.observableArrayList(new ProductController().findByProductTypeOrderByName(ProductType.MISCELLANEOUS)));
        cb_Component.setItems(FXCollections.observableArrayList(Project_ConstructionMaterialListController.getInstance().getComponents()));

        cb_Product.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Product> observable, Product oldValue, Product newValue) -> {
            if (cb_Product.getSelectionModel().getSelectedItem() != null) {
                tf_Price.setText(UtilityFormat.getStringForTextField(cb_Product.getSelectionModel().getSelectedItem().getPriceUnit()));
            }
        });

        refreshListView();
    }

    public static Project_ConstructionMaterialController getInstance() {
        return instance;
    }

    public Double getMaterial() {
        return getTotalCosts(assemblyList);
    }

    public List<Assembly> getAssemblys() {
        return assemblyList;
    }

    public void refreshListView() {
        tv_Assembly.setItems(FXCollections.observableArrayList(assemblyList));
        tv_Assembly.getColumns().get(0).setVisible(false);
        tv_Assembly.getColumns().get(0).setVisible(true);
        lb_TotalCosts.setText(UtilityFormat.getStringForLabel(getTotalCosts(assemblyList)) + " €");
    }

    public void refreshComponents() {

        componentList.clear();
        componentList.addAll(Project_ConstructionMaterialListController.getInstance().getComponents());

        if (Assembling_FormworkController.getInstance().getComponent().getProduct() != null) {
            componentList.add(Assembling_FormworkController.getInstance().getComponent());
        }

        if (Assembling_VisibleFormworkController.getInstance().getComponent().getProduct() != null) {
            componentList.add(Assembling_VisibleFormworkController.getInstance().getComponent());
        }

        if (Assembling_FoilController.getInstance().getComponent().getProduct() != null) {
            componentList.add(Assembling_FoilController.getInstance().getComponent());
        }

        if (Assembling_SealingBandController.getInstance().getComponent().getProduct() != null) {
            componentList.add(Assembling_SealingBandController.getInstance().getComponent());
        }

        if (Assembling_CounterBattenController.getInstance().getComponent().getProduct() != null) {
            componentList.add(Assembling_CounterBattenController.getInstance().getComponent());
        }

        if (Assembling_BattensOrFullFormworkController.getInstance().getComponent().getProduct() != null) {
            componentList.add(Assembling_BattensOrFullFormworkController.getInstance().getComponent());
        }

        cb_Component.setItems(FXCollections.observableArrayList(componentList));

        if (cb_Component.getItems().isEmpty()) {
            cb_Component.setPromptText("Kein Bauteil vorhanden");
        } else {
            cb_Component.setPromptText("Bauteil auswählen");
        }
    }

    @FXML
    private void addMaterial(ActionEvent event) {

        double amount = 0;
        double price = 0;
        String errorMessage = "";

        if (cb_Product.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Es wurde kein Produkt ausgewählt.\n";
        }
        if (cb_Component.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Es wurde kein Bauteil ausgewählt.\n";
        }

        try {
            amount = Double.parseDouble(tf_Amount.getText());
            price = Double.parseDouble(tf_Price.getText());

            if (amount == 0) {
                errorMessage += "Die Anzahl muss größer als 0 sein.\n";
            }
            if (amount < 0 || price < 0) {
                errorMessage += "Negative Zahlen sind nicht erlaubt.\n";
            }
        } catch (NumberFormatException ex) {
            errorMessage += "Die eingegebenen Zahlen sind nicht gültig.\n";
        }

        if (errorMessage.equals("")) {

            Component component = cb_Component.getSelectionModel().getSelectedItem();
            component.setProject(ProjectViewController.getOpenedProject());

            assemblyList.add(new Assembly(cb_Product.getSelectionModel().getSelectedItem(),
                    component,
                    ProjectViewController.getOpenedProject(),
                    amount,
                    price));

            ModifyController.getInstance().setProject_constructionMaterial(Boolean.TRUE);
            refreshListView();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage);
            alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
            alert.showAndWait();
        }
    }

    public void persist() {

        AssemblyController assemblyController = new AssemblyController();

        for (Assembly assembly : assemblyList) {
            if (assembly.getId() == null) {
                assemblyController.create(assembly);
            }
        }
    }

    private double getTotalCosts(List<Assembly> assemblys) {

        double totalCosts = 0;

        for (Assembly assembly : assemblys) {
            totalCosts += assembly.getPrice() * assembly.getNumberOfComponents();
        }

        return totalCosts;
    }

    public int getAssemblyCount() {
        return assemblyList.size();
    }

    public void deleteRelativeAssemblies(Component c) {
        try {
            List<Assembly> supportList = new LinkedList<>();
            supportList.addAll(assemblyList);
            for (Assembly assembly : supportList) {
                if (assembly.getComponent().equals(c) || assembly.getComponent().getId().equals(c.getId())) {
                    assemblyList.remove(assembly);

                    if (assembly.getId() != null) {
                        new AssemblyController().destroy(assembly.getId());
                    }
                }
            }

            ModifyController.getInstance().setProject_constructionMaterial(Boolean.TRUE);
        } catch (Exception ex) {
            Logging.getLogger().log(Level.SEVERE, "Couldn't delete relative assemblies.", ex);
        } finally {
            refreshListView();
        }
    }

}
