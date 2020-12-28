/*
 * Clase que herededa de Gridview y que implementa funciones para detectar cuando se toca un boton
 * del gridview y en que condiciones se permitirá su movimiento
 * */

package com.example.puzzledroid;

import android.content.Context;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridView;

import android.annotation.TargetApi;


public class moveGridView extends GridView {

    /*
    * Atributos de la clase
    * */

    private GestureDetector gestureDetector;
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    private boolean flingConfirmed = false;
    private float touchX;
    private float touchY;
    private Puzzle_view mypz;
    private SoundPool soundPool;
    private int sounditem;
    private String msjganar;
    private String msjRegistroJugada;
    private String msjContinuaJugando;

    /*
    * Constructores heredados a implementar
    *
    * */


    public moveGridView(Context context) {
        super(context);
        iniciar(context);
    }


    public moveGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        iniciar(context);
    }

    public moveGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        iniciar(context);
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP)
    public moveGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        iniciar(context);
    }

    public void iniciar(final Context context) {
        mypz=new Puzzle_view();
        soundPool=mypz.getSoundPool();
        sounditem=soundPool.load(context,R.raw.uirefreshfeed,1);
        gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            } //End onDown

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int posicion =
                        moveGridView.this.pointToPosition(Math.round(e1.getX()), Math.round(e1.getY()));
                if(mypz.isResuelto()) return false;
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                    if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH
                            || Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY ) {
                        return false;
                    }
                    if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {

                        mypz.moverPiezas(context, mypz.up, posicion);
                        soundPool.play(sounditem, (float) 0.2,(float)0.2,0,0,1);

                    } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
                        mypz.moverPiezas(context, mypz.down, posicion);
                        soundPool.play(sounditem,(float) 0.2,(float) 0.2,0,0,1);
                    }
                }
                else {

                    if (Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {
                        return false;}
                        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                           mypz.moverPiezas(context, mypz.left, posicion);
                            soundPool.play(sounditem,(float) 0.2,(float) 0.2,0,0,1);}
                        else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                            mypz.moverPiezas(context, mypz.right, posicion);
                            soundPool.play(sounditem,(float) 0.2,(float) 0.2,0,0,1);}
                    }// End else

                    return super.onFling(e1, e2, velocityX, velocityY);
                }//End onFling
            });
    } // End iniciar

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        int action=event.getActionMasked();
        gestureDetector.onTouchEvent(event);
        if(action==MotionEvent.ACTION_CANCEL||action==MotionEvent.ACTION_UP){
            flingConfirmed=false;
        }else if (action==MotionEvent.ACTION_DOWN){
            touchX=event.getX();
            touchY=event.getY();
        }else{
            if(flingConfirmed){
                return true;}
            float deltaX=(Math.abs(event.getX()-touchX));
            float deltaY=(Math.abs(event.getY()-touchY));
            if (deltaX>SWIPE_MIN_DISTANCE || deltaY>SWIPE_MIN_DISTANCE){
                flingConfirmed=true;
                return true;
            }
        } //End else
        return super.onInterceptTouchEvent(event);
    }// End onInterceptTouchEvent

    @Override
    public boolean onTouchEvent(MotionEvent event){
        return gestureDetector.onTouchEvent(event);
    }

    public String getMsjganar() {
        msjganar=getResources().getString(R.string.msjganaste);
        return msjganar;
    }

    public String getMsjRegistroJugada() {
        msjRegistroJugada=getResources().getString(R.string.msjregistrojugada);
        return msjRegistroJugada;
    }

    public String getMsjContinuaJugando() {
        msjContinuaJugando=getResources().getString(R.string.msjregresarotrajugada);
        return msjContinuaJugando;
    }
} // End class moveGridView
