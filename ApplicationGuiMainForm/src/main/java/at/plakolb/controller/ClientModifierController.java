/*	HTL Leonding	*/
package at.plakolb.controller;

import at.plakolb.calculationlogic.db.controller.ClientController;
import at.plakolb.calculationlogic.db.entity.Client;
import at.plakolb.calculationlogic.util.Logging;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FXML Controller class
 *
 * @author Kepplinger
 */
public class ClientModifierController implements Initializable {

    private static ClientModifierController instance;

    @FXML
    private Label lb_Id;
    @FXML
    private TextField tf_Name;
    @FXML
    private TextField tf_Street;
    @FXML
    private TextField tf_Zipcode;
    @FXML
    private TextField tf_City;
    @FXML
    private TextField tf_Phonenumber;
    @FXML
    private TextField tf_Email;

    private Client openedClient;

    private TableView modifyingTable;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
    }

    public static ClientModifierController getInstance() {
        return instance;
    }

    /**
     * Loads a client into the modifier.
     *
     * @param client
     */
    public void loadClientIntoModifier(TableView modifyingTable,Client client) {
        openedClient = client;
        lb_Id.setText(String.valueOf(openedClient.getId()));
        tf_Name.setText(openedClient.getName());
        tf_Street.setText(openedClient.getStreet());
        tf_Zipcode.setText(openedClient.getZipCode());
        tf_City.setText(openedClient.getCity());
        tf_Phonenumber.setText(openedClient.getTelephoneNumber());
        tf_Email.setText(openedClient.getEmail());

        this.modifyingTable = modifyingTable;
    }

    /**
     * Saves all the changes that were made.
     *
     * @param event
     */
    @FXML
    private void save(ActionEvent event) {

        if (tf_Name.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Bitte geben Sie einen Namen ein.");
            alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
            alert.showAndWait();
            return;
        } else if (!tf_Email.getText().isEmpty()) {
            String regex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            Matcher matcher = Pattern.compile(regex).matcher(tf_Email.getText());

            if (!matcher.matches()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Die eingegeben E-Mail Adresse ist nicht korrekt.\nMöchten Sie trotzdem fortfahren?",
                        ButtonType.YES, ButtonType.CANCEL);
                alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
                alert.showAndWait();
                if (alert.getResult() == ButtonType.CANCEL) {
                    return;
                }
            }
        }

        try {
            openedClient.setName(tf_Name.getText());
            openedClient.setStreet(tf_Street.getText());
            openedClient.setZipCode(tf_Zipcode.getText());
            openedClient.setCity(tf_City.getText());
            openedClient.setTelephoneNumber(tf_Phonenumber.getText());
            openedClient.setEmail(tf_Email.getText());
            new ClientController().edit(openedClient);
        } catch (Exception ex) {
            Logging.getLogger().log(Level.SEVERE, "Client couldn't be modified correctly.", ex);
        }
        modifyingTable.getItems().clear();
        modifyingTable.setItems(FXCollections.observableArrayList(new ClientController().findAll()));
        modifyingTable.refresh();
        ((Stage) (((Node) event.getSource()).getScene().getWindow())).close();
    }

    /**
     * Nothing gets saved and the window gets closed.
     *
     * @param event
     */
    @FXML
    private void cancel(ActionEvent event) {
        ((Stage) (((Node) event.getSource()).getScene().getWindow())).close();
    }
}
