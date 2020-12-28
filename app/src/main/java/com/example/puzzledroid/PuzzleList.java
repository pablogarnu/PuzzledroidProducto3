/**
 * Clase con la lógica para mostrar las imágenes representativas de los puzzles y asignar el nivel
 * de dificultad del juego
 */


package com.example.puzzledroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.puzzledroid.entidades.Jugador;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class PuzzleList extends AppCompatActivity {

    /*
    * Atributo de la clase
    * */

    private Jugador mijugador;
    private RadioGroup radioGroupNiveles;
    private Boolean esmonojugador;
    private RadioButton rdblevel1;
    private RadioButton rdblevel2;
    private RadioButton rdblevel3;
    private int niveljuego;
    String midireccionfoto;
    String idioma;
    private ArrayList<Integer> ImagesToQuit=new ArrayList<>();

    static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE=2;
    static final int REQUEST_IMAGE_CAPTURE=1;
    static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE=3;
    static final int REQUEST_IMAGE_GALLERY=4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        esmonojugador=getIntent().getExtras().getBoolean("esmonojugador");
        mijugador=(Jugador)getIntent().getSerializableExtra("jugador_activo");
        idioma=getIntent().getExtras().getString("idioma");

        setContentView(R.layout.activity_puzzle_list);
        radioGroupNiveles=findViewById(R.id.id_RdGrpupNiveles);
        rdblevel1=findViewById(R.id.id_rdblevel1);
        rdblevel2=findViewById(R.id.id_rdblevel2);
        rdblevel3=findViewById(R.id.id_rdblevel3);

        AssetManager assetManager=getAssets();
        GridView gridView=findViewById(R.id.gridImagenes);


        try {
                final String[] ficheros= assetManager.list("imagenes");
                gridView.setAdapter(new ImageAdapter(this));

                //Añadir Listener al Gridview
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    niveljuego=setLevelToJugador();
                    Intent intent=new Intent(getApplicationContext(),ImageToSplit.class);
                    intent.putExtra("assetName",ficheros[position % ficheros.length]);
                    intent.putExtra("niveljuego", niveljuego);
                    intent.putExtra("idioma",idioma);

                    //solo si es multijugador le pasa el objeto jugador
                    if(!esmonojugador) {
                        intent.putExtra("jugador_activo", mijugador);
                    }else
                    {
                        intent.putExtra("esmonojugador",true);
                    }
                    startActivity(intent);
                }
            });
            } catch (IOException e) {
                Toast.makeText(this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

        }//End onCreate


    /**
     * Método que asigna el nivel de juego en función del radiobutton clicado.
     */


    public int setLevelToJugador(){
        int level = 0;
        if(rdblevel1.isChecked()){
            level=0;
        }
        if (rdblevel2.isChecked()){
            level=1;
        }
        if (rdblevel3.isChecked()){
            level=2;
        }
        return level;
    }//end setLevelToJugador


    /*
     * Método para seleccionar imagen de galeria al clickar el floating button de galería
     * */

    public void selectImagenFromGaleria(View view) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        } else {


            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);

        }
    }

    /*
    * Método que devuelve un array de imágenes contenidas en la galeria
    * */

    public ArrayList<String> getGaleriaImagenes(){

        final ArrayList<String> milistaImagenes=new ArrayList<>();
        String[] projection=new String[]{MediaStore.Images.Media.DATA,};
        Uri imagenes=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor=getContentResolver().query(imagenes,projection,null,null,null);
        if(cursor.moveToFirst()){
            int indiceColumna=cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                milistaImagenes.add(cursor.getString(indiceColumna));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return milistaImagenes;
    }

    /*
    * Método para devolver un elemento aleatorio del array de Strings
    * */

    public String getRandomImage(){
        Random r=new Random();
        int valor=r.nextInt(getGaleriaImagenes().size());
        while(ImagesToQuit.contains(valor)){
            valor=r.nextInt(getGaleriaImagenes().size());
        }
        String nombreimagen=getGaleriaImagenes().get(valor);
        return nombreimagen;
    }

    /*
    * Método que se activa al clicar en boton cámara
    * */

    public void takefotoCamara(View view) throws IOException{
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            File archivofoto=null;
            archivofoto=creaArchivoFoto();
            if (archivofoto!=null){
                Uri urifoto= FileProvider.getUriForFile(this,getApplicationContext().getPackageName()
                                +".fileprovider",archivofoto);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,urifoto);
                startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    /*
     * Método para crear el archivo en el que guardar la imagen
     * */

    private File creaArchivoFoto() throws IOException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // permission not granted, initiate request
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            // crea nombre de archivo para imagen
            String marcatiempo = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreArchivoImagen = "JPEG_" + marcatiempo + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imagen = File.createTempFile(
                    nombreArchivoImagen,  /* prefijo */
                    ".jpg",         /* sufijo */
                    storageDir      /* directorio */
            );
            midireccionfoto = imagen.getAbsolutePath(); // guarda para usuarlo en el intent

            return imagen;
        }

        return null;
    }

    @Override
    public void onRequestPermissionsResult(int codigoPedido, @NonNull String[] permissions
                                          ,@NonNull int[] grantResults)  {
        super.onRequestPermissionsResult(codigoPedido,permissions,grantResults);
        switch (codigoPedido){

            case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:{
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                    try {
                        takefotoCamara(new View(this));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return;
            } //End case
        }//End Switch
    }//End OnRequestPermissionResult

    /*
    * Método que recibe la imagen de la cámara o de la galería para enviarla a la actividad ImageToSplit
    * */

    @Override
    protected void onActivityResult(int codigoPedido, int codigoResultado, Intent datos) {
        niveljuego=setLevelToJugador();
        super.onActivityResult(codigoPedido, codigoResultado, datos);
        if (codigoPedido == REQUEST_IMAGE_CAPTURE && codigoResultado == RESULT_OK  ) {
            Intent intent = new Intent(this, ImageToSplit.class);
            intent.putExtra("niveljuego", niveljuego);
            intent.putExtra("idioma",idioma);
            //solo si es multijugador le pasa el objeto jugador
            if(!esmonojugador) {
                intent.putExtra("jugador_activo", mijugador);
            }else
            {
                intent.putExtra("esmonojugador",true);
            }
            intent.putExtra("midireccionfoto", midireccionfoto);
            startActivity(intent);
        }

        if (codigoPedido == REQUEST_IMAGE_GALLERY && codigoResultado == RESULT_OK) {

            Intent intent = new Intent(this, ImageToSplit.class);
            intent.putExtra("ImagenUri", getRandomImage());
            intent.putExtra("idioma",idioma);
            //solo si es multijugador le pasa el objeto jugador
            if(!esmonojugador) {
                intent.putExtra("jugador_activo", mijugador);
            }else
            {
                intent.putExtra("esmonojugador",true);
            }
            startActivity(intent);
        }
    }


}//End class PuzzleList
