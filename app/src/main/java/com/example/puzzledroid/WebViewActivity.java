/*
 * Clase para cargar en el webview una html como un fichero local
 * */

package com.example.puzzledroid;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class WebViewActivity extends AppCompatActivity {

    //se deja el elemento Webview como público

    WebView webView;
    private String fileName="pagina_ayuda.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView=(WebView) findViewById(R.id.wbview);

        // mostrar contenido en Webview del html alamacenado en carpeta assets
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/"+fileName);

    }

    public void Cerrar(View view){
        finish();
    }
}