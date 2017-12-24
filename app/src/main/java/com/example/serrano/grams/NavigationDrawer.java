package com.example.serrano.grams;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.DynamicImageLoader;
import com.kosalgeek.android.json.JsonConverter;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class NavigationDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    double chk_longitude, chk_latitude;
    double longi, lati;
    JSONObject globalJSON;
    String globalNFC_ID;
    Criteria criteria;
    boolean isInRange = false;
    boolean isLongitude = false;
    boolean isLatitude = false;
    LocationManager locationManager;
    LocationListener locationListener;
    final String LOG = "NavigationDrawer";
    final Looper looper = null;
    TextView tvName, tvEmail;
    String tagContent;
    Context context;
    String userinformation, firstname, lastname, email, username, password;
    public static String userId = "0";
    public static final String IMAGE_DIRECTORY_NAME = "GRAMS";
    public static final String IMAGE_URL = "http://grams.antematix.com/images/usermobile/";
    public static final String CHECKPOINT_URL = "http://grams.antematix.com/user/chk_checkpoint/";
    private Bitmap bitmap;
    //private JSONObject jObj;

    // NFC
    NfcAdapter nfcAdapter;
    // END NFC

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new AccountProfileActivity()).commit();

        context = getApplicationContext();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        tvName = (TextView) header.findViewById(R.id.tvName);
        tvEmail = (TextView) header.findViewById(R.id.tvEmail);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();

        tvName.setText(prefs.getString("firstname", "Firstname") + " " + prefs.getString("lastname", "Lastname"));
        tvEmail.setText(prefs.getString("email", "grams@gmail.com"));

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            //Toast.makeText(this,"NFC Available!",Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(this,"NFC Not Available!",Toast.LENGTH_SHORT).show();
        }

        // GPS LOCATION
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                chk_longitude = location.getLongitude();
                chk_latitude = location.getLatitude();
                Log.i("GPS from Satellite", "Latitude: " + chk_latitude);
                Log.i("GPS from Satellite", "Longitude: " + chk_longitude);

                if (chk_longitude == 0.0 && chk_latitude == 0.0) {
                    if (ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestSingleUpdate(criteria, locationListener, looper);
                }else {
                    checkGPS(chk_longitude, chk_latitude);

                    if (isInRange) {
                        Toast.makeText(NavigationDrawer.this, "Checkpoint ID found.", Toast.LENGTH_SHORT).show();
                        PatrolReportsActivity fragmentObj = new PatrolReportsActivity();
                        Bundle checkpointName = new Bundle();
                        try {

                            checkpointName.putInt("chk_id", (globalJSON.getInt("id")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        checkpointName.putString("checkpoint", globalNFC_ID);
                        fragmentObj.setArguments(checkpointName);
                        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragmentObj).commit();
                    } else {
                        Toast.makeText(NavigationDrawer.this, "Not within checkpoint range", Toast.LENGTH_SHORT).show();
                        PatrolReportsActivity fragmentObj = new PatrolReportsActivity();
                        Bundle checkpointName = new Bundle();
                        checkpointName.putInt("chk_id", 0);
                        checkpointName.putString("checkpoint", "");
                        fragmentObj.setArguments(null);
                        getFragmentManager().beginTransaction().replace(R.id.content_frame, fragmentObj).commit();
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(NavigationDrawer.this, "Please turn on your location", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                }, 10);
                return;
            } else {
                getGPS();
            }

        }
        // GPS LOCATION END
    }
    // GPS

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                }
        }
    }

    private void getGPS() {

        chk_longitude = 0.0;
        chk_latitude = 0.0;

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
            locationManager.requestSingleUpdate(criteria, locationListener, looper);
    }
    // END GPS

    // NFC
    @Override
    protected void onNewIntent(Intent intent) {

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            //Toast.makeText(this, "NFC Intent Received", Toast.LENGTH_SHORT).show();
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (parcelables != null && parcelables.length > 0) {
                //Toast.makeText(this, "Parcelables Received", Toast.LENGTH_SHORT).show();

                //GETTING TEXT FROM METHOD
                String nfcContent = readTextFromMessage((NdefMessage) parcelables[0]);
                //Toast.makeText(this, nfcContent, Toast.LENGTH_SHORT).show();
                //END GET

                /*Bundle bundle = new Bundle();
                bundle.putString("checkpoint",nfcContent);*/
                validateCheckpoint(nfcContent);

            } else {
                Toast.makeText(this, "No NDEF Message", Toast.LENGTH_SHORT);
            }
        }
        super.onNewIntent(intent);
    }

    public void validateCheckpoint(final String nfc_id) {
        AsyncTask<Void, Void, String> getCheckpoint = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(NavigationDrawer.this, "Please wait", "Validating checkpoint information...", false, false);
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("nfc_id", nfc_id);
                String result = rh.sendPostRequest(CHECKPOINT_URL, param);
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                String chkId = s;
                globalNFC_ID = nfc_id;
                Log.d("CHK", s);
                getGPS();
                try {
                    JSONObject chkObject = new JSONObject(s);
                    globalJSON = chkObject;
                    if (globalJSON.getString("result").equals("success")) {
                        longi = globalJSON.getDouble("longitude");
                        lati = globalJSON.getDouble("latitude");
                        Log.d("GPS from Database", "Longitude: " + longi);
                        Log.d("GPS from Database", "Latitude: " + lati);
                    } else {
                        Toast.makeText(NavigationDrawer.this, "Checkpoint ID not found.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loading.dismiss();
            }
        };
        getCheckpoint.execute();
    }

    private boolean checkGPS(double gps_longitude, double gps_latitude) {
        double tolerance = 0.005;

        Log.d("GPS Longitude Condition",longi + "<=" + (gps_longitude+tolerance));
        Log.d("GPS Longitude Condition",longi + ">=" + (gps_longitude-tolerance));
        Log.d("GPS Latitude Condition",lati + ">=" + (gps_latitude-tolerance));
        Log.d("GPS Latitude Condition",lati + "<=" + (gps_latitude+tolerance));

        if (longi <= gps_longitude + tolerance && longi >= gps_longitude - tolerance) {
            Log.d("GPS Longitude Condition","SUCCEEDED");
            isLongitude = true;
        } else {
            Log.d("GPS Longitude Condition","FAILED");
            isLongitude = false;
        }

        if(lati <= gps_latitude + tolerance && lati >= gps_latitude - tolerance){
            Log.d("GPS Latitude Condition","SUCCEEDED");
            isLatitude = true;
        }else {
            Log.d("GPS Latitude Condition","FAILED");
            isLatitude = false;
        }


        if(isLatitude == true && isLongitude == true){
            isInRange = true;
        }

        Log.d("GPS Comparison Result", String.valueOf(isInRange));
        return isInRange;
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, NavigationDrawer.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        super.onResume();
    }

    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    private String readTextFromMessage(NdefMessage ndefMessage) {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if (ndefRecords != null & ndefRecords.length > 0) {
            NdefRecord ndefRecord = ndefRecords[0];

            tagContent = getTextFromNdefRecord(ndefRecord);
            //Toast.makeText(this,tagContent, Toast.LENGTH_SHORT);
            Log.i("Content",tagContent);
        } else {
            Toast.makeText(this, "No NDEF Message", Toast.LENGTH_SHORT);
        }
        return tagContent;
    }

    public String getTextFromNdefRecord(NdefRecord ndefRecord)
    {
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1,
                    payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }
    // NFC END

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /**if (id == R.id.action_settings) {
            return true;
        }**/
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.account_profile) {
            // Handle the camera action
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            fragmentManager.beginTransaction().replace(R.id.content_frame, new AccountProfileActivity()).commit();
        } else if (id == R.id.patrol_map) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new PatrolMapActivity()).commit();

        } else if (id == R.id.patrol_details) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new PatrolDetailsActivity()).commit();

        } else if (id == R.id.patrol_reports) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new PatrolReportsActivity()).commit();

        } else if (id == R.id.logout) {
            Intent intent = new Intent(NavigationDrawer.this, MainActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
