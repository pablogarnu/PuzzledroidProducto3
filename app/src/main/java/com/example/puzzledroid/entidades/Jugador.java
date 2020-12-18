/*
 * Clase POJO asociado a objeto jugador
 * */

package com.example.puzzledroid.entidades;

import java.io.Serializable;

public class Jugador implements Serializable {

    /*
    * Atributos de la clase
    * */

    private int idJugador;
    private String nickname;
    private String password;

    /*
    * Constructores de la clase
    * */

    public Jugador(int idJugador, String nickname, String password) {
        this.idJugador = idJugador;
        this.nickname = nickname;
        this.password = password;
    }

    public Jugador() {

    }


    /*
     * Setters y getters
     * */

    public int getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(Integer idJugador) {
        this.idJugador = idJugador;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
