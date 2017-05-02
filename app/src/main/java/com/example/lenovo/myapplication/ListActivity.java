package com.example.lenovo.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ListActivity extends AppCompatActivity implements Runnable {

    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private ListView listView;
    public String name;
    private String oppName;
    private Activity act = this;
    private String received;
    private boolean condition=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Bundle bundle = getIntent().getExtras();
        String name = bundle.getString("Name");
        this.name = name;
        Thread x = new Thread(this);
        x.start();

        listView = (ListView) findViewById(R.id.ListView);
        Toast.makeText(getApplicationContext(), "Welcome to the Game "+this.name,
                Toast.LENGTH_SHORT).show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Toast.makeText(getApplicationContext(), "Select a player to challenge",
                            Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        outToServer.writeBytes("request to," + ((TextView) view).getText() + '\n');
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    @Override
    public void run() {
        this.inFromServer = MainActivity.getInFromServer();
        this.outToServer = MainActivity.getOutToServer();

        while (condition) {
            try {
                received = inFromServer.readLine();
                if (received.startsWith("List")) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            String[] Alist = received.split(",");
                            ArrayList<String> list = new ArrayList<String>(Arrays.asList(Alist));
                            list.remove(list.indexOf(name));
                            ArrayAdapter a = new ArrayAdapter(act, android.R.layout.simple_list_item_1,list);
                            listView.setAdapter(a);
                        }
                    });
                }
                if (received.startsWith("sorry")) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), received.split(",")[1],
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (received.startsWith("challenge")) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //STUFF THAT UPDATES UI
                            // pop up window with two options
                            CharSequence options[] = new CharSequence[]{"ACCEPT", "REJECT"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(act);
                            builder.setCancelable(false);
                            builder.setTitle(received.split(",")[1]);
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // the user clicked on options[which]
                                    if (which == 0) {
                                        try {
                                            outToServer.writeBytes("Accept," + received.split(",")[2] +","+name+ '\n');
                                            oppName=received.split(",")[2];
                                        } catch (IOException e) {
                                        }
                                        Intent intent = new Intent(act, Main2Activity.class);
                                        intent.putExtra("color", 'w');
                                        intent.putExtra("opp", oppName);
                                        intent.putExtra("my", name);
                                        startActivity(intent);
                                        act.finish();
                                    }
                                    if (which == 1) {
                                        try {
                                            outToServer.writeBytes("Reject," + received.split(",")[2]
                                                    + '\n');
                                        } catch (IOException e) {
                                        }
                                    }
                                }
                            });
                            builder.show();
                        }
                    });
                }
                if (received.startsWith("close")) {
                    //work around for the invited player closing thread problem
                    condition=false;
                }
                if (received.startsWith("BATTLE")) {
                    condition=false;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            oppName=received.split(",")[1];
                            Intent intent = new Intent(act, Main2Activity.class);
                            intent.putExtra("color", 'b');
                            intent.putExtra("opp", oppName);
                            intent.putExtra("my", name);
                            startActivity(intent);
                            act.finish();
                        }
                    });
                }
                if (received.startsWith("Reject")) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "CHALLEGNE REJECTED",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
            }
        }
    }
}


