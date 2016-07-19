package com.example.restdeliverycollaboratorapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class MainActivity extends AppCompatActivity {
    EditText edtEmail;
    EditText edtPassword;
    Button btnLogin;
    Context ctx;

    Meteor mMeteor;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    String HELPERS="helpers";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        mMeteor = MeteorSingleton.getInstance();
        ctx = this;

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        prefs = getSharedPreferences(HELPERS, Context.MODE_PRIVATE);
        editor = prefs.edit();
        Log.i("logeado",""+prefs.getBoolean("logged",false));
        if(prefs.getBoolean("logged",false)){
            startActivity(new Intent(this,MapActivity.class));
            finish();
        }
    }

    public void login(){

        final ProgressDialog dialog = new ProgressDialog(this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String email = edtEmail.getText().toString(),
                        password = edtPassword.getText().toString();
                Log.i("params",email + " " + password);
                mMeteor.loginWithEmail(email,password,new ResultListener(){


                    @Override
                    public void onError(String error, String reason, String details) {
                        if(error.equals(reason)) Toast.makeText(ctx,"Usuario o contraseña incorrecta", Toast.LENGTH_LONG).show();

//                        Log.i("Error-Login",error);
//                        Log.i("Reason-Login",reason);
//                        Log.i("Details-login",details);
                    }

                    @Override
                    public void onSuccess(String result) {
                        Log.i("Result",result);
                        dialog.hide();
                        editor.putBoolean("logged",true);
                        editor.putString("userId",mMeteor.getUserId());
                        editor.commit();
                        startActivity(new Intent(MainActivity.this,MapActivity.class));
                        finish();
                    }
                });

            }
        });


/*

        final JSONObject data = new JSONObject();
        try{
            data.put("email",email);
            data.put("password",password);
        }catch (JSONException e){

        }

        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                int code = 0;
                try{
                    code = okHttpRequest.post("",data.toString());
                }catch (IOException e){
                }

                return code;
            }

            @Override
            protected void onPostExecute(Integer code) {
                super.onPostExecute(code);
                if(code==200) Toast.makeText(LoginActivity.this,"Llevando a la actividad principal",Toast.LENGTH_LONG).show();
            }
        }.execute(null,null,null);

*/


    }
}
