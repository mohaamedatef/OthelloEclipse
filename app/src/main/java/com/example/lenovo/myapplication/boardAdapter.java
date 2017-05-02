package com.example.lenovo.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class boardAdapter extends BaseAdapter {

    private Context context;
    private char[][] chips;
    private static final char NO_CHIP = '-';
    private static final char BLACK = 'b';
    private static final char WHITE = 'w';
    private static final char HINT = '.';
    private char[] oneDarray;

    public boardAdapter(Context context, char[][] chips) {
        this.context = context;
        this.chips = chips;

        oneDarray = new char[64];
        int c = 0;
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                oneDarray[c] = chips[i][j];
                c++;
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView = null;

        if (convertView == null) {

            // currentCell.setLayoutParams(new GridView.LayoutParams(120, 120));
            // currentCell.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // currentCell.setPadding(8, 8, 8, 8);

            gridView = inflater.inflate(R.layout.cell, null);
            ImageView currentCell = (ImageView) gridView.findViewById(R.id.grid_item_image);

            if (oneDarray[position] == BLACK) {

                currentCell.setImageResource(R.drawable.black);
            }
            if (oneDarray[position] == WHITE) {

                currentCell.setImageResource(R.drawable.white);
            }
            if (oneDarray[position] == NO_CHIP) {

                currentCell.setImageResource(R.drawable.nochip);
            }
            if (oneDarray[position] == HINT) {

                currentCell.setImageResource(R.drawable.hint);
            }
        } else {
            gridView = (View) convertView;
        }
        return gridView;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return 64;
    }

}
