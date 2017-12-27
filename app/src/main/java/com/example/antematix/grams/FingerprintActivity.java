package com.example.antematix.grams;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.multidots.fingerprintauth.*;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class FingerprintActivity extends AppCompatActivity implements FingerPrintAuthCallback {


    private TextView mAuthMsgTv;
    private Bitmap bitmap;
    private FingerPrintAuthHelper mFingerPrintAuthHelper;
    private boolean isFingerprint;
    public static String userId = "0";
    public static final String IMAGE_URL = "http://grams.antematix.com/images/usermobile/";
    public static final String USERDATA_URL = "http://grams.antematix.com/user/getuser/";
    public static final String IMAGE_DIRECTORY_NAME = "Images/User";
    public static final String SEND_TOKEN = "http://grams.antematix.com/user/fcm_insert/";
    int failureCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint);
        isFingerprint = false;
        mAuthMsgTv = (TextView) findViewById(R.id.AuthMsgTv);

        loginSuccess();
        //mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
        //failureCounter = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuthMsgTv.setText("SCAN YOUR FINGER");

        //start finger print authentication
        //mFingerPrintAuthHelper.startAuth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mFingerPrintAuthHelper.stopAuth();
    }

    @Override
    public void onNoFingerPrintHardwareFound() {
        Toast.makeText(FingerprintActivity.this, "Your device does not have finger print scanner.", Toast.LENGTH_SHORT).show();
        isFingerprint = false;
    }

    @Override
    public void onNoFingerPrintRegistered() {
        Toast.makeText(FingerprintActivity.this, "There are no finger prints registered on this device. Please register your finger from settings.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBelowMarshmallow() {
        Toast.makeText(FingerprintActivity.this, "You are running older version of android that does not support finger print authentication.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
            final String username = getIntent().getStringExtra("username");
            final String password = getIntent().getStringExtra("password_md5");
            final HashMap postData = new HashMap();
            postData.put("username", username);
            postData.put("password", password);
            getUserData(postData);
            Toast.makeText(FingerprintActivity.this, "Fingerprint Authentication Succeeded.", Toast.LENGTH_SHORT).show();
            String token = FirebaseInstanceId.getInstance().getToken();
            Toast.makeText(FingerprintActivity.this, token, Toast.LENGTH_SHORT).show();
    }

    public void getUserData(final HashMap postData){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        AsyncTask<Void, Void, String> getUser = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(FingerprintActivity.this, "Please wait", "Loading...", false, false);
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String result = rh.sendPostRequest(USERDATA_URL, postData);
                return result;
            }

            @Override
            protected void onPostExecute(String s){
                try{
                    JSONObject jObj = new JSONObject(s);
                    if(jObj != null) {
                        Log.d("aven", s);
                        JSONObject userobj = jObj.getJSONObject("user");
                        if(userobj != null) {
                            userId = userobj.getString("id");
                            String userImage = userobj.getString("image");
                            editor.putInt("id", userobj.getInt("id"));
                            editor.putString("firstname", userobj.getString("firstname"));
                            editor.putString("lastname", userobj.getString("lastname"));
                            editor.putString("middlename", userobj.getString("middlename"));
                            editor.putString("address", userobj.getString("address"));
                            editor.putString("contact", userobj.getString("contact"));
                            editor.putString("birthdate", userobj.getString("birthdate"));
                            editor.putString("image", userobj.getString("image"));
                            editor.putInt("handler_id", userobj.getInt("handler_id"));
                            editor.commit();
                            getBitmap(IMAGE_URL + userImage);
                        }
                    }
                } catch(Exception ex){
                    Log.d("aven", "ex: "+ex.getMessage());
                    Toast.makeText(FingerprintActivity.this,"userdata not found",Toast.LENGTH_LONG).show();
                }
                loading.dismiss();
                onFingerprintSuccess();
            }
        };
        getUser.execute();
    }

    public void loginSuccess(){
        final String username = getIntent().getStringExtra("username");
        final String password = getIntent().getStringExtra("password_md5");
        final HashMap postData = new HashMap();
        postData.put("username", username);
        postData.put("password", password);
        getUserData(postData);
        Toast.makeText(FingerprintActivity.this, "Fingerprint Authentication Succeeded.", Toast.LENGTH_SHORT).show();
        String token = FirebaseInstanceId.getInstance().getToken();
        Toast.makeText(FingerprintActivity.this, token, Toast.LENGTH_SHORT).show();
    }

    public void getBitmap(final String url) {
        AsyncTask<Void, Void, String> viewImage = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(FingerprintActivity.this, "Please wait", "Loading...", false, false);
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    InputStream is = (InputStream) new URL(url).getContent();
                    Bitmap bmap = BitmapFactory.decodeStream(is);
                    is.close();
                    bitmap = bmap;
                    Log.d("aven", "User Image downloaded");
                } catch (Exception ex) {
                    Log.d("aven", "Error: " + ex.getMessage());
                }
                return "getCheckpoint";
            }

            @Override
            protected void onPostExecute(String s){
                try{
                    bitmap = resizeBitmap(getOutputMediaFileUri(1), bitmap);
                } catch(Exception ex){
                    Log.d("aven", "Error: "+ex.getMessage());
                    Toast.makeText(FingerprintActivity.this,"resize fail",Toast.LENGTH_LONG).show();
                }
                loading.dismiss();
            }
        };
        viewImage.execute();
        Log.d("aven", "image download execute");
    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {
        if (failureCounter == 2){
            Toast.makeText(FingerprintActivity.this, "Fingerprint Authentication Failed.", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            switch (errorCode) {
                case com.multidots.fingerprintauth.AuthErrorCodes.CANNOT_RECOGNIZE_ERROR:
                    Toast.makeText(FingerprintActivity.this, "Cannot recognize your finger print. Please try again.", Toast.LENGTH_SHORT).show();
                    failureCounter = failureCounter + 1;
                    Log.d("Lemu", "Failure Count After Adding: " + failureCounter);
                    break;
                case com.multidots.fingerprintauth.AuthErrorCodes.RECOVERABLE_ERROR:
                    mAuthMsgTv.setText(errorMessage);
                    break;
            }
        }
    }

    public Bitmap resizeBitmap(Uri filePath, Bitmap bitmap) throws IOException {
        File file = new File(filePath.getPath());
        double xFactor = 0;
        double width = Double.valueOf(bitmap.getWidth());
        Log.d("width", String.valueOf(width));
        double height = Double.valueOf(bitmap.getHeight());
        Log.d("height", String.valueOf(height));
        if (width > height) {
            xFactor = 480 / width;
        } else {
            xFactor = 480 / height;
        }
        Log.d("Nheight", String.valueOf(height * xFactor));
        Log.d("Nweight", String.valueOf(width * xFactor));
        int Nheight = (int) (xFactor * height);
        int NWidth = (int) (xFactor * width);
        bitmap = Bitmap.createScaledBitmap(bitmap, NWidth, Nheight, true);
        try {
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
        } catch (Exception ex) {
            Log.d("aven", ex.getMessage());
        }
        return bitmap;
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getDataDirectory(), IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("aven", "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + userId + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void onFingerprintSuccess(){
        String token = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final HashMap postData = new HashMap();
        postData.put("user_id", String.valueOf(prefs.getInt("id",0)));
        postData.put("fcm_token", token);
        sendToken(postData);
        Intent intent = new Intent(FingerprintActivity.this, NavigationDrawer.class);
        startActivity(intent);
    }

    public void sendToken(final HashMap postData){

        AsyncTask<Void, Void, String> sendToken = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String result = rh.sendPostRequest(SEND_TOKEN, postData);
                return result;
            }

            @Override
            protected void onPostExecute(String s){
                Log.d("TOKEN SENT","result:"+s);
            }
        };
        sendToken.execute();
    }
}
