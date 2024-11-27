package com.client;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListController implements Initializable {
    @FXML
    private VBox clientList;
    @FXML
    private Button addButton;

    private String clientName;

    public static ListController instance;

    public Alert alert;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;

        alert = new Alert(Alert.AlertType.INFORMATION);
        // Show waiting dialog with no buttons
        alert.setTitle("Connecting to server");
        alert.setHeaderText("Please wait while a connection to the server is established...");
        alert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true); // Disable the OK button
        alert.setOnCloseRequest(Event::consume); // Prevent closing the dialog
        alert.initModality(Modality.APPLICATION_MODAL); // Make the alert modal
        //alert.show();
        alert.getDialogPane().requestFocus(); // Request focus for the dialog pane

        addButton.setOnAction(e -> addClient());
    }

    private void addClient() {
        // Dialog to ask for client name
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Client Name");
        dialog.setHeaderText("Enter the name of the new client:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> clientName = name);

        if (!clientName.isEmpty()) {
            // Check unique name
            if (checkName(clientName)) {
                addClientBox(clientName);
            }
        }
    }

    private void addClientBox(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/client_layout.fxml"));
            HBox clientBox = loader.load();

            ClientController controller = loader.getController();
            controller.setName(name);
            controller.setPosition(clientList.getChildren().size());

            // Set the controller as user data for the HBox
            clientBox.setUserData(controller);

            JSONObject obj = new JSONObject();
            obj.put("type", "addClient");
            obj.put("name", name);
            obj.put("position", clientList.getChildren().size());

            clientList.getChildren().add(clientBox);
            Main.wsClient.safeSend(obj.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void moveClientUp(String name) {
        ArrayList<HBox> clients = new ArrayList<>();
        clientList.getChildren().forEach(node -> clients.add((HBox) node));

        for (int i = 0; i < clients.size(); i++) {
            HBox clientBox = clients.get(i);
            ClientController controller = (ClientController) clientBox.getUserData();
            if (controller.getName().equals(name)) {
                if (i > 0) {
                    clientList.getChildren().remove(clientBox);
                    clientList.getChildren().add(i - 1, clientBox);
                    controller.setPosition(i - 1);
                }
                break;
            }
        }
    }

    public void moveClientDown(String name) {
        ArrayList<HBox> clients = new ArrayList<>();
        clientList.getChildren().forEach(node -> clients.add((HBox) node));

        for (int i = 0; i < clients.size(); i++) {
            HBox clientBox = clients.get(i);
            ClientController controller = (ClientController) clientBox.getUserData();
            if (controller.getName().equals(name)) {
                if (i < clients.size() - 1) {
                    clientList.getChildren().remove(clientBox);
                    clientList.getChildren().add(i + 1, clientBox);
                    controller.setPosition(i + 1);
                }
                break;
            }
        }
    }

    public void clearList() {
        // Delete all clients
        clientList.getChildren().clear();
    }

    public void deleteClientFromList(String name) {
        clientList.getChildren().removeIf(node -> {
            HBox clientBox = (HBox) node;
            ClientController controller = (ClientController) clientBox.getUserData();
            return controller.getName().equals(name);
        });
    }

    public void editClient(String name, String newName) {
        for (int i = 0; i < clientList.getChildren().size(); i++) {
            HBox clientBox = (HBox) clientList.getChildren().get(i);
            ClientController controller = (ClientController) clientBox.getUserData();
            if (controller.getName().equals(name)) {
                controller.setName(newName);
                break;
            }
        }
    }

    public boolean checkName(String name) {
        for (int i = 0; i < clientList.getChildren().size(); i++) {
            HBox clientBox = (HBox) clientList.getChildren().get(i);
            ClientController controller = (ClientController) clientBox.getUserData();
            if (controller.getName().equals(name)) {
                System.out.println("Client name already exists");
                // Show error alert
                Alert alertName = new Alert(Alert.AlertType.ERROR);
                alertName.setTitle("Error");
                alertName.setHeaderText("Client name already exists");
                alertName.showAndWait();
                return false;
            }
        }
        return true;
    }
}
