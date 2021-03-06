/*	HTL Leonding	*/
package at.plakolb.controller;

import at.plakolb.calculationlogic.db.controller.ProductController;
import at.plakolb.calculationlogic.db.controller.UnitController;
import at.plakolb.calculationlogic.db.entity.Product;
import at.plakolb.calculationlogic.db.entity.Unit;
import at.plakolb.calculationlogic.eunmeration.ProductType;
import at.plakolb.calculationlogic.util.Logging;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

import static at.plakolb.calculationlogic.util.UtilityFormat.removeUnnecessaryCommas;

/**
 * FXML Controller class
 *
 * @author Kepplinger
 */
public class ProductCreatorController implements Initializable {

    @FXML
    private TextField tf_Name;
    @FXML
    private TextField tf_Width;
    @FXML
    private TextField tf_Height;
    @FXML
    private TextField tf_Length;
    @FXML
    private TextField tf_PriceUnit;
    @FXML
    private ComboBox<Unit> cb_Unit;
    @FXML
    private ComboBox<ProductType> cb_ProductType;
    @FXML
    private TextField tf_ColorFactor;

    private ObservableList<Unit> units;
    private ObservableList<ProductType> productTypes;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        units = FXCollections.observableArrayList(new UnitController().findAll());
        productTypes = FXCollections.observableArrayList(ProductType.values());
        cb_Unit.setItems(units);
        cb_ProductType.setItems(productTypes);

        tf_ColorFactor.textProperty().addListener((observable, oldValue, newValue) -> {
            tf_ColorFactor.setText(tf_ColorFactor.getText().replace('.',',').replaceAll("[^\\d,]", ""));
            tf_ColorFactor.setText(removeUnnecessaryCommas(tf_ColorFactor.getText()));
        });

        tf_Height.textProperty().addListener((observable, oldValue, newValue) -> {
            tf_Height.setText(tf_Height.getText().replace('.',',').replaceAll("[^\\d,]", ""));
            tf_Height.setText(removeUnnecessaryCommas(tf_Height.getText()));
        });

        tf_Length.textProperty().addListener((observable, oldValue, newValue) -> {
            tf_Length.setText(tf_Length.getText().replace('.',',').replaceAll("[^\\d,]", ""));
            tf_Length.setText(removeUnnecessaryCommas(tf_Length.getText()));
        });

        tf_Width.textProperty().addListener((observable, oldValue, newValue) -> {
            tf_Width.setText(tf_Width.getText().replace('.',',').replaceAll("[^\\d,]", ""));
            tf_Width.setText(removeUnnecessaryCommas(tf_Width.getText()));
        });

        tf_PriceUnit.textProperty().addListener((observable, oldValue, newValue) -> {
            tf_PriceUnit.setText(tf_PriceUnit.getText().replace('.',',').replaceAll("[^\\d,]", ""));
            tf_PriceUnit.setText(removeUnnecessaryCommas(tf_PriceUnit.getText()));
        });
    }

    /**
     * Saves the changes that were made.
     *
     * @param event
     */
    @FXML
    private void create(ActionEvent event) {

        String errorMessage = "";

        if (cb_ProductType.getValue() == null) {
            errorMessage += "Wählen Sie bitten einen Produkttyp aus.";
        }
        if (tf_Name.getText().isEmpty()) {
            errorMessage += "Geben Sie bitte einen Namen ein.\n";
        }
        if (tf_PriceUnit.getText().isEmpty()) {
            errorMessage += "Geben Sie bitte einen Preis ein.\n";
        } else if (tf_PriceUnit.getText().contains("-")) {
            errorMessage += "Der Preis darf nicht negativ sein.";
        } else {
            try {
                Double.parseDouble(tf_PriceUnit.getText().replace(',','.'));
            } catch (NumberFormatException e) {
                errorMessage += "Der Preis hat ein ungültiges Format.\n";
            }
        }

        if (errorMessage.isEmpty()) {
            try {
                Product product = new Product();

                if (cb_ProductType.getSelectionModel().getSelectedItem() == ProductType.COLOR) {
                    product.setName(tf_Name.getText());
                    product.setWidthProduct(null);
                    product.setHeightProduct(null);
                    product.setLengthProduct(null);
                    product.setColorFactor(tryParseDouble(tf_ColorFactor.getText().replace(',','.')));
                    product.setPriceUnit(tryParseDouble(tf_PriceUnit.getText().replace(',','.')));
                    product.setUnit(cb_Unit.getValue());
                    product.setProductType(cb_ProductType.getValue());
                } else {
                    product.setName(tf_Name.getText());
                    product.setWidthProduct(tryParseDouble(tf_Width.getText().replace(',','.')));
                    product.setHeightProduct(tryParseDouble(tf_Height.getText().replace(',','.')));
                    product.setLengthProduct(tryParseDouble(tf_Length.getText().replace(',','.')));
                    product.setColorFactor(null);
                    product.setPriceUnit(tryParseDouble(tf_PriceUnit.getText().replace(',','.')));
                    product.setUnit(cb_Unit.getValue());
                    product.setProductType(cb_ProductType.getValue());
                }
                new ProductController().create(product);

            } catch (Exception ex) {
                Logging.getLogger().log(Level.SEVERE, "Couldn't create product.", ex);
            }

            ProductListController.getInstance().refreshTable();
            ((Stage) (((Node) event.getSource()).getScene().getWindow())).close();
        } else {
            Alert alert = new Alert(AlertType.ERROR, errorMessage);
            alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
            alert.showAndWait();
        }
    }

    /**
     * Closes window.
     *
     * @param event
     */
    @FXML
    private void cancel(ActionEvent event) {
        ((Stage) (((Node) event.getSource()).getScene().getWindow())).close();
    }

    /**
     * Gets called when the product type ComboBox selection is changed.
     *
     * @param event
     */
    @FXML
    private void productTypeChanged(ActionEvent event) {
        if (cb_ProductType.getSelectionModel().getSelectedItem() == ProductType.COLOR) {
            cb_Unit.getSelectionModel().select(new UnitController().findUnitByShortTerm("l"));
            cb_Unit.setDisable(true);
            tf_Height.setDisable(true);
            tf_Length.setDisable(true);
            tf_Width.setDisable(true);
            tf_ColorFactor.setDisable(false);
        } else {
            cb_Unit.getSelectionModel().select(0);
            cb_Unit.setDisable(false);
            tf_Height.setDisable(false);
            tf_Length.setDisable(false);
            tf_Width.setDisable(false);
            tf_ColorFactor.setDisable(true);
        }
    }

    /**
     * Parses String to Double if possible.
     *
     * @param numberString
     * @return
     */
    private Double tryParseDouble(String numberString) {
        try {
            numberString = numberString.replace(",", ".");
            return Double.parseDouble(numberString);
        } catch (NumberFormatException ex) {
            return 0d;
        }
    }

}
