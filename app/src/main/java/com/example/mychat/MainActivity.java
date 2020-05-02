package com.example.mychat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    static String myName;
    static Server server;
    RecyclerView chatWindow;
    Button sendButton;
    EditText inputMessage;
    EditText statusText;
    MessageController controller;

    protected void getUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите Ваше имя");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myName = input.getText().toString();
                server.sendName(myName);
            }
        });
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatWindow = findViewById(R.id.chatWindow);
        sendButton = findViewById(R.id.sendMessage);
        inputMessage = findViewById(R.id.chatInput);
        statusText = findViewById(R.id.statusText);

        controller = new MessageController();

        controller
                .setIncomingLayout(R.layout.incoming_message)
                .setOutgoingLayout(R.layout.outgoing_message)
                .setMessageTextId(R.id.messageText)
                .setMessageTimeId(R.id.messageDate)
                .setUserNameId(R.id.userName)
                .appendTo(chatWindow, this);

        // Временно, для теста

        server = new Server(new Consumer<Pair<String, String>>() {
            @Override
            public void accept(final Pair<String, String> pair) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        controller.addMessage(
                                new MessageController.Message(
                                        pair.second,
                                        pair.first,
                                        false
                                )
                        );
                    }
                });

            }
        }, new Consumer<Pair<String, String>>() {
            @Override
            public void accept(final Pair<String, String> pair) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = String.format("User %s", pair.first);
                        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
                        statusText.setText(String.format("Всего пользователей: %s", pair.second));
                    }
                });
            }
        });
        server.connect(); // Подключаемся

        getUserName();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputMessage.getText().toString();
                controller.addMessage(
                        new MessageController.Message(
                                text,
                                myName,
                                true
                        )
                );
                inputMessage.setText("");
                server.sendMessage(text);
            }
        });
    }
}
