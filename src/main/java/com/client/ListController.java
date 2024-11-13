package com.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class ListController implements Initializable {
    @FXML
    private ListView<String> clientList;
    @FXML
    private Button addButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
   
        addButton.setOnAction(e -> addClient());
    }

    private void addClient() {
        clientList.getItems().add("Hola");
    }
}
