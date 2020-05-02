package com.example.mychat;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.core.util.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private WebSocketClient client;
    private Map<Long, String> names = new ConcurrentHashMap<>();
    private Consumer<Pair<String, String>> onMessageReceived;
    private Consumer<Pair<String, String>> onUserConnected;

    public Server(Consumer<Pair<String, String>> onMessageReceived, Consumer<Pair<String, String>> onUserConnected) {
        this.onMessageReceived = onMessageReceived;
        this.onUserConnected = onUserConnected;
    }

    public void connect() {
        // Выполняет подключение к серверу
        // lab.nnbabkov.ru (мой) или 35.214.1.221 (skillbox)

        URI address = null;
        try {
            address = new URI("ws://lab.nnbabkov.ru:8881");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        client = new WebSocketClient(address) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("SERVER", "Connected to server");
            }

            @Override
            public void onMessage(String json) {
                Log.i("SERVER", "Got json from server: " + json);
                int type = Protocol.getType(json);
                if (type == Protocol.MESSAGE) {
                    // Пришло входящее сообщение
                    displayIncoming(Protocol.unpackMessage(json)); //
                }
                if (type == Protocol.USER_STATUS) {
                    // Пришёл статус пользователя
                    updateStatus(Protocol.unpackStatus(json));
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("SERVER", "Connection closed");

            }

            @Override
            public void onError(Exception ex) {
                Log.i("SERVER", "Error: " + ex.getMessage());
            }
        };
        client.connect();
    }

    public void sendName(String name) {
        final Protocol.UserName userName = new Protocol.UserName(name);
        if (client != null && client.isOpen()) {
            client.send(Protocol.packName(userName));
        }
    }

    public void sendMessage(String text) {
        try {
            text = Crypto.encrypt(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Protocol.Message mess = new Protocol.Message(text);
        if (client != null && client.isOpen()) {
            client.send(Protocol.packMessage(mess));
        }
    }

    private void updateStatus(Protocol.UserStatus status) {
        // Запомнить, что пользователь (имя и ID) имеет такой-то статус (online/offline)
        Protocol.User user = status.getUser();
        if (status.isConnected()) {
            names.put(user.getId(), user.getName());
        } else {
            names.remove(user.getId());
        }
        showToast(user, status);
    }

    private void displayIncoming(Protocol.Message message) {
        String name = names.get(message.getSender());
        if (name == null) {
            name = "Unnamed";
        }

        String text = null;
        try {
            text = Crypto.decrypt(message.getEncodedText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        onMessageReceived.accept(
                new Pair<>(name, text)
        );
    }

    private void showToast(Protocol.User user, Protocol.UserStatus status) {
        String action = status.isConnected() ? "Connected" : "Disconnected";
        String name = user.getName();
        if (name == null) {
            name = "Unnamed";
        }
        name = name  + ", " + Long.toString(user.getId()) + " " + action;
        String totalUsers = Long.toString(names.size());
        onUserConnected.accept(
                new Pair<String, String>(name, totalUsers)
        );
    }
}
