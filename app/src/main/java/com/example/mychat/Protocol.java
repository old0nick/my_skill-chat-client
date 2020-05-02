package com.example.mychat;

import com.google.gson.Gson;

public class Protocol {
    // Описание взаимодействия с сервером

    // USER_STATUS - сообщать состояние пользователя
    // MESSAGE - входящее/исходящее сообщение
    // USER_MANE - сообщаем серверу наше имя

    public final static int USER_STATUS = 1;
    public final static int MESSAGE = 2;
    public final static int USER_NAME = 3;

    // USER_NAME:   3{ name: "Николай" }
    // USER_STATUS: 1{ connected: true, user: {name: "Nick", id: 1828282 } }
    // MESSAGE:     2{ encodedText: "Хаюшки", sender: 1828282 }

    static class User {
        private String name;
        private long id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public User() {
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    static class UserStatus {
        private boolean connected;
        private User user;

        public UserStatus() {
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    static class UserName {
        private String name;

        public UserName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static class Message {
        public final static int GROUP_CHAT = 1;
        private long sender; // id отправителя
        private String encodedText;
        private long receiver = GROUP_CHAT;

        public Message(String encodedText) {
            this.encodedText = encodedText;
        }

        public long getSender() {
            return sender;
        }

        public void setSender(long sender) {
            this.sender = sender;
        }

        public String getEncodedText() {
            return encodedText;
        }

        public void setEncodedText(String encodedText) {
            this.encodedText = encodedText;
        }

        public long getReceiver() {
            return receiver;
        }

        public void setReceiver(long receiver) {
            this.receiver = receiver;
        }
    }

    // Будем вызывать при отправке своего имени на сервер
    public static String packName(UserName name) {
        Gson g = new Gson(); // поможет упаковать в JSON
        return USER_NAME + g.toJson(name); // 3{ name: "Николай" }
    }

    // Как получать с сервера информацию:
    // Статусы пользователей, имена пользователей и сами сообщения

    // Что за сообщение пришло
    public static int getType(String json) {
        if (json == null || json.length() == 0) {
            return -1;
        }
        return Integer.parseInt(json.substring(0, 1));
    }

    public static String packMessage(Message mess) {
        Gson g = new Gson(); // поможет упаковать в JSON
        return MESSAGE + g.toJson(mess); // 2{ encodedText: "Хаюшки", sender: 1828282 }
    }

    public static Message unpackMessage(String json) {
        Gson g = new Gson();
        return g.fromJson(json.substring(1), Message.class);

    }

    public static UserStatus unpackStatus(String json) {
        Gson g = new Gson();
        return g.fromJson(json.substring(1), UserStatus.class);
    }
}
