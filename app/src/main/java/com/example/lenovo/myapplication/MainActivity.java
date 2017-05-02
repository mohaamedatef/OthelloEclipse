package com.example.lenovo.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements Runnable {
    private EditText editText;
    private Socket clientSocket;
    private static BufferedReader inFromServer;
    private static DataOutputStream outToServer;
    private boolean firstTime = false;
    private Thread x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x = new Thread(this);

        editText = (EditText) findViewById(R.id.editText);
        findViewById(R.id.dummy).setFocusableInTouchMode(true);
        findViewById(R.id.dummy).requestFocus();
    }

    public void run() {
        try {
            clientSocket = new Socket("192.168.1.12", 7777);
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(editText.getText().toString() + '\n');

            boolean condition = true;
            while (condition) {
                String x = inFromServer.readLine();
                if (x.startsWith("success")) {
                    condition = false;
                    showOnlinePlayers(x.split(",")[1]);
                }
                if (x.startsWith("sorry")) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Sorry Name Unavailable",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showOnlinePlayers(String name) {

        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("Name", name);
        startActivity(intent);
        this.finish();
    }

    public void ssll(View v) throws IOException {

        if (firstTime) {
            outToServer.writeBytes(editText.getText().toString() + '\n');
            return;
        }
        x.start();
        firstTime = true;
    }

    public static BufferedReader getInFromServer() {
        return inFromServer;
    }

    public static DataOutputStream getOutToServer() {
        return outToServer;
    }

}