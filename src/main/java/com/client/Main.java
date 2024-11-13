package com.client;

import java.util.List;

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


        pauseDuring(1500, () -> { // Give time to show connecting message ...

            String protocol = "a";
            String host = "a";
            String port = "a";
            wsClient = UtilsWS.getSharedInstance(protocol + "://" + host + ":" + port);

            wsClient.onMessage((response) -> { Platform.runLater(() -> { wsMessage(response); }); });
            wsClient.onError((response) -> { Platform.runLater(() -> { wsError(response); }); });
        });
    }
   
    private static void wsMessage(String response) {
        // System.out.println(response);
        JSONObject msgObj = new JSONObject(response);
        switch (msgObj.getString("type")) {
            case "clients":

                break;
            
        }
    }

    private static void wsError(String response) {

        
    }
}
