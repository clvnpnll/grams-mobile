package com.example.antematix.grams;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class MapActivity extends AppCompatActivity {

    Drawable mapBackground;
    public MapView mapView;
    public static String map_name, image_name;
    int source_id;
    int x, y;
    JSONObject jsonObject;
    JSONArray jsonArray;
    CheckpointAdapter checkpointAdapter;
    public static final String IMAGE_URL = "http://grams.antematix.com/images/map/";
    public static final String CHECK_URL = "http://grams.antematix.com/user/getCheckpointByMapId/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        map_name = getIntent().getStringExtra("mapName");
        image_name = getIntent().getStringExtra("imageName");
        source_id = getIntent().getIntExtra("mapId",0);
        setTitle(map_name);
        checkpointAdapter = new CheckpointAdapter(MapActivity.this, 0);
        new DownloadImage().execute(IMAGE_URL + image_name);
        getCheckpoints();

    }

    public void getCheckpoint(){
        Checkpoint check;
        int count = 0;
        Object object;
        while(count < checkpointAdapter.getCount()){
            object =  checkpointAdapter.getItem(count);
            check = (Checkpoint) object;
            Log.i("Check","Check: " + check);
            mapView.setObject(check);
            count++;
        }
    }

    public class DownloadImage extends AsyncTask<String, Integer, Drawable> {

        @Override
        protected Drawable doInBackground(String... arg0) {
            // This is done in a background thread
            return downloadImage(arg0[0]);
        }

        /**
         * Called after the image has been downloaded
         * -> this calls a function on the main thread again
         */
        protected void onPostExecute(Drawable image)
        {
            mapBackground = image;
            //Log.i("Maps","Image: " + mapBackground);
            mapView = new MapView(getApplicationContext(),mapBackground);
            setContentView(mapView);
        }


        /**
         * Actually download the Image from the _url
         * @param _url
         * @return
         */
        private Drawable downloadImage(String _url)
        {
            //Prepare to download image
            URL url;
            BufferedOutputStream out;
            InputStream in;
            BufferedInputStream buf;

            //BufferedInputStream buf;
            try {
                url = new URL(_url);
                in = url.openStream();

            /*
             * THIS IS NOT NEEDED
             *
             * YOU TRY TO CREATE AN ACTUAL IMAGE HERE, BY WRITING
             * TO A NEW FILE
             * YOU ONLY NEED TO READ THE INPUTSTREAM
             * AND CONVERT THAT TO A BITMAP
            out = new BufferedOutputStream(new FileOutputStream("testImage.jpg"));
            int i;

             while ((i = in.read()) != -1) {
                 out.write(i);
             }
             out.close();
             in.close();
             */

                // Read the inputstream
                buf = new BufferedInputStream(in);

                // Convert the BufferedInputStream to a Bitmap
                Bitmap bMap = BitmapFactory.decodeStream(buf);
                if (in != null) {
                    in.close();
                }
                if (buf != null) {
                    buf.close();
                }

                return new BitmapDrawable(bMap);

            } catch (Exception e) {
                Log.e("Error reading file", e.toString());
            }

            return null;
        }

    }

    public void getCheckpoints(){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MapActivity.this);
        final SharedPreferences.Editor editor = preferences.edit();

        AsyncTask<Void,Void,String> sendReport = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MapActivity.this, "Please wait", "Getting checkpoints", false, false);
                loading.dismiss();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if(s != null) {
                    try {
                        jsonObject = new JSONObject(s);
                        jsonArray = jsonObject.getJSONArray("checkpoint");
                        int count = 0;
                        String chkName;
                        int chkX, chkY;

                        while (count < jsonArray.length()) {
                            JSONObject JO = jsonArray.getJSONObject(count);
                            chkName = JO.getString("name");
                            chkX = JO.getInt("map_x");
                            chkY = JO.getInt("map_y");
                            Checkpoint checkpoints= new Checkpoint(chkName,chkX,chkY);
                            checkpointAdapter.add(checkpoints);
/*                            Log.i("Checkpoint", "Count: " + count);
                            Log.i("Checkpoint", "Name: " + chkName);
                            Log.i("Checkpoint", "X: " + chkX);
                            Log.i("Checkpoint", "Y: " + chkY);*/
                            count++;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // txtSched.setText(s);
                    //Log.i("Checkpoint","Checkpoint: " + s);
                    if(checkpointAdapter.getCount()!=0){
                        getCheckpoint();
                    }
                }else{
                    Toast.makeText(MapActivity.this,"No schedules found",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                HashMap<String,String> param = new HashMap<String,String>();
                param.put("id",String.valueOf(source_id));
                String result = rh.sendPostRequest(CHECK_URL,param);
                return result;
            }
        }.execute();
    }



}



