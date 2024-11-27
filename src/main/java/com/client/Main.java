package com.client;

import java.util.List;

import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.json.JSONObject;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Main extends Application {

    public static Stage stageFX;
    public static UtilsWS wsClient;
    public static ListController listController;

    @Override
    public void start(Stage stage) throws Exception {

        stageFX = stage;
        // Carrega la vista inicial des del fitxer FXML
        Parent root = FXMLLoader.load(getClass().getResource("/assets/layout_main.fxml"));
        Scene scene = new Scene(root);

        listController = new ListController();

        stageFX.setScene(scene);
        stageFX.setResizable(true);
        stageFX.setTitle("ClientList");
        stageFX.show();

        // Connect to local server
        connectToServer();
    }

    @Override
    public void stop() {
        if (wsClient != null) {
            wsClient.forceExit();
        }
        System.exit(1); // Kill all executor services
    }

    public static Stage getStage() {
        return stageFX;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void pauseDuring(long milliseconds, Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.millis(milliseconds));
        pause.setOnFinished(event -> Platform.runLater(action));
        pause.play();
    }

    public static void connectToServer() {
        // Show waiting dialog with no buttons
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connecting to server");
        alert.setHeaderText("Please wait while connecting to server...");
        alert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true); // Disable the OK button
        alert.setOnCloseRequest(Event::consume); // Prevent closing the dialog
        //alert.show();

        pauseDuring(1500, () -> { // Give time to show connecting message ...
            String protocol = "ws";
            String host = "localhost";
            String port = "12345";
            wsClient = UtilsWS.getSharedInstance(protocol + "://" + host + ":" + port);

            // Close the alert after connection is established
            Platform.runLater(alert::close);

            wsClient.onMessage((response) -> { Platform.runLater(() -> { wsMessage(response); }); });
            wsClient.onError((response) -> { Platform.runLater(() -> { wsError(response); }); });

        });
    }
   
    private static void wsMessage(String response) {
        // System.out.println(response);
        JSONObject msgObj = new JSONObject(response);
        switch (msgObj.getString("type")) {
            case "deleteClient":
                ListController.instance.deleteClientFromList(msgObj.getString("name"));
                break;
            case "editClient":
                ListController.instance.editClient(msgObj.getString("name"), msgObj.getString("newName"));
                break;
            case "moveClientUp":
                ListController.instance.moveClientUp(msgObj.getString("name"));
                break;
            case "moveClientDown":
                ListController.instance.moveClientDown(msgObj.getString("name"));
                break;
            case "clearList":
                ListController.instance.clearList();
                break;
        }
    }

    private static void wsError(String response) {

    }
}
