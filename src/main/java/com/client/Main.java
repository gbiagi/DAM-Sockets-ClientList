package com.client;

import org.json.JSONObject;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
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

        stageFX.setScene(scene);
        stageFX.setResizable(true);
        stageFX.setTitle("ClientList");
        stageFX.show();

        listController = new ListController();

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

        String protocol = "ws";
        String host = "localhost";
        String port = "12345";
        wsClient = UtilsWS.getSharedInstance(protocol + "://" + host + ":" + port);

        wsClient.onMessage((response) -> {
            Platform.runLater(() -> {
                wsMessage(response);
            });
        });

        wsClient.onError((response) -> {
            Platform.runLater(() -> {
                wsError(response);
            });
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
        System.out.println("erorr..");
    }
}
