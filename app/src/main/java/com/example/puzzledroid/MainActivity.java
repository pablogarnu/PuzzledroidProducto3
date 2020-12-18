/**
 * Clase principal con la lógica para el inicio y los modos del juego
 */




package com.example.puzzledroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    //Atributos de la clase

    private MediaPlayerService playerService;
    public boolean serviceBound=false;
    public ArrayList<Audio> playlist;
    public static final String Broadcast_PLAY_NEW_AUDIO="com.example.puzzledroid.PlayNewAudio";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadAudio();
        //playAudio(playlist.get(0).getData());

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound){
            unbindService(serviceConnection);
            //el servicio está activo
            playerService.stopSelf();
        }
        playerService.onDestroy();
    }

    /**
     * Método para mostrar y ocultar el menu en el action bar
     */

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.help_actionbar,menu);
        return true;}


    /**
     * Método para asignar función a menu de Ayuda
     */

    public boolean onOptionsItemSelected(MenuItem item){

        int i=item.getItemId();
        if(i==R.id.item_help){
            //funcion a ejecutar
            Intent intent=new Intent(this,WebViewActivity.class);
            startActivity(intent);
        }else if (i==R.id.item_playlist){
            if(playlist.size()>0){
                playAudio(3); //melodia oficial
            }
        }
            return super.onOptionsItemSelected(item);
    }

    /**
     * Método para activar pantalla puzzlelist en modo monojugador
     */

    public void activaMonojugador(View view){
        Boolean esmonojugador=true;
        Intent intent=new Intent(MainActivity.this,PuzzleList.class);
        intent.putExtra("esmonojugador",esmonojugador);
        startActivity(intent);
    }

    /**
     * Método para activar multijugador ( provisionalmente activamos el login )
     */

    public void activaMultijugador(View view){
        Intent intent=new Intent(MainActivity.this,loginActivity.class);
        startActivity(intent);
    }

    /**
     * Método para salir de la aplicación
     */

    public void cerrarAplicacion(View view){
        MainActivity.this.finish();
        System.exit(0);
    }

    /**
     * Método para activar pantalla ListarRecords y mostrar los 10 primeros en el ranking
     */

    public void mostrarListaRecords(View view){
        Intent intent=new Intent(MainActivity.this,ListarRecords.class);
        startActivity(intent);
    }


    /*
     * Método para vincular este cliente con el servicio de com.example.puzzledroid.Audio
     * */

    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Hemos vinculado el LocalService, casteamos el IBinder and obtenemos la instancia LocalService
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, "Servicio vinculado", Toast.LENGTH_SHORT).show();
        } //end onServiceConnected


        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound=false;
        }
    };


    /*
     * Método para guardar estado del serviceBound
     * */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("ServiceState",serviceBound);
        super.onSaveInstanceState(outState);
    }

    /*
     * Método para restaurar el estado del serviceBound
     * */

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound=savedInstanceState.getBoolean("ServiceState");
    }


    /*
     * Método para extraer datos de audio del movil en orden ascendente
     *
     * */
    private void loadAudio(){

        ContentResolver contentResolver=this.getContentResolver();
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection=MediaStore.Audio.Media.IS_MUSIC+"!= 0";
        String sortOrder=MediaStore.Audio.Media.TITLE+" ASC";
        Cursor cursor=contentResolver.query(uri,null,selection,null,sortOrder);

        if(cursor!=null && cursor.getCount()>0){
            playlist=new ArrayList<>();
            while(cursor.moveToNext()){
                String data=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                //Guardamos en el audiolist
                playlist.add(new Audio(data,title,album,artist));
            }//End While
        }//End if
        cursor.close();
    }//End loadAudio


    /*
     * Método que crea una instancia de MediaPlayerService y manda archivo de audo para su
     * reporducción
     * */

    public void playAudio(int audioIndex){

        if (!serviceBound){
            //Almacena la audioList en SharedPreferences
            StorageUtil storage=new StorageUtil(getApplicationContext());
            storage.storeAudio(playlist);
            storage.storeAudioIndex(audioIndex);
            Intent playerIntent = new Intent(this,MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent,serviceConnection, Context.BIND_AUTO_CREATE);

        }else{

            //Almacena audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Servicio activo
            // Manda archivo con BroadcastReceiver -> PLAY_NEW_AUDIO
            Intent broadcastIntent=new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }//End PlayAudio

}//End class MainActivity