/**
 * Clase para gestionar las conexiones a la base de datos SQLITE
 */


package com.example.puzzledroid;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.puzzledroid.entidades.Jugada;
import com.example.puzzledroid.entidades.Jugador;
import com.example.puzzledroid.utilidades.Utilidades;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ConexionSQLite extends SQLiteOpenHelper {

    // El constructor crea la base de datos
    public ConexionSQLite(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // genera las tablas asociadas a las entidades con la instrucción correspondiente
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(Utilidades.CREAR_TABLA_JUGADOR);
        db.execSQL(Utilidades.CREAR_TABLA_JUGADA);
    }

    //Verifica la existencia de versiones anteriores de la base de datos cada vez que volvemos a ejecutar la aplicación
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+Utilidades.TABLA_JUGADOR);
        db.execSQL("DROP TABLE IF EXISTS "+Utilidades.TABLA_JUGADA);
        onCreate(db);
    }


    //Método para obtener una lista de todos los jugadores

    public ArrayList<Jugador> getListaJugadores(){
        ArrayList<Jugador> jugadores=new ArrayList<>();
        String query="SELECT * FROM "+Utilidades.TABLA_JUGADOR;
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor registros=db.rawQuery(query,null);
        while(registros.moveToNext()){
            Jugador mijugador=new Jugador();
            mijugador.setIdJugador(registros.getInt(0));
            mijugador.setNickname(registros.getString(1));
            mijugador.setPassword(registros.getString(2));
            jugadores.add(mijugador);
        }
        db.close();
        return jugadores;
    }//End getListaJugadores()

    //Método para obtener una lista de todos las jugadas

    public ArrayList<Jugada> getListaJugadas(){
        ArrayList<Jugada> jugadas=new ArrayList<>();
        String query="SELECT * FROM "+Utilidades.TABLA_JUGADA;
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor registros=db.rawQuery(query,null);
        while(registros.moveToNext()){
            Jugada mijugada=new Jugada();
            mijugada.setIdJugada(registros.getInt(0));
            mijugada.setIdGamer(registros.getInt(1));
            mijugada.setFecha(convierteStringToDate(registros.getString(2)));
            mijugada.setDuracion(registros.getInt(3));
            mijugada.setPuntos(registros.getInt(4));
            jugadas.add(mijugada);
        }
        db.close();
        return jugadas;
    }//End getListaJugadas()


    public Date convierteStringToDate(String fechastring){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date d=null;
        try {
            d=  dateFormat.parse(fechastring);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return d;
    } //End convierteStringToDate

}
