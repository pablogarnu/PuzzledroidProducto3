/*
* Clase Adapter empleada para llenar el gridview de la clase PuzzleList
* */


package com.example.puzzledroid;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class ImageAdapter extends BaseAdapter {

    /*
    * Atributos de la clase
    *
    * */

    private Context context;
    private AssetManager assetManager;
    private String[] archivos;

    /*
    *  Constructor de la clase
    * */

    public ImageAdapter(Context context) {
        this.context=context;
       assetManager=this.context.getAssets();
       try {
           archivos=assetManager.list("imagenes");
       }catch (IOException e){
           e.printStackTrace();
       }
    }

    @Override
    public int getCount() {
        return archivos.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /*
     *  Con este método creamos una ImageView para cada item referenciado por
     *  el Adapter
     * */

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            final LayoutInflater layoutInflater= LayoutInflater.from(this.context);
            convertView=layoutInflater.inflate(R.layout.grid_item,null);
        }
        final ImageView imageView=convertView.findViewById(R.id.gridImageView);
        imageView.setImageBitmap(null);

        // Ejecuta codigo asociado a la imagen antes de que esta sea dispuesta
        imageView.post(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<Void,Void,Void>(){
                    private Bitmap bitmap;
                    @Override
                    protected Void doInBackground(Void... voids) {
                        bitmap = getImageFromAsset(imageView,archivos[position]);
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void aVoid){
                        super.onPostExecute(aVoid);
                        imageView.setImageBitmap(bitmap);
                    }
                }.execute();
            }
        });

        return convertView;
    } // End getView

    private Bitmap getImageFromAsset(ImageView imageView,String nombreAsset){

        // Obtener dimensiones del View
        int anchura=imageView.getWidth();
        int altura=imageView.getHeight();
        if(anchura==0 ||altura==0){
            return null;
        }
        try {
            InputStream inputStream=assetManager.open("imagenes/"+nombreAsset);

            //Obtener las dimensiones del bitmap

            BitmapFactory.Options bmOptions=new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds=true;
            BitmapFactory.decodeStream(inputStream,new Rect(-1,-1,-1,-1),bmOptions);
            int anchuraFoto=bmOptions.outWidth;
            int alturafoto=bmOptions.outHeight;

            // Obtener el escalado necesario para la imagen

            int factorEscala=Math.min(anchuraFoto/anchura,alturafoto/altura);
            inputStream.reset();

            // decodificar el archivo imagen en un Bitmap dimensionado para llenar el View

            bmOptions.inJustDecodeBounds=false;
            bmOptions.inSampleSize=factorEscala;
            bmOptions.inPurgeable=true;
            return BitmapFactory.decodeStream(inputStream,new Rect(-1,-1,-1,-1),bmOptions);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }// End getImageFromAsset



} // End class ImageAdapter


