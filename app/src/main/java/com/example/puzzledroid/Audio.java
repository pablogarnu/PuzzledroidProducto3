package com.example.puzzledroid;

import java.io.Serializable;


/*
 * Creamos esta clase para usar sus instancias como objetos de audio
 * */
public class Audio implements Serializable {

    //Atributos privados de la clase correspondientes a información que un archivo de audio precisa

    private String data;
    private String title;
    private String album;
    private String artist;

    /*
     * Cosntructor de la clase
     * */

    public Audio(String data, String title, String album, String artist) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    /*
     * getters de la clase
     * */

    public String getData() {
        return data;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    /*
     * Setters de la clase
     * */

    public void setData(String data) {
        this.data = data;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

} //End class com.example.puzzledroid.Audio