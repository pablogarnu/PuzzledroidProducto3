
/**
 * Clase con la lógica para logear a los jugadores en modo multijugador
 */


package com.example.puzzledroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.puzzledroid.entidades.Jugador;
import com.example.puzzledroid.utilidades.Utilidades;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class loginActivity extends AppCompatActivity implements View.OnClickListener {

    /*
    * Constantes de la clase
    * */
    private static final int RC_SIGN_IN =9001;
    private static final String TAG = "GoogleActivity";

    /*
    * Atributos de la clase
    * */

    private EditText mylogin;
    private Button btnLogin;
    private Jugador mijugador;
    private String idioma;
    //private static String loginPassword="admin";

    ConexionSQLite conexion;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth myAuth;
    private FirebaseUser currentuser;
    private SignInButton googleButton;
    private Button signoutButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        idioma=intent.getExtras().getString("idioma");

        setContentView(R.layout.activity_login);

        conexion=new ConexionSQLite(getApplicationContext(),"bd_jugadores",null,1);

        mylogin = (EditText) findViewById(R.id.edtLogin);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        checkLogin();
        googleButton=(SignInButton)findViewById(R.id.sign_in_button);
        signoutButton=(Button)findViewById(R.id.singoutbutton);
        googleButton.setOnClickListener(this);
        signoutButton.setOnClickListener(this);


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Inicializa FirebaseAuth

        myAuth=FirebaseAuth.getInstance();
        currentuser=myAuth.getCurrentUser();


    } // End onCreate


   @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        currentuser=myAuth.getCurrentUser();
        updateUI(currentuser);
    }

    private void updateUI(FirebaseUser currentuser) {

        if (currentuser!=null){
            googleButton.setVisibility(View.GONE);
            signoutButton.setVisibility(View.VISIBLE);
            String username=currentuser.getDisplayName();
            String usermail=currentuser.getEmail();
            String mensaje=getString(R.string.autenticacionOK)+username+"  email: "+usermail;
            Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_SHORT).show();
            goToPuzzleList();
        }else{
            googleButton.setVisibility(View.VISIBLE);
            signoutButton.setVisibility(View.GONE);
            String mensaje=getString(R.string.autenticacionNOok);
            Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_SHORT).show();
        }
    }

    private void goToPuzzleList() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(getApplicationContext(),PuzzleList.class);
                startActivity(intent);            }
        }, 5000);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.singoutbutton:
                signOut();
                break;
            // ...
        }
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {

        //Firebase sign out
        FirebaseAuth.getInstance().signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                updateUI(null);
            }
        }
    }



    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        myAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = myAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msjfalloautenticacion), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }


    /**
     * Método que se ejecuta al clicar en botón de login, comprobando si está registrado
     */

    public void checkLogin(){

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mylogin.getText().toString().trim().length()==0){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.msjcampologinvacio), Toast.LENGTH_SHORT).show();
                }else{

                    if (loginRegistrado()) {
                        mijugador=getMijugador(mylogin.getText().toString());
                        Intent intent = new Intent(loginActivity.this, PuzzleList.class);
                        intent.putExtra("idioma",idioma);
                        intent.putExtra("jugador_activo",mijugador); // mandamos como parámetro el jugador que se ha logeado correctamente
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msjusuarionoregistrado), Toast.LENGTH_SHORT).show();
                        mylogin.setText("");
                    }
                }
            }
        });
    } //End checklogin


    /**
     * Método que se ejecuta al clicar en botón de registro, activando la pantalla de registro.
     */


    public void activateRegistro(View view){

        Intent intent=new Intent(loginActivity.this,RegisterActivity.class);
        intent.putExtra("idioma",idioma);
        startActivity(intent);
    }


    /**
     * Método para consultar si encuentra el login en la tabla de jugadores.
     */

    public boolean loginRegistrado() {

        SQLiteDatabase db = conexion.getReadableDatabase();
        String[] parametros = {mylogin.getText().toString()};
        String[] campos = {Utilidades.CAMPO_IDJUGADOR,Utilidades.CAMPO_NICKNAME,Utilidades.CAMPO_PASSWORD};

        Cursor cursor = db.query(Utilidades.TABLA_JUGADOR, campos, Utilidades.CAMPO_PASSWORD + "=?", parametros, null, null, null);
        if (cursor != null && cursor.moveToFirst() && cursor.getCount()>0){
            cursor.close();
            db.close();
            return true;}
        db.close();
        return false;
    } //End loginRegistrado


    /**
     * Método que devuelve el objeto jugador asociado a un determinado login.
     */


    public Jugador getMijugador(String mipasword){
        SQLiteDatabase db = conexion.getReadableDatabase();
        String[] parametros = {mipasword};
        String[] campos = {Utilidades.CAMPO_IDJUGADOR,Utilidades.CAMPO_NICKNAME,Utilidades.CAMPO_PASSWORD};
        try {
            Cursor cursor = db.query(Utilidades.TABLA_JUGADOR, campos, Utilidades.CAMPO_PASSWORD + "=?", parametros, null, null, null);
            cursor.moveToFirst();
            mijugador = new Jugador(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            cursor.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.msjnoexistejugador),Toast.LENGTH_SHORT).show();
        }
        db.close();
        return mijugador;
    }


}//End Class loginActivity