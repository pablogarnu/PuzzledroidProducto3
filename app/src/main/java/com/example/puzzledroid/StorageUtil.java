package com.example.puzzledroid;

/*classe para gestionar el alamcenamiento de datos de audio*/

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StorageUtil {

    /*Atributos de la clase*/

    private final String STORAGE="com.example.puzzledroid.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    /*Constructor de la clase*/

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<Audio> arrayList){
        preferences=context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        Gson gson=new Gson();
        String json=gson.toJson(arrayList);
        editor.putString("audioArrayList",json);
        editor.apply();
    }//End storeAudio

    public ArrayList<Audio> loadAudio(){
        preferences=context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        Gson gson=new Gson();
        String json=preferences.getString("audioArrayList",null);
        Type type=new TypeToken<ArrayList<Audio>>(){}.getType();
        return gson.fromJson(json,type);
    }// End loadAudio()

    public void storeAudioIndex(int index){
        preferences=context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt("audioIndex",index);
        editor.apply();
    }//End storeAudioIndex

    public int loadAudiIndex(){
        preferences=context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex",-1); // devuelve -1 si no encuentra datos
    }//End loadAudiIndex

    public void clearCacheAudioPlayList(){
        preferences=context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.clear();
        editor.commit();
    }//End clearCacheAudioPlayList


} //End classStorageUtil
