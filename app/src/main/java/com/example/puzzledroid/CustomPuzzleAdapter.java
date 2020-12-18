/*
 * Clase Adapter empleada para llenar el gridview de la clase Puzzleview
 * */


package com.example.puzzledroid;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;

import java.util.ArrayList;

public class CustomPuzzleAdapter extends BaseAdapter {

    //Atributos de la clase

    private ArrayList<ImageButton> buttons=null;
    private int anchuraColumna,alturaColumna;
    private ImageView imageView;

   public CustomPuzzleAdapter(ArrayList<ImageButton> buttons, int anchuraColumna, int alturaColumna, ImageView imageView) {
        this.buttons = buttons;
        this.anchuraColumna = anchuraColumna;
        this.alturaColumna = alturaColumna;
        this.imageView = imageView;
    }

    public CustomPuzzleAdapter(ArrayList<ImageButton> buttons, int anchuraColumna, int alturaColumna) {
        this.buttons=buttons;
        this.alturaColumna=alturaColumna;
        this.anchuraColumna=anchuraColumna;
    }

    @Override
    public int getCount() {
        return this.buttons.size();
    }

    @Override
    public Object getItem(int position) {
        return (Object) this.buttons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageButton button;
        if (convertView==null){
            button=this.buttons.get(position);
        }else{
            button=(ImageButton) convertView;
        }
        AbsListView.LayoutParams params=new AbsListView.LayoutParams(this.anchuraColumna,this.alturaColumna);
        button.setLayoutParams(params);
        return button;
    }
}
