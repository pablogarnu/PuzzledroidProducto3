/**
 * Clase con la lógica para
 */


package com.example.puzzledroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.puzzledroid.entidades.Jugada;
import com.example.puzzledroid.entidades.Jugador;
import com.example.puzzledroid.utilidades.Utilidades;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

import static com.example.puzzledroid.App.CHANNEL_RECORDS_ID;

public class Puzzle_view extends AppCompatActivity {

    /**
     * Atributos de la clase
     */

    private static moveGridView myGridview;
    private int filas;
    private int columnas;
    private int dimensiones;

    public static final String up = "up";
    public static final String down = "down";
    public static final String left = "left";
    public static final String right = "right";

    private static int anchuraColumna,alturaColumna;
    private static String[] listaPiezas;

    private static Jugador mijugador;
    public static ArrayList<Bitmap> piezas;
    private static long tini; // variable para guardar el instante en el que se inicia la aplicación
    private static long tfin; // variable para guardar el instante en el que se resuelve el puzzle
    private static SimpleDateFormat sdf; // objeto para dar formato a fechas
    private static Date mifecha;
    private int niveljuego;
    private Boolean esmonojugador;
    private NotificationManagerCompat notificationManager;
    private int record;

    public static ConexionSQLite conexion;

    // crear variable global tipo sounpool

    private SoundPool soundPool;
    private int pieceMovementSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_view);

        record=getNewRecord();

        // Crear notification manager

        notificationManager=NotificationManagerCompat.from(this);


        Bundle mibundle = this.getIntent().getExtras();
        if (mibundle != null) {
            mijugador = (Jugador) mibundle.getSerializable("jugador_activo");
            niveljuego=mibundle.getInt("niveljuego");
            esmonojugador=mibundle.getBoolean("esmonojugador");
        }
        try {
            convertFilesToBitmaps();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        iniciar();
        revolverPiezas();
        setDimensiones();

        //Registra tiempo inicio jugada

        tini = System.currentTimeMillis();
        sdf = new SimpleDateFormat("dd/MM/yyyy");


    }// End onCreate



    /*
    * En el método ondestroy activo la carga de puntuación de la jugada en el calendario
    * */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!esmonojugador) {
            ArrayList<Jugada> misjugadas = conexion.getListaJugadas();
            Jugada lastjugada = misjugadas.get(misjugadas.size() - 1);
            String contenido = mijugador.getNickname() + " puntos:" + lastjugada.getPuntos();

            // crear y mostrar notificacion
           if (getNewRecord()>record){
               record=getNewRecord();
                Notification notification=new NotificationCompat.Builder(this,CHANNEL_RECORDS_ID)
                    .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
                    .setContentText(record+"")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
                notificationManager.notify(1,notification);
            }

            /*Cargar contenido en calendario mediante intent*/

            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setData(CalendarContract.Events.CONTENT_URI);
            intent.putExtra(CalendarContract.Events.TITLE, contenido);
            intent.putExtra(CalendarContract.Events.ALL_DAY,true);
            startActivity(intent);
        }
    }//End ondestroy

    /*Constructor vacio de la clase*/
    public Puzzle_view() {
    }

    public SoundPool getSoundPool() {

        // Crear SoundPool

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){

            AudioAttributes audioAttributes=new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool=new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }else {
            soundPool=new SoundPool(2, AudioManager.STREAM_MUSIC,0);
        }
        return soundPool;
    }

    /*public int getPieceMovementSound() {
        pieceMovementSound=soundPool.load(this,R.raw.uirefreshfeed,1);
        return pieceMovementSound;
    }*/

    /**
     * Método que convierte los ficheros guardados en los directorios de la memoria interna del
     * móvil, los transforma en bitmaps y luego los transfiere a un array de bitmaps.
     */


    public ArrayList<Bitmap> convertFilesToBitmaps() throws FileNotFoundException {

        ContextWrapper contextWrapper=new ContextWrapper(getApplicationContext());
        File directorioInterno= contextWrapper.getDir("dirImagenesLevel"+niveljuego, Context.MODE_PRIVATE);
        File[] misarchivos=directorioInterno.listFiles();
        piezas=new ArrayList<>();
        for(int i=0;i<misarchivos.length;i++) {
            //File directorio = context.getFilesDir();
            File archivo = new File(String.valueOf(misarchivos[i]));
            Bitmap bmp=BitmapFactory.decodeFile(String.valueOf(archivo));

            piezas.add(bmp);
        }
        return piezas;
    } //convertFilesToBitmaps



    /**
     * Método que carga el Gridview tipo movegridview, le asigna el número de piezas y crea el array
     * de strings representativo de cada pieza
     */

    private void iniciar(){

        filas=3;
        columnas=3;


        myGridview=(moveGridView) findViewById(R.id.grid_puzzle);
        switch (niveljuego){

            case 0:
            {
                break;
            }
            case 1:
            {
                filas=filas+1;
                break;
            }
            case 2:
            {
                filas=filas+1;
                columnas=columnas+1;
            }
        }

        myGridview.setNumColumns(columnas);
        dimensiones=filas*columnas;

        // Crear lista de piezas en la que cada una tendrá el valor de su indice

        listaPiezas=new String[dimensiones];
        for(int i=0;i<dimensiones;i++){

            listaPiezas[i]=String.valueOf(i);
        }
    } // End iniciar


    /**
     * Método para revolver el contenido del Array listaPiezas de forma que el contenido
     * no coincida con el índice de la posición de la pieza. Esto aplicado al puzzle servirá
     * para revolver las piezas
     */

    private void revolverPiezas()
    {
        int indice;
        String temporal;
        Random random=new Random();
        for (int i=listaPiezas.length-1;i>0;i--){

            indice=random.nextInt(i+1);
            temporal=listaPiezas[indice];
            listaPiezas[indice]=listaPiezas[i];
            listaPiezas[i]=temporal;
        }

    }

    /**
     * Método para establecer las dimensiones de cada item en el GridView, que será en definitiva la
     * medida de la pieza
     */

    private void setDimensiones(){

        ViewTreeObserver viewTreeObserver=myGridview.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myGridview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int anchura_ini=myGridview.getMeasuredWidth();
                int altura_ini=myGridview.getMeasuredHeight();
                int altura_statusbar=getStatusBarHeight(getApplicationContext());
                int altura_requerida=altura_ini-altura_statusbar;
                anchuraColumna=anchura_ini/columnas;
                alturaColumna=altura_requerida/filas;
                    display(getApplicationContext());
            }
        });
    } // End setDimensiones


    /**
     * Método para mostrar el contenido de los bitmaps (segmentos de imagen ) en botones que
     * representaran las piezas del puzzle
     */



    public static void display(Context context) {
        ArrayList<ImageButton> buttons=new ArrayList<>();
        ImageButton button;
                for (int i = 0; i < listaPiezas.length; i++) {
                    button = new ImageButton(context);
                    for (int j=0;j<listaPiezas.length;j++){
                        if(listaPiezas[i].equals(String.valueOf(j))){
                            button.setImageBitmap(piezas.get(j));
                        }
                    }
                    buttons.add(button);
                }
        myGridview.setAdapter(new CustomPuzzleAdapter(buttons, anchuraColumna, alturaColumna));
        }



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
     * Método para intercambiar la posición de las piezas del puzzle
     *
     */

    public void intercambiaPieza(final Context context, int posicion, int arrastre){
        String nuevaPosicion=listaPiezas[posicion+arrastre];
        listaPiezas[posicion+arrastre]=listaPiezas[posicion];
        listaPiezas[posicion]=nuevaPosicion;
        //soundPool.play(pieceMovementSound,1,1,0,0,1);
        display(context);
        if (isResuelto()) {
            Toast.makeText(context, "YOU WIN", Toast.LENGTH_SHORT).show();
            //Abrimos la base de datos
                if(mijugador!=null) {
                    conexion = new ConexionSQLite(context, "bd_jugadores", null, 1);
                    // ver valor mayor de puntuación antes de insertar nuevo registro
                    gestionaSiguientePaso();
                    Toast.makeText(context, "Se registraron los datos de la jugada", Toast.LENGTH_SHORT).show();

                }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Haz click en el boton de regresar para seleccionar otro puzzle u otro nivel", Toast.LENGTH_LONG).show();
                }
            },3000);
        } //end if
    }//end intercambiapieza


    /**
     * Método que establecera hacia donde mover (intercambiar la posición de las piezas), en
     * función de su posición actual
     *
     */

    public void moverPiezas(Context context, String direccion, int posicion){

        setColumnsForLevel();

        // Pieza situada en la esquina superior izquierda de la matriz de piezas

        if(posicion==0){
            //solo podrá moverse hacia abajo y hacia la derecha
            if (direccion.equals(right)) intercambiaPieza(context,posicion,1);
            else if (direccion.equals(down))intercambiaPieza(context,posicion,columnas);
            else Toast.makeText(context,"Movimiento NO VALIDO",Toast.LENGTH_SHORT).show();
        }

        // Pieza situada en la fila superior y las columnas intermedias de la matriz de piezas

        else if(posicion >0 && posicion<columnas-1) {
            if (direccion.equals(left)) intercambiaPieza(context, posicion, -1);
            else if (direccion.equals(down)) intercambiaPieza(context, posicion, columnas);
            else if (direccion.equals(right)) intercambiaPieza(context, posicion, 1);
            else Toast.makeText(context, "Movimiento NO VALIDO", Toast.LENGTH_SHORT).show();
        }

            // Pieza situada en la esquina superior derecha de la matriz de piezas

        else if(posicion==columnas-1){

            if (direccion.equals(left))intercambiaPieza(context,posicion,-1);
            else if (direccion.equals(down))intercambiaPieza(context,posicion,columnas);
            else Toast.makeText(context,"Movimiento NO VALIDO",Toast.LENGTH_SHORT).show();
        }

        // Pieza situadas en la columna izquierda de la matriz y en las filas intermedias

        else if((posicion > (columnas - 1)) && (posicion<dimensiones-columnas) && (posicion%columnas==0)){
            if (direccion.equals(right))intercambiaPieza(context,posicion,1);
            else if (direccion.equals(down))intercambiaPieza(context,posicion,columnas);
            else if (direccion.equals(up))intercambiaPieza(context,posicion,-columnas);
            else Toast.makeText(context,"Movimiento NO VALIDO",Toast.LENGTH_SHORT).show();
        }

        // Pieza situadas en columna derecha y filas intermedias de la matriz de piezas

        else if (posicion>=(2*columnas-1) && posicion<(dimensiones-columnas) &&((posicion+1)%columnas==0)){
            if (direccion.equals(left)) intercambiaPieza(context, posicion, -1);
            else if (direccion.equals(down))intercambiaPieza(context,posicion,columnas);
            else if (direccion.equals(up))intercambiaPieza(context,posicion,-columnas);
            else Toast.makeText(context,"Movimiento NO VALIDO",Toast.LENGTH_SHORT).show();
        }

        // Pieza situada en la esquina inferior izquierda de la matriz de piezas

        else if (posicion==dimensiones-columnas){
            if (direccion.equals(right))intercambiaPieza(context,posicion,1);
            else if (direccion.equals(up))intercambiaPieza(context,posicion,-columnas);
            else Toast.makeText(context,"Movimiento NO VALIDO",Toast.LENGTH_SHORT).show();
        }

        // Pieza situada en la fila inferior y las columnas intermedias de la matriz de piezas

        else if(posicion >dimensiones-columnas && posicion<dimensiones-1) {
            if (direccion.equals(left)) intercambiaPieza(context, posicion, -1);
            else if (direccion.equals(up)) intercambiaPieza(context, posicion, -columnas);
            else if (direccion.equals(right)) intercambiaPieza(context, posicion, 1);
            else Toast.makeText(context, "Movimiento NO VALIDO", Toast.LENGTH_SHORT).show();
        }

        // Pieza situada en la esquina inferior derecha de la matriz de piezas

        else if(posicion==dimensiones-1){

            if (direccion.equals(left))intercambiaPieza(context,posicion,-1);
            else if (direccion.equals(up))intercambiaPieza(context,posicion,-columnas);
            else Toast.makeText(context,"Movimiento NO VALIDO",Toast.LENGTH_SHORT).show();
        }

        // Pieza situadas en columnas y filas intermedias de la matriz de piezas
        else
        {
            if (direccion.equals(left)) intercambiaPieza(context, posicion, -1);
            else if (direccion.equals(right)) intercambiaPieza(context, posicion, 1);
            else if (direccion.equals(down)) intercambiaPieza(context, posicion, columnas);
            else intercambiaPieza(context, posicion, -columnas);
        }
    } // End moverPiezas


    /**
     * Método para determinar si el puzzle quedó resuelto
     *
     */

    public static boolean isResuelto(){

        boolean resuelto=false;
        for (int i=0;i<listaPiezas.length;i++){
            if (listaPiezas[i].equals(String.valueOf(i))){
                resuelto=true;
            }else{
                resuelto=false;
                break;
            }
        } //End for
        return resuelto;
    } // End isResuelto


    /**
     * Método toma el instante en el que el juego se resuelve y llama a registrar los datos de la
     * jugada
     */

    public void gestionaSiguientePaso() {
        tfin = System.currentTimeMillis();
        registraDatosJugada();
    }


    /**
     * Método para registrar datos en la tabla jugada
     */

    public void registraDatosJugada(){
        int descuento;
        descuento = setPointsPerSecond(); // establece el número ptos a descontar por segundo
        mifecha=Calendar.getInstance().getTime();
        String fecha=sdf.format(mifecha);
        long duracion=(tfin-tini)/1000; // conversion a segundos
        long puntos=1000-(descuento*duracion); //formula con la que se obtienen los ptos
        SQLiteDatabase db=conexion.getWritableDatabase();

        // insert into jugada (idgamer,fecha,duracion,puntos) values ();

        String insert="INSERT INTO "+ Utilidades.TABLA_JUGADA+" ("
                +Utilidades.CAMPO_IDGAMER+","+Utilidades.CAMPO_FECHA+","
                +Utilidades.CAMPO_DURACION+","+Utilidades.CAMPO_PUNTOS+") VALUES ("
                +String.valueOf(mijugador.getIdJugador())+",'"+fecha+"',"
                +String.valueOf(duracion)+","+String.valueOf(puntos)+")";
        db.execSQL(insert);
        db.close();

    }


    /**
     * Método que asigna las filas y columnas del puzzle en función del número de piezas del mismo,
     * y por tanto del nivel de juego
     */

    public void setColumnsForLevel(){

        switch (listaPiezas.length){
            case 9:
            {
                columnas=3;
                filas=3;
                break;
            }
            case 12:
            {
                columnas=3;
                filas=4;
                break;
            }
            case 16:
            {
                columnas=4;
                filas=4;
                break;
            }
        }
        dimensiones=columnas*filas;
    }

    /**
     * Método que establece el número de puntos a restar por cada segundo perdido en la resolución del
     * puzzle en función del nivel del juego
     */


    public int setPointsPerSecond(){

        int puntosArestar=0;

        switch (listaPiezas.length){
            case 9:
            {
                puntosArestar=15;
                break;
            }
            case 12:
            {
                puntosArestar=10;
                break;
            }
            case 16:
            {
                puntosArestar=5;
                break;
            }
        }
        return puntosArestar;
    } // End setPointsPerSecond

/*
* Metodo que me devuelva el máximo valor de la lista de jugadas
* */

public int getNewRecord() {
    conexion = new ConexionSQLite(getApplicationContext(), "bd_jugadores", null, 1);
    ArrayList<Jugada> misjugadas = conexion.getListaJugadas();
    if (misjugadas.size()!=0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            misjugadas.sort(Comparator.comparing(Jugada::getPuntos).reversed());
        }
        return misjugadas.get(0).getPuntos();
    } else
        return 0;
}
}//End Puzzle_view class