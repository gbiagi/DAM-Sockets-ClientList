package com.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends WebSocketServer {

    // private HashMap<String, Integer> clientList = new HashMap<>();
    private ArrayList<String> clientList = new ArrayList<>();

    public Main(InetSocketAddress address) {
        super(address);
    }

    public static void main(String[] args) {
        Main server = new Main(new InetSocketAddress(12345));
        server.start();

        LineReader reader = LineReaderBuilder.builder().build();
        System.out.println("Server running. Type 'exit' to gracefully stop it.");

        try {
            while (true) {
                String line = reader.readLine("> ");
                if ("exit".equalsIgnoreCase(line.trim())) {
                    System.out.println("Stopping server...");
                    server.stop(1000);
                    break;
                } else {
                    System.out.println("Unknown command. Type 'exit' to stop server gracefully.");
                }
            }
        } catch (UserInterruptException | EndOfFileException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server stopped.");
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("WebSocket client connected: " + conn);
        clientList.clear();

        JSONObject message = new JSONObject();
        message.put("type", "clearList");
        conn.send(message.toString());

        System.out.println("Client list cleared");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        JSONObject obj = new JSONObject(message);
        JSONObject response = new JSONObject();

        // Verificar si el mensaje tiene un tipo
        if (obj.has("type")) {
            String type = obj.getString("type");
            switch (type) {
                case "addClient":
                    System.out.println("Added client");
                    String name = obj.getString("name");
                    int position = obj.getInt("position");
                    clientList.add(name);
                    System.out.println("Updated Client List:\n" + clientList);
                    break;
                case "editClient":

                    clientList.add(obj.getString("newName"));
                    clientList.remove(obj.getString("name"));

                    response.put("type", "editClient");
                    response.put("name", obj.getString("name"));
                    response.put("newName", obj.getString("newName"));
                    conn.send(response.toString());

                    System.out.println("Client information updated");
                    System.out.println("Updated Client List:\n" + clientList.toString());
                    break;
                case "moveClientUp":
                    System.out.println("Moving client up");
                    response.put("type", "moveClientUp");
                    response.put("name", obj.getString("name"));
                    conn.send(response.toString());
                    break;
                case "moveClientDown":
                    System.out.println("Moving client down");
                    response.put("type", "moveClientDown");
                    response.put("name", obj.getString("name"));
                    conn.send(response.toString());
                    break;
                case "deleteClient":
                    System.out.println("Deleting client");
                    response.put("type", "deleteClient");
                    response.put("name", obj.getString("name"));
                    conn.send(response.toString());
                    clientList.remove(obj.getString("name"));
                    System.out.println("Updated Client List:\n" + clientList.toString());
                    break;
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port: " + getPort());
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
}
