
/**
 * Clase con la lógica para logear a los jugadores en modo multijugador
 */


package com.example.puzzledroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.puzzledroid.entidades.Jugador;
import com.example.puzzledroid.utilidades.Utilidades;

public class loginActivity extends AppCompatActivity {

    /*
    * Atributos de la clase
    * */

    private EditText mylogin;
    private Button btnLogin;
    private Jugador mijugador;
    //private static String loginPassword="admin";

    ConexionSQLite conexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        conexion=new ConexionSQLite(getApplicationContext(),"bd_jugadores",null,1);

        mylogin = (EditText) findViewById(R.id.edtLogin);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        checkLogin();


    } // End onCreate

    /**
     * Método que se ejecuta al clicar en botón de login, comprobando si está registrado
     */

    public void checkLogin(){

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mylogin.getText().toString().trim().length()==0){
                    Toast.makeText(getApplicationContext(), "El campo de login esta vacio", Toast.LENGTH_SHORT).show();
                }else{

                    if (loginRegistrado()) {
                        mijugador=getMijugador(mylogin.getText().toString());
                        Intent intent = new Intent(loginActivity.this, PuzzleList.class);
                        intent.putExtra("jugador_activo",mijugador); // mandamos como parámetro el jugador que se ha logeado correctamente
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Login incorrecto o usuario no registrado", Toast.LENGTH_SHORT).show();
                        mylogin.setText("");
                    }
                }
            }
        });
    } //End checklogin


    /**
     * Método que se ejecuta al clicar en botón de registro, activando la pantalla de registro.
     */


    public void activateRegistro(View view){

        Intent intent=new Intent(loginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }


    /**
     * Método para consultar si encuentra el login en la tabla de jugadores.
     */

    public boolean loginRegistrado() {

        SQLiteDatabase db = conexion.getReadableDatabase();
        String[] parametros = {mylogin.getText().toString()};
        String[] campos = {Utilidades.CAMPO_IDJUGADOR,Utilidades.CAMPO_NICKNAME,Utilidades.CAMPO_PASSWORD};

        Cursor cursor = db.query(Utilidades.TABLA_JUGADOR, campos, Utilidades.CAMPO_PASSWORD + "=?", parametros, null, null, null);
        if (cursor != null && cursor.moveToFirst() && cursor.getCount()>0){
            cursor.close();
            db.close();
            return true;}
        db.close();
        return false;
    } //End loginRegistrado


    /**
     * Método que devuelve el objeto jugador asociado a un determinado login.
     */


    public Jugador getMijugador(String mipasword){
        SQLiteDatabase db = conexion.getReadableDatabase();
        String[] parametros = {mipasword};
        String[] campos = {Utilidades.CAMPO_IDJUGADOR,Utilidades.CAMPO_NICKNAME,Utilidades.CAMPO_PASSWORD};
        try {
            Cursor cursor = db.query(Utilidades.TABLA_JUGADOR, campos, Utilidades.CAMPO_PASSWORD + "=?", parametros, null, null, null);
            cursor.moveToFirst();
            mijugador = new Jugador(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            cursor.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"No existe ningun jugador con ese login",Toast.LENGTH_SHORT).show();
        }
        db.close();
        return mijugador;
    }

}//End Class loginActivity