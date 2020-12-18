package com.example.puzzledroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.puzzledroid.App.CHANNEL_MEDIA_ID;



/*
 * Clase con la que creamos un servicio para reproducir música y que implementa varias interfaces para
 * gestionar eventos mientras esta sonando
 * */


public class MediaPlayerService extends Service implements  MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener
{

    //Constantes de la clase

    public static final String ACTION_PLAY = "com.example.puzzledroid.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.puzzledroid.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.puzzledroid.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.puzzledroid.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.puzzledroid.ACTION_STOP";



    // Atributos globales de la clase

    private MediaPlayer mediaPlayer;
    private int resumePosition; // usado para pausar/renudar el MediaPlayer
    private AudioManager audioManager;
    private boolean ongoingCall=false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private NotificationManagerCompat notificationManager;


    //Lista de archivos de audio disponibles
    private ArrayList<Audio> audioList;
    private int audioIndex=-1;
    private Audio activeAudio; // objeto que este en esos momentos reproduciendo audio


    //Binder que se da a los clientes de audio
    private final IBinder iBinder=new LocalBinder();

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;


    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager=NotificationManagerCompat.from(this);
        //gestionar llamadas entrantes
        callStateListener();
        //escuchar nuevos audios a reproducir
        register_playNewAudio();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(playNewAudio);

        //clear cached playlist
        new StorageUtil(getApplicationContext()).clearCacheAudioPlayList();
    }

    //Método para inicializar mediaPlayer

    private void initMediaPlayer(){

        mediaPlayer=new MediaPlayer();

        //Preparar los listeners de eventos MediaPlayer

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        // Resetear para que el mediaplayer no apunte a otra fuente de datos

        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try{
            mediaPlayer.setDataSource(activeAudio.getData());

        }catch (IOException e){
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    } // End initMediaPlayer



    // Metodo para reproducir Play

    private void playMedia(){

        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }// End playMedia

    // Método para parar la reproducción Stop

    private void stopMedia(){
        if (mediaPlayer==null) return;
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    // Método para pausar

    private void pauseMedia(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            resumePosition=mediaPlayer.getCurrentPosition();
        }
    }

    // Método para reanudar

    private void resumeMedia(){

        if(!mediaPlayer.isPlaying()){

            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    /*
     * Método que gestiona la interacción con otras aplicaciones de audio
     * */
    @Override
    public void onAudioFocusChange(int focusChange) {
        //Se invoca cuando cambia el foco del audio del sistema
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:
                //reanuda reproducción
                if(mediaPlayer==null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f,1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Pierde el foco por una cantidad de tiempo indeterminada: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Pierde el foco por un corto tiempo pero paramos la reproducción. no liberamos
                // elmedia player porque la reproducción es probable que se reanude
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Pierde el foco por un corto tiempo pero es ok que siga reproduciendo a un
                // nivel atenuado
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }//end switch
    }//End onAudioFocusChange

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        // Se invoca cuando la reproducción finaliza
        stopMedia();
        // Parar servicio
        stopSelf();
    } // End onCompletion


    //Metodo para gestionar los errores
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        // Se invoca cuando hubo un error durante una operación asincrona
        switch (what){
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }//End onError


    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        // Invocado cuando el audio está listo para su reproducción
        playMedia();
    } //End onPrepared

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    //

    public class LocalBinder extends Binder{

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


    private boolean requestAudioFocus(){

        audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int result=audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC
                ,AudioManager.AUDIOFOCUS_GAIN);
        if (result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            //se gana el foco
            return true;
        }
        // No se pudo ganar el foco
        return false;
    }

    private boolean removeAudioFocus(){
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED==audioManager.abandonAudioFocus(this);
    }


    /*
     * Método que gestiona la inicialización de MediaSession, MediaPlayer, carga el audilist y
     * construye la notificación
     * */


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)  {
        try{
            //Cargar datos de SharedPrefernces
            StorageUtil storage=new StorageUtil(getApplicationContext());
            audioList=storage.loadAudio();
            audioIndex=storage.loadAudiIndex();

            if (audioIndex!=-1 && audioIndex < audioList.size()){
                //El índice está en rango válido
                activeAudio=audioList.get(audioIndex);
            }else{
                stopSelf();
            }
        }catch (NullPointerException e){
            stopSelf();
        }

        //Solicita foco para audio

        if(requestAudioFocus()==false){
            //No gana el foco
            stopSelf();
        }

        if(mediaSessionManager==null){
            try {
                initMediaSession();
                initMediaPlayer();
            }catch(RemoteException e){
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }
        //Gestiona accion del Intent desde MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    } //End onStartCommand



    /*
     * Método para gestionar llamadas entrantes, evitando la reproducción de música
     * */

    private void callStateListener(){
        // crear gestor de telefono
        telephonyManager=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //Comenzar la escucha de cambio de estado en llamadas
        phoneStateListener=new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch(state){
                    //si al menos existe una llamada o el telefono está sonando
                    //pausa el MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if(mediaPlayer!=null){
                            pauseMedia();
                            ongoingCall=true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        //teléfono inactivo. Iniciar la reproducción
                        if(mediaPlayer!=null){
                            if(ongoingCall){
                                ongoingCall=false;
                                resumeMedia();
                            }
                        }
                        break;
                }//End switch
            }//End onCallStateChanged
        };

        // Registrar el listener con el gestor de teléfono
        // Escucha los cambios en el estado de llamada del teléfono
        telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
    }//End callStateListener

    /*
     * Métodos para gestionar la reproducción de un nuevo audio cuando está sonando ya otro haciendo
     * que el servicio escuche las llamadas a reproducir nuevos audios mediante un BroadcastReceiver
     * */


    private BroadcastReceiver playNewAudio=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //obtener el nuevo indice de SharedPreferences

            audioIndex=new StorageUtil(getApplicationContext()).loadAudiIndex();
            if(audioIndex!=-1 && audioIndex<audioList.size()){
                //el indice se encuentra en un rango permitido
                activeAudio=audioList.get(audioIndex);
            }else{
                stopSelf();
            }

            // Se recibe una acción PLAY_NEW_AUDIO
            // se resetea mediaPlayer para reproducir el nuevo audio

            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    }; //End playNewAudio



    private void register_playNewAudio(){
        // Registra playNewMedia receiver

        IntentFilter filter=new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio,filter);
    }

    /*
     * Método par manejar la inicialización de MediaSession
     * */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() throws RemoteException{

        if (mediaSessionManager!=null) return; // Cuando el mediaSessionManager existe
        //Crear nueva MediaSession
        mediaSessionManager= (MediaSessionManager) getSystemService((Context.MEDIA_SESSION_SERVICE));

        mediaSession=new MediaSessionCompat(getApplicationContext(),"AudioPlayer");
        //Obtener MediaSessions control de transporte
        transportControls=mediaSession.getController().getTransportControls();
        //MediaSession preparar para recibir comandos
        mediaSession.setActive(true);
        //indicar que MediaSession maneja los comandos de Transport control mediante MediaSessionCompat.Callback
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Poner los metadatos
        updateMetaData();

        //Adjuntar Callback para recibir actualizaciones de MediaSession

        mediaSession.setCallback(new MediaSessionCompat.Callback() {

            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);

            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevios();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Parar servicio
                stopSelf();
            }


            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }

        });

    }//End initMediaSession



    private void skipToPrevios() {

        if(audioIndex==0){
            //es el primero en la lista y asignamos el último
            audioIndex=audioList.size()-1;
            activeAudio=audioList.get(audioIndex);
        }else{
            //obten el anterior en la lista
            activeAudio=audioList.get(--audioIndex);
        }
        //actualiza el indice almacenado
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToNext() {
        if(audioIndex==audioList.size()-1) {
            //ultimo en la lista
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        }else{
            //obten el siguiente
            activeAudio=audioList.get(++audioIndex);
        }

        //actualiza el indice almacenado
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    /*
     * Método que construye la interfaz de la notificación prepara todos los eventos que se disparan
     * cuando el usuario clica en un boton de notificación
     * */

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction= android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction=null;
        //Construir una nueva notificación de acuerdo al estado actual del mediaPlayer

        if(playbackStatus==PlaybackStatus.PLAYING){
            notificationAction=android.R.drawable.ic_media_pause;
            //crear la accion de pausa
            play_pauseAction=playbackAction(1);
        }else if(playbackStatus==PlaybackStatus.PAUSED){

            notificationAction= android.R.drawable.ic_media_play;
            //crear la accion play
            play_pauseAction=playbackAction(0);
        }

        //Reemplazar con nuestra imagen
        Bitmap largeIcon=BitmapFactory.decodeResource(getResources(),R.drawable.microphone);
        //Crear notificación


        Notification notification=new NotificationCompat.Builder(getApplicationContext(), CHANNEL_MEDIA_ID)

                .setColor(getResources().getColor(R.color.colorPrimary))
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getAlbum())
                .setContentInfo(activeAudio.getTitle())
                .setShowWhen(false)
                //Añadir acciones playback
                .addAction(android.R.drawable.ic_media_previous,"previous",playbackAction(3))
                .addAction(notificationAction,"pause",play_pauseAction)
                .addAction(android.R.drawable.ic_media_next,"next",playbackAction(2))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0,1,2))
                .build();

        notificationManager.notify(1,notification);

    } //End buildNotification


    /*genera las acciones que playback*/
    private PendingIntent playbackAction(int actionNumber) {

        Intent playbackAction=new Intent(this,MediaPlayerService.class);
        switch (actionNumber){
            case 0:
                //Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this,actionNumber,playbackAction,0);
            case 1:
                //Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this,actionNumber,playbackAction,0);
            case 2:
                //Pista siguiente
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this,actionNumber,playbackAction,0);
            case 3:
                //Pista previa
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this,actionNumber,playbackAction,0);
            default:
                break;
        }
        return null;
    }//End playbackAction


    /*
     * Método para getionar las acciones que el servicio genera cuando el usuario clica en los botones
     * de notificación
     * */

    private void handleIncomingActions(Intent playbackAction){
        if (playbackAction==null || playbackAction.getAction()==null) return;
        String actionString =playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }//End handleIncomingActions



    private void removeNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }





    /*
     * Método par manejar el paso de Metados a una session activa
     * */

    private void updateMetaData(){
        Bitmap album= BitmapFactory.decodeResource(getResources(),R.drawable.microphone);
        //Actualizar metadatos

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,activeAudio.getTitle())
                .build());
    }//End updateMetaData





}//End MediaPlayerService
