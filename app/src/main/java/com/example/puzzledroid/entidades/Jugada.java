/*
 * Clase POJO asociado a objeto jugada
 * */


package com.example.puzzledroid.entidades;

import java.io.Serializable;
import java.util.Date;

public class Jugada implements Serializable {


    /*
    * Atributos de la clase
    * */

    private int idJugada;
    private int idGamer;
    private Date fecha;
    private int duracion;
    private int puntos;

    /*
    * Cosntructores de la clase
    * */

    public Jugada(int idJugada, int idGamer, Date fecha, int duracion, int puntos) {
        this.idJugada = idJugada;
        this.idGamer = idGamer;
        this.fecha = fecha;
        this.duracion = duracion;
        this.puntos = puntos;
    }

    public Jugada() {
    }

    /*
    * Getters y setters
    * */

    public int getIdJugada() {
        return idJugada;
    }

    public void setIdJugada(Integer idJugada) {
        this.idJugada = idJugada;
    }

    public int getIdGamer() {
        return idGamer;
    }

    public void setIdGamer(Integer idGamer) {
        this.idGamer = idGamer;
    }


    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }


}
