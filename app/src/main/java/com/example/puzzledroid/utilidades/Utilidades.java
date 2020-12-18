/*
 * Clase para crear las constantes que representen las tablas y los campos de nuestra base de datos
 * */

package com.example.puzzledroid.utilidades;


public class Utilidades {

    /*
    * Cosntantes asociados a los campos de la tabla juggdor
    * */

    public static final String TABLA_JUGADOR="jugador";
    public static final String CAMPO_IDJUGADOR="idjugador";
    public static final String CAMPO_NICKNAME="nickname";
    public static final String CAMPO_PASSWORD="password";


    public static final String CREAR_TABLA_JUGADOR="CREATE TABLE "+TABLA_JUGADOR+
            " ("+CAMPO_IDJUGADOR+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CAMPO_NICKNAME+" TEXT, "+CAMPO_PASSWORD+" TEXT)";


    /*
     * Constantes asociados a los campos de la tabla jugada
     * */

    public static final String TABLA_JUGADA="jugada";
    public static final String CAMPO_IDJUGADA="idjugada";
    public static final String CAMPO_IDGAMER="idgamer";
    public static final String CAMPO_FECHA="fecha";
    public static final String CAMPO_DURACION="duracion";
    public static final String CAMPO_PUNTOS="puntos";

    public static final String CREAR_TABLA_JUGADA="CREATE TABLE "+TABLA_JUGADA+
            " ("+CAMPO_IDJUGADA+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +CAMPO_IDGAMER+" INTEGER, "+CAMPO_FECHA+" TEXT, "
            +CAMPO_DURACION+" INTEGER, "+CAMPO_PUNTOS+" INTEGER, "
            +"FOREIGN KEY ("+CAMPO_IDGAMER+") REFERENCES "+TABLA_JUGADOR+"("+CAMPO_IDJUGADOR+") )";


}
