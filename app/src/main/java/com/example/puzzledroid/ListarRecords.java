
/**
 * Clase con la lógica para registrar a los jugadores en modo multijugador
 */


package com.example.puzzledroid;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.puzzledroid.entidades.Jugada;
import com.example.puzzledroid.entidades.Jugador;
import com.example.puzzledroid.utilidades.Utilidades;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListarRecords extends AppCompatActivity {

    /*
     * Atributos de la clase
     * */

    ListView recordlist;
    ArrayList<String> informjugada;
    ArrayList<Jugada> listajugadas;
    ArrayList<Jugador> listajugadores;

    ConexionSQLite conexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_records);

        conexion=new ConexionSQLite(getApplicationContext(),"bd_jugadores",null,1);
        recordlist=findViewById(R.id.idRecordList);

        consultarListaJugadas();
        ArrayAdapter arrayAdapter=new ArrayAdapter(this, android.R.layout.simple_list_item_1,informjugada);
        recordlist.setAdapter(arrayAdapter);
    }

    /**
     * Método para consultar la lista de jugadas en la base de datos y añadir los registros en los
     * correspondientes arrayList de jugadores y de jugadas
     */

    private void consultarListaJugadas() {

        SQLiteDatabase db=conexion.getReadableDatabase();
        Jugador mijugador;
        Jugada mijugada;
        listajugadas=new ArrayList<Jugada>();
        listajugadores=new ArrayList<Jugador>();

        //select * from jugador inner join jugada where idjugador=idgamer order by puntos DESC;

        Cursor cursor=db.rawQuery("SELECT * FROM "+Utilidades.TABLA_JUGADOR+" INNER JOIN "+Utilidades.TABLA_JUGADA+
                " ON "+Utilidades.TABLA_JUGADA+"."+Utilidades.CAMPO_IDGAMER+"="+
                Utilidades.TABLA_JUGADOR+"."+Utilidades.CAMPO_IDJUGADOR+" ORDER BY "+Utilidades.CAMPO_PUNTOS+
                " DESC",null);

        while (cursor.moveToNext()){

            mijugada=new Jugada();
            mijugada.setIdJugada(cursor.getInt(3));
            mijugada.setIdGamer(cursor.getInt(4));
            String fecha=cursor.getString(5);
            mijugada.setFecha(convierteStringToDate(fecha));
            mijugada.setDuracion(cursor.getInt(6));
            mijugada.setPuntos(cursor.getInt(7));

            listajugadas.add(mijugada);

            mijugador=new Jugador();
            mijugador.setIdJugador(cursor.getInt(0));
            mijugador.setNickname(cursor.getString(1));
            mijugador.setPassword(cursor.getString(2));

            listajugadores.add(mijugador);

        }//end while

        obtenerLista();

    }//End consultarListaJugadas

    /**
     * Método para transformar un objeto tipo String a tipo Date
     */

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

    /**
     * Método para crear items con la información del jugador y la jugada que nos interesa y
     * cargarlos en el arraylist de información con un determinado formato
     */

    private void obtenerLista() {

        informjugada=new ArrayList<String>();
        int numItemsMostrados;
        if(listajugadas.size()>10){
            numItemsMostrados=10;
        }else{
            numItemsMostrados=listajugadas.size();
        }

        for(int i=0;i<numItemsMostrados;i++){

            informjugada.add("\nNickname: "+listajugadores.get(i).getNickname()+"      Puntos: "+listajugadas.get(i).getPuntos()+"\nFecha: "+
                    listajugadas.get(i).getFecha()+"\n");
        }

    }// End obtenerLista

}// End class ListarRecords