package com.example.lenovo.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class Main2Activity extends Activity implements Runnable {

    private final char NO_CHIP = '-';
    private final char BLACK = 'b';
    private final char WHITE = 'w';
    private final char HINT = '.';
    private final int BOARD_SIZE = 8;
    private char[][] board = new char[BOARD_SIZE][BOARD_SIZE];

    private char myColor;
    private char oppColor;
    private String oppName;
    private String myName;
    private String name;

    private boolean condition = true;
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private String received;

    private Activity act=this;
    private GridView gridView;
    private TextView textView;
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Bundle bundle = getIntent().getExtras();

        myColor = bundle.getChar("color");
        oppColor = this.getColoropp();
        oppName = bundle.getString("opp");
        myName=bundle.getString("my");
        String name = bundle.getString("Name");
        this.name = name;

        this.intiate();
        gridView = (GridView) findViewById(R.id.board);
        gridView.setAdapter(new boardAdapter(this, board));

        button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    outToServer.writeBytes("Quit,"+((TextView) textView).getText());
                }catch (IOException e){

                }
            }
        });




        textView=(TextView)findViewById(R.id.score);
        textView.setText("My name: "+myName+"  Opp name: "+oppName);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int y = position % 8;
                int x = (position - y) / 8;
                //Log.e("debug", "you clicked" + x + "," + y + "," + position);
                //block ui if clicked on no chip (in either turns)
                if (board[x][y] != HINT) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "This is Not a Valid move",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    fill(x,y,myColor,oppColor);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            gridView.setAdapter(new boardAdapter(act, board));
                            textView.setText("My name: "+myName+"  Opp name: "+oppName+
                                    "  Black: "+blackCount()+"  White :"+whiteCount());
                        }
                    });
                    String z = sendBoardToServer(board);
                    z=oppName + ","+ z;
                    try {
                        outToServer.writeBytes("Game,"+ z + '\n');
                    } catch (IOException e) {
                    }
                }
            }
        });

        //trigger first move
        firstMove();
        Thread x = new Thread(this);
        x.start();
    }
//    @Override
//    public void onConfigurationChanged(Configuration newConfig){
//        super.onConfigurationChanged(newConfig);
//        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
//            Toast.makeText(this,"LANDSCAPE", Toast.LENGTH_SHORT).show();
//        }else if (newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(this, "PORTRAIT", Toast.LENGTH_SHORT).show();
//        }
//    }


    private void firstMove() {
        if (myColor == BLACK) {
               displayValidMoves(myColor,oppColor);
            runOnUiThread(new Runnable() {
                public void run() {
                    gridView.setAdapter(new boardAdapter(act, board));
                }
            });
        }
    }

    public String sendBoardToServer(char[][] chips) {
        String board = "";
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                board += chips[i][j] + ",";
            }
        }
        board = board.substring(0, board.length() - 1);
        return board;
    }

    public char[][] getBoardFromServer(String map) {
        String[] x = map.split(",");
        char[][] chips = new char[BOARD_SIZE][BOARD_SIZE];
        int c = 0;
        for (int j = 0; j < chips.length; j++) {
            for (int k = 0; k < chips[j].length; k++) {
                chips[j][k] = x[c].charAt(0);
                c++;
            }
        }
        return chips;
    }

    @Override
    public void run() {

        this.inFromServer = MainActivity.getInFromServer();
        this.outToServer = MainActivity.getOutToServer();

        while (condition) {
            try {
                received = inFromServer.readLine();

                if (received.startsWith("List")) {
                    //someone logged out,update list later
                } else {

                    board = getBoardFromServer(received);
                    displayValidMoves(myColor, oppColor);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            gridView.setAdapter(new boardAdapter(act, board));
                        }
                    });
                    if (movesCount() != 0) {
                        //play
                    } else {
                        String x = sendBoardToServer(this.board);
                        x = oppName + "," + x;
                        outToServer.writeBytes("Game," + x + '\n');
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //a method that gets called whenever the server sends a specific keyword
            //method betb3at lel server te2olo meen el tele3 w tehadelo el opponenet name
            // w bet-declare el opponenet as a winner
            if (received.startsWith("Winner")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //an alert dialog with a Yes or No option
                        AlertDialog.Builder builder = new AlertDialog.Builder(act);
                        builder.setCancelable(false);
                        builder.setTitle("Confirm Exit");
                        builder.setIcon(R.drawable.question);
                        builder.setMessage("Are you sure you want to exit?"+'\n'+" Other opponent will win if you do so");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    outToServer.writeBytes("End," + received.split(",")[2] +","+name+ '\n');
                                    oppName= received.split(",")[2];
                                }catch (IOException e){

                                }
                                Intent intent = new Intent(act, ListActivity.class);
                                startActivity(intent);
                                act.finish();

                            }
                        });
                        builder.show();


                    }
                });

            }
            if (received.startsWith("Ready")){
                //declare winner, set condition to true, wait some time, and open listactivity intent
            }
        }
    }
    public int movesCount()
    {
        int c=0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if(board[i][j] == HINT)
                      c++;
            }
        }
        return c;
    }

    public char getColoropp() {
        if (myColor == BLACK)
            return WHITE;
        return BLACK;
    }

    public void intiate() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = NO_CHIP;
            }
        }
        board[4][3] = BLACK;
        board[3][4] = BLACK;
        board[4][4] = WHITE;
        board[3][3] = WHITE;
    }

    public int blackCount() {
        int c = 0;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == BLACK)
                    c++;
            }
        }
        return c;
    }

    public int whiteCount() {
        int c = 0;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == WHITE)
                    c++;
            }
        }
        return c;
    }

    public void displayValidMoves(char myCol, char oppCol) {

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == NO_CHIP) {
                    markIfValid(i, j, myCol, oppCol);
                }
            }
        }
    }

    public void markIfValid(int row, int col, char myColor, char oppColor) {
        // check 8 dimensions must one atleast have the opp color
        // AND hit my color at the end of that direction
        for (int deltaRow = -1; deltaRow < 2; deltaRow++) {
            for (int deltaCol = -1; deltaCol < 2; deltaCol++) {

                int currentRow = 0;
                currentRow = row + deltaRow;
                int currentCol = 0;
                currentCol = col + deltaCol;

                while (checkBounds(currentRow, currentCol) && board[currentRow][currentCol] == oppColor) {

                    currentRow += deltaRow;
                    currentCol += deltaCol;

                    if (checkBounds(currentRow, currentCol) && board[currentRow][currentCol] == myColor) {
                        board[row][col] = '.';
                        return;
                    }
                }
            }
        }
    }

    public boolean checkBounds(int i, int j) {
        if (i >= 0 && i <= 7 && j >= 0 && j <= 7)
            return true;
        return false;
    }

    public void fill(int row, int col, char myColor, char oppColor) {

        board[row][col] = myColor;

        for (int deltaRow = -1; deltaRow < 2; deltaRow++) {
            for (int deltaCol = -1; deltaCol < 2; deltaCol++) {

                int currentRow = 0;
                currentRow = row + deltaRow;
                int currentCol = 0;
                currentCol = col + deltaCol;

                int currentRow1 = currentRow;
                int currentCol1 = currentCol;
                boolean changeColor = false;

                while (checkBounds(currentRow1, currentCol1) && board[currentRow1][currentCol1] == oppColor) {

                    currentRow1 += deltaRow;
                    currentCol1 += deltaCol;
                    if (checkBounds(currentRow1, currentCol1) && board[currentRow1][currentCol1] == myColor) {
                        changeColor = true;
                    }
                }
                while (changeColor && board[currentRow][currentCol] == oppColor) {

                    board[currentRow][currentCol] = myColor;
                    currentRow += deltaRow;
                    currentCol += deltaCol;

                }
            }
        }

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == '.') {
                    board[i][j] = NO_CHIP;
                }
            }
        }
    }
}
