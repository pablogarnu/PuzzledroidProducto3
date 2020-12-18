

package com.example.puzzledroid;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.example.puzzledroid.utilidades.Utilidades;

public class RegisterActivity extends AppCompatActivity {

    /*
    * Atributos de la clase
    * */

    private EditText myedtnickname;
    private EditText myedtloginPassword;
    private Button mybutton;

    ConexionSQLite conexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

     myedtnickname=(EditText)findViewById(R.id.edtNickname);
     myedtloginPassword=(EditText)findViewById(R.id.edtPassword);
     mybutton=(Button)findViewById(R.id.btnSaveRegistry);

     conexion=new ConexionSQLite(this,"bd_jugadores",null,1);

    }


    //Método que GUARDA el usuario cuando clicamos en el boton GUARDAR

    public void guardaJugador(View view){

        //conexion=new ConexionSQLite(this,"bd_jugadores",null,1);
        SQLiteDatabase db=conexion.getWritableDatabase();
        //insert into jugador (nickname,password) values ('admin','admin');
        String insert="INSERT INTO "+Utilidades.TABLA_JUGADOR+" ( "+Utilidades.CAMPO_NICKNAME+","+
                Utilidades.CAMPO_PASSWORD+") VALUES ('"+myedtnickname.getText().toString()+"','"
                +myedtloginPassword.getText().toString()+"')";
        db.execSQL(insert);
        db.close();
        finish();
    } //End guardaJugador


} //End class RegisterActivity