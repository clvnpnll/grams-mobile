package com.example.antematix.grams;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {


    Button loginBtn;
    EditText etUsername, etPassword;
    final String LOG = "MainActivity";
    String fullname, email;
    private MessageDigest md;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("GRAMS");
        loginBtn = (Button) findViewById(R.id.loginBtn);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                String password_md5 = "";
                try {
                    md = MessageDigest.getInstance("MD5");
                    md.update(password.getBytes());

                    byte byteData[] = md.digest();

                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < byteData.length; i++) {
                        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
                    }
                    password_md5 = sb.toString();

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                if (isNetworkAvailable()) {
                    if(!username.equals("") && !password.equals("")){
                        final HashMap postData = new HashMap();
                        postData.put("username", username);
                        postData.put("password", password_md5);
                        final String finalPassword_md = password_md5;
                        PostResponseAsyncTask task1 = new PostResponseAsyncTask(MainActivity.this, postData, new AsyncResponse() {
                            @Override
                            public void processFinish(String s) {
                                // Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                                if (s.contains("success")) {
                                    Toast.makeText(MainActivity.this, "Login Succesfully", Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(MainActivity.this, FingerprintActivity.class);
                                    i.putExtra("username",username);
                                    i.putExtra("password_md5", finalPassword_md);
                                    startActivity(i);
                                } else if (s.contains("invalid user")) {
                                    Toast.makeText(MainActivity.this, "Inactive Account", Toast.LENGTH_LONG).show();
                                    etUsername.setText("");
                                    etPassword.setText("");
                                } else if(s.contains("fail")){
                                    Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MainActivity.this,"Login Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        task1.execute("http://grams.antematix.com/user/login");

                    } else if (username.equals("") && !password.equals("")) {
                        Toast.makeText(MainActivity.this, "Username is required", Toast.LENGTH_LONG).show();
                    } else if (!username.equals("") && password.equals("")) {
                        Toast.makeText(MainActivity.this, "Password is required", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Please complete the fields", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }



    @Override
    public void onBackPressed() {

    }
}

