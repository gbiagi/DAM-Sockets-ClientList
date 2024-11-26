package com.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import org.json.JSONObject;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    Button bt1, bt2, bt3, bt4;
    @FXML
    Text name;

    private int position;
    private String newName;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        bt1.setOnAction(e -> moveUp());
        bt2.setOnAction(e -> moveDown());
        bt3.setOnAction(e -> editClient());
        bt4.setOnAction(e -> deleteClient());
    }

    public String getName() {
        return name.getText();
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void moveUp() {
        System.out.println("Moving client up");
        JSONObject obj = new JSONObject();
        obj.put("type", "moveClientUp");
        obj.put("name", getName());
        Main.wsClient.safeSend(obj.toString());
    }
    public void moveDown(){
        System.out.println("Moving client down");
        JSONObject obj = new JSONObject();
        obj.put("type", "moveClientDown");
        obj.put("name", getName());
        Main.wsClient.safeSend(obj.toString());
    }
    public void editClient(){
        System.out.println("Edit client");
        // Dialog to ask for client name
        String newName = "";

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Client Name");
        dialog.setHeaderText("Enter the new name of the client:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            newName = result.get();
            JSONObject obj = new JSONObject();
            obj.put("type", "editClient");
            obj.put("name", getName());
            obj.put("newName", newName);
            Main.wsClient.safeSend(obj.toString());
        }
    }
    public void deleteClient(){
        System.out.println("Deleting client");
        JSONObject obj = new JSONObject();
        obj.put("type", "deleteClient");
        obj.put("name", getName());
        Main.wsClient.safeSend(obj.toString());
    }
}
