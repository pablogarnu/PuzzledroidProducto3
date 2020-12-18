/**
 * Clase con la lógica para cargar la imagen seleccionada, segmentarla, guardar los bitmaps como ficheros
 * en un directorio del almacenamiento interno del móvil
 */


package com.example.puzzledroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.puzzledroid.entidades.Jugador;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class ImageToSplit extends AppCompatActivity {


    /*
    * Atributos de la clase
    * */

    public static ArrayList<Bitmap> piezas;
    private static Jugador mijugador;
    private Boolean esmonojugador;
    private int niveljuego;
    private String uriImagen;
    private String midireccionArchivoFoto;
    private String nombrefichero;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_to_split);
        final ConstraintLayout layout=findViewById(R.id.imagetosplit);
        final ImageView myimage=findViewById(R.id.imgTosegment);
        Intent intent=getIntent();
        nombrefichero=intent.getStringExtra("assetName");
        mijugador=(Jugador)getIntent().getSerializableExtra("jugador_activo");
        niveljuego=getIntent().getExtras().getInt("niveljuego");
        esmonojugador=getIntent().getExtras().getBoolean("esmonojugador");
        uriImagen=getIntent().getStringExtra("ImagenUri");
        midireccionArchivoFoto=getIntent().getStringExtra("midireccionfoto");

        try {
            loadProperImageSource(myimage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //setImagenFromAsset(nombrefichero,myimage);
        myimage.post(new Runnable() {
            @Override
            public void run() {
                if (nombrefichero!=null){
                    layout.removeView(myimage);
                    layout.addView(myimage);
                }else if (uriImagen!=null){
                    layout.removeView(myimage);
                    layout.addView(myimage);
                }else if (midireccionArchivoFoto!=null){
                    layout.removeView(myimage);
                    layout.addView(myimage);
                }
                piezas=segmentaImagen();
            }
        });


        FloatingActionButton fab=(FloatingActionButton)findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Bucle para guardar las piezas como ficheros en directorio externo
                for(int i=0;i<piezas.size();i++){
                    try {
                        guardaBitmapEnFicheroInterno(piezas.get(i),i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent1=new Intent(ImageToSplit.this,Puzzle_view.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable("jugador_activo",mijugador);
                bundle.putInt("niveljuego",niveljuego);
                if(esmonojugador){
                    bundle.putBoolean("esmonojugador",true);
                }
                intent1.putExtras(bundle);
                startActivity(intent1);
                finish();

            }
        });
    }


    /*
    * Método que pondrá la foto cuyo nombre archivo hemos mandado en la imagen
    * */
    private void setImagenFromDireccionFoto(String midireccionArchivoFoto, ImageView myimage) {

        // Obtener las dimensiones del View

        //int anchura=myimage.getWidth();
        //int altura=myimage.getHeight();

        int anchura=myimage.getDrawable().getIntrinsicWidth();
        int altura=myimage.getDrawable().getIntrinsicHeight();

        // Obtener las dimensiones del bitmap

        BitmapFactory.Options bmOptions=new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(midireccionArchivoFoto,bmOptions);
        int anchuraFoto=bmOptions.outWidth;
        int alturaFoto=bmOptions.outHeight;

        // Determinar cuanto escalar la imagen

        int factorEscala=Math.min(anchuraFoto/anchura,alturaFoto/altura);

        // Decodificar el archivo de imagen en bitmap cuyo tamaño ocupe el View

        bmOptions.inJustDecodeBounds=false;
        bmOptions.inSampleSize=factorEscala;
        bmOptions.inPurgeable=true;

        Bitmap bitmap=BitmapFactory.decodeFile(midireccionArchivoFoto,bmOptions);
        Bitmap bitmapRotado=bitmap;

        // girar el bitmap si es necesario

        try {
            ExifInterface exifInterface=new ExifInterface(midireccionArchivoFoto);
            int orientacion=exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientacion){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmapRotado=giraImagen(bitmap,90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmapRotado=giraImagen(bitmap,180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmapRotado=giraImagen(bitmap,270);
                    break;
            } //End switch
        } catch (IOException e) {
            Toast.makeText(this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        myimage.setImageBitmap(bitmapRotado);

    } // End setImagenFromDireccionFoto

    /*
    * Método para girar bitmap los grados especificados
    * */


    private Bitmap giraImagen(Bitmap bitmap, float angulo){

        Matrix matrix=new Matrix();
        matrix.postRotate(angulo);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),
                matrix,true);
    } //End giraImagen


    /**
     * Método que coloca la imagen guardada en el directorio imagenes de assets en función de su
     * nombre y la coloca en el imageview como bitmap
     */

    private void setImagenFromAsset(String nombrefichero,ImageView imageView) {


        int anchura=imageView.getDrawable().getIntrinsicWidth();
        int altura=imageView.getDrawable().getIntrinsicHeight();

        AssetManager assetManager = getAssets();
        try{
            InputStream inputStream=assetManager.open("imagenes/"+nombrefichero);
            // Obtener las dimensiones del bitmap
            BitmapFactory.Options bmOptions=new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds=true;
            BitmapFactory.decodeStream(inputStream,new Rect(-1,-1,-1,-1),bmOptions);
            int anchurafoto=bmOptions.outWidth;
            int alturafoto=bmOptions.outHeight;

            //Calcular factor de escala a aplicar

            int factorEscala=Math.min(anchurafoto/anchura,alturafoto/altura);

            inputStream.reset();

            // Decodificar el archivo de imagen en bitmap que llene la imagen

            bmOptions.inJustDecodeBounds=false;
            bmOptions.inSampleSize=factorEscala;
            bmOptions.inPurgeable=true;

            Bitmap bitmap=BitmapFactory.decodeStream(inputStream,new Rect(-1,-1,-1,-1),bmOptions);
            imageView.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }//End setImagenFromAsset




    /**
     * Método que devuelve la imagen cargada como un array de bitmaps, teniendo en cuenta el nivel de juego
     * para dividirla en el número de piezas que corresponda.
     */


    private ArrayList<Bitmap> segmentaImagen() {

        int numpiezas;
        int filas=3;
        int columnas=3;

        //Establecemos como cambia el número de piezas de la matriz según nivel de juego
        switch (niveljuego){

            case 0: {
                break;
            }
            case 1: {
                filas=filas+1;
                break;
            }
            case 2:{
                filas=filas+1;
                columnas=columnas+1;
                break;
            }
        }
        numpiezas=filas*columnas;

        ImageView imageView=findViewById(R.id.imgTosegment);
        piezas=new ArrayList<>(numpiezas);

        //Obtener bitmap a partir del fichero de la imagen

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap mibitmap = drawable.getBitmap();

        //Obtener factores de corrección de la escala de las imágenes, asi como los de posición

        int[] dimensions = getBitmapPositionInsideImageView(imageView);
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int croppedImageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft);
        int croppedImageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(mibitmap, scaledBitmapWidth, scaledBitmapHeight, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, abs(scaledBitmapLeft), abs(scaledBitmapTop), croppedImageWidth, croppedImageHeight);


        //Calcular la altura y anchura de las piezas

        int anchuraColumna=croppedImageWidth/columnas;
        int alturaColumna=croppedImageHeight/filas;

        // crear cada pieza bitmap y añadirla al array de piezas

        int Ypos = 0;
        for (int i = 0; i < filas; i++) {
            int Xpos = 0;
            for (int j = 0; j < columnas; j++) {
                piezas.add(Bitmap.createBitmap(croppedBitmap, Xpos, Ypos, anchuraColumna, alturaColumna));
                Xpos += anchuraColumna;
            }
            Ypos += alturaColumna;
        }
        return piezas;
    } //End Segmenta Imagen


    /**
     * Método que nos permite obtener unos factores de corrección de escala para que las
     * imágenes segmentadas obtenidas sean proporcionales.
     */


    private int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Función que nos devuelve una matriz de valores asociados a una imageny
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extrae los valores de escala usando las constantes especificadas (el aspecto se mantiene si scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Obtener el drawable (obtener la anchura intriseca y su altura intrinseca, restando na altura de la barra de estado)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight()-getStatusBarHeight(getApplicationContext());

        // calcula dimensiones reales redondeando los decimales.
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);


        //asigna valores como factores de corrección
        ret[2] = actW;
        ret[3] = actH;

        // obtenemos la posición de la imagén
        // asumimos que la imagen está centrada en el imageview
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - actH)/2;
        int left = (int) (imgViewW - actW)/2;

        //asigna vlos valores de posición

        ret[0] = left;
        ret[1] = top;

        return ret;
    }


    /**
     * Método para guardar un bitmap como fichero en un directorio del almacenamiento interno. Cada
     * nivel de juego tiene su propio directorio
     */


    private void guardaBitmapEnFicheroInterno(Bitmap bitmap, int i) throws IOException {

        ContextWrapper contextWrapper=new ContextWrapper(getApplicationContext());
        String nombrefichero="imagen"+String.valueOf(11+i)+".png";

        // path to /data/data/yourapp/app_data/imageDir
        File directorioInterno= contextWrapper.getDir("dirImagenesLevel"+niveljuego, Context.MODE_PRIVATE);
        // fichero ubicado en nuevo directorio
        File archivoImagen=new File(directorioInterno,nombrefichero);

        FileOutputStream fileOutputStream=null;
        try{
                fileOutputStream = new FileOutputStream(archivoImagen);
                bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
        } catch (IOException e){
            e.printStackTrace();
        }finally {
            if (fileOutputStream!=null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }//end if
        }//end finally
    }// End guardaBitmapEnFicheroInterno


    /**
     * Método para obtener la altura de la barra de estado
     */
    private int getStatusBarHeight(Context context) {

        int result=0;
        int resourceId=context.getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId>0){
            result=context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    } // End getStatusBarHeight


    /**
     * Método para cargar la imagen en función de la fuente
     * selección imagen , galeria o camara foto
     */

    public void loadProperImageSource(ImageView imageView) throws FileNotFoundException {

            if(nombrefichero!=null){
                setImagenFromAsset(nombrefichero,imageView);
            }else if(uriImagen!=null){
                scaleGalleryImage(uriImagen,imageView);
            }else if(midireccionArchivoFoto!=null)
                setImagenFromDireccionFoto(midireccionArchivoFoto,imageView);
    }

    /*
    * Método para escalar correctamente la imagen que proviene de la galería
    * */

    public void scaleGalleryImage(String uriImagen,ImageView imageView) throws FileNotFoundException {

        // Obtener las dimensiones del View

        int anchura=imageView.getDrawable().getIntrinsicWidth();
        int altura=imageView.getDrawable().getIntrinsicHeight();

        // Ajustar la imagen como Uri a partir de su path

        imageView.setImageURI(Uri.parse(uriImagen));

        // Obtener las dimensiones después del ajuste

        int anchurapost=imageView.getDrawable().getIntrinsicWidth();
        int alturapost=imageView.getDrawable().getIntrinsicHeight();

        // crear bitmap partiendo de la imagen ajustada ai imageview
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap mibitmap = drawable.getBitmap();

        int factorEscala=Math.min(anchura/anchurapost,altura/alturapost);

        int anchurafinal=Math.round((float)factorEscala*anchurapost);
        int alturafinal=Math.round((float)factorEscala*alturapost);

        // Obtener nuevo bitmap escalado

        Bitmap nuevobitmap=Bitmap.createScaledBitmap(mibitmap,anchurafinal,alturafinal,true);
        imageView.setImageBitmap(nuevobitmap);

    }
}