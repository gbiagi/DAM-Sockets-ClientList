package com.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UtilsWS {

    private static UtilsWS sharedInstance = null;
    private WebSocketClient client;
    private Consumer<String> onOpenCallBack = null;
    private Consumer<String> onMessageCallBack = null;
    private Consumer<String> onCloseCallBack = null;
    private Consumer<String> onErrorCallBack = null;
    private String location = "";
    private static AtomicBoolean exitRequested = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Stage connectionDialog;

    private UtilsWS(String location) {
        this.location = location;
        createNewWebSocketClient();
    }

    private void createNewWebSocketClient() {
        try {
            // Show connection dialog
            Platform.runLater(this::showConnectionDialog);

            this.client = new WebSocketClient(new URI(location), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    String message = "WS connected to: " + getURI();
                    System.out.println(message);
                    Main.pauseDuring(2000, () -> {
                        hideConnectionDialog();
                    });
                    if (onOpenCallBack != null) {
                        onOpenCallBack.accept(message);
                    }
                }

                @Override
                public void onMessage(String message) {
                    if (onMessageCallBack != null) {
                        onMessageCallBack.accept(message);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    // Show dialog when connection is lost
                    Platform.runLater(() -> showConnectionDialog());

                    String message = "WS closed connection from: " + getURI() + " with reason: " + reason;
                    System.out.println(message);
                    if (onCloseCallBack != null) {
                        onCloseCallBack.accept(message);
                    }
                    if (remote) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception e) {
                    String message = "WS connection error: " + e.getMessage();
                    System.out.println(message);
                    if (onErrorCallBack != null) {
                        onErrorCallBack.accept(message);
                    }
                    if (e.getMessage().contains("Connection refused") || e.getMessage().contains("Connection reset")) {
                        scheduleReconnect();
                    }
                }
            };
            this.client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.out.println("WS Error, " + location + " is not a valid URI");
        }
    }

    private void scheduleReconnect() {
        if (!exitRequested.get()) {
            scheduler.schedule(this::reconnect, 5, TimeUnit.SECONDS);
        }
    }

    private void reconnect() {
        if (exitRequested.get()) {
            return;
        }

        System.out.println("WS reconnecting to: " + this.location);

        if (client != null) {
            client.close();
        }
        createNewWebSocketClient();
    }

    private void showConnectionDialog() {
        if (connectionDialog == null) {
            connectionDialog = new Stage();
            connectionDialog.initModality(Modality.APPLICATION_MODAL);
            connectionDialog.setTitle("Connection Status");

            VBox content = new VBox(10);
            content.setAlignment(Pos.CENTER);
            content.setPadding(new Insets(20));

            Label headerLabel = new Label("Connecting to server...");
            headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label contentLabel = new Label("Please wait while we establish connection.");

            ProgressIndicator progress = new ProgressIndicator();
            progress.setProgress(-1);

            content.getChildren().addAll(headerLabel, contentLabel, progress);

            Scene scene = new Scene(content);
            connectionDialog.setScene(scene);
            connectionDialog.setResizable(false);

            // Prevent window from being closed
            connectionDialog.setOnCloseRequest(Event::consume);
        }

        Platform.runLater(() -> {
            if (!connectionDialog.isShowing()) {
                connectionDialog.show();
                System.out.println("Dialog shown");
            }
        });
    }

    private void hideConnectionDialog() {
        Platform.runLater(() -> {
            if (connectionDialog != null && connectionDialog.isShowing()) {
                connectionDialog.hide();
                System.out.println("Dialog hidden");
            }
        });
    }

    public static UtilsWS getSharedInstance(String location) {
        if (sharedInstance == null) {
            sharedInstance = new UtilsWS(location);
        }
        return sharedInstance;
    }

    public void onOpen(Consumer<String> callBack) {
        this.onOpenCallBack = callBack;
    }

    public void onMessage(Consumer<String> callBack) {
        this.onMessageCallBack = callBack;
    }

    public void onClose(Consumer<String> callBack) {
        this.onCloseCallBack = callBack;
    }

    public void onError(Consumer<String> callBack) {
        this.onErrorCallBack = callBack;
    }

    public void safeSend(String text) {
        try {
            if (client != null && client.isOpen()) {
                client.send(text);
            } else {
                System.out.println("WS Error: Client is not connected. Attempting to reconnect...");
                scheduleReconnect();
            }
        } catch (Exception e) {
            System.out.println("WS Error sending message: " + e.getMessage());
        }
    }

    public void forceExit() {
        System.out.println("WS Closing ...");
        exitRequested.set(true);
        try {
            if (client != null && !client.isClosed()) {
                client.closeBlocking();
            }
        } catch (Exception e) {
            System.out.println("WS Interrupted while closing WebSocket connection: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            scheduler.shutdownNow();
        }
    }

    public boolean isOpen() {
        return client != null && client.isOpen();
    }
}