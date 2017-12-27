package com.example.antematix.grams;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PatrolReportsActivity extends Fragment {

    List<Report> reports = new ArrayList<>();
    Button btnAddReport, btnImage;
    EditText etContent;
    TextView tvCheckpoint;
    View myView;
    String chkpntName = "Default";
    ImageView imgPreview;
    public static int checkpointId = -1;
    String timeStamp;
    public static final String IMAGE_DIRECTORY_NAME = "Images/Report";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_CHECKPOINT_ID = "checkpoint_id";
    public static final String KEY_TARGET_ID = "target_id";
    public static final String KEY_ID = "source_id";
    public static final String REPORT_URL = "http://grams.antematix.com/user/sendreport/";
    public static final String CHECKPOINT_URL = "http://grams.antematix.com/user/get_checkpoint/";
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static List<JSONObject> checkpoints = new ArrayList<>();
    private static boolean attachImage = false;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    private int PICK_IMAGE_REQUEST = 1;
    private Bitmap bitmap;
    ReportsDatabaseHelper dbhelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("Patrol Report");
        dbhelper = ReportsDatabaseHelper.getInstance(getActivity());
        myView = inflater.inflate(R.layout.patrol_reports, container, false);
        etContent = (EditText) myView.findViewById(R.id.etContent);
        btnAddReport = (Button) myView.findViewById(R.id.btnAddReport);
        btnImage = (Button) myView.findViewById(R.id.btnImage);

        btnAddReport.setEnabled(false);
        btnImage.setEnabled(false);

        //NFC
        tvCheckpoint = (TextView) myView.findViewById(R.id.txtCheckpoint);
        //NFC END

        imgPreview = (ImageView) myView.findViewById(R.id.imgPreview);
        btnImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        //       btnAddReport.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (!tvCheckpoint.getText().equals("Default")) {
//                    if (isNetworkAvailable()) {
//                        sendReport();
//                    } else {
//                        saveReport();
//                    }
//                } else {
//                    Toast.makeText(getActivity(), "No Checkpoint Tagged", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        btnAddReport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReport();
            }
        });

        //        if (isNetworkAvailable()) {
//            syncReports();
//        }

        if (this.getArguments() != null) {
            chkpntName = this.getArguments().getString("checkpoint");
            checkpointId = this.getArguments().getInt("chk_id");
            btnAddReport.setEnabled(true);
            btnImage.setEnabled(true);
        }
        tvCheckpoint.setText(chkpntName);
        return myView;
    }


    // REPORT SENDING

    public void sendReport() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();
        final String content = etContent.getText().toString().trim();
        final String checkpoint_name = chkpntName;
        final String source_id = String.valueOf(preferences.getInt("id", 0));
        final String handler_id = String.valueOf(preferences.getInt("handler_id", 0));
        AsyncTask<Void, Void, String> sendReport = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getActivity(), "Please wait", "Sending Report...", false, false);
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()).format(new Date());
                HashMap<String, String> param = new HashMap<String, String>();
                param.put(KEY_CONTENT, content);
                if (attachImage) {
                    param.put(KEY_IMAGE, getStringImage(bitmap));
                }
                param.put(KEY_TIMESTAMP, String.valueOf(timeStamp));
                param.put(KEY_CHECKPOINT_ID, String.valueOf(checkpointId));
                param.put(KEY_TARGET_ID, String.valueOf(handler_id));
                param.put(KEY_ID, source_id);
                String result = rh.sendPostRequest(REPORT_URL, param);
                Log.d("aven", "report: "+result);
                Log.i("aven", "report: "+result);
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getActivity(), "Report Sent", Toast.LENGTH_SHORT).show();
                Log.d("aven", "Report uploaded!");
                Log.d("aven", "Report content: " + s);
                tvCheckpoint.setText("Default");
                btnAddReport.setEnabled(false);
                btnImage.setEnabled(false);
                etContent.setText("");
                imgPreview.setImageDrawable(null);
                attachImage = false;
            }
        }.execute();
    }

    // END

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void selectImage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.image_select)
                .setItems(R.array.image_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                chooseImage();
                                imgPreview.setBackgroundColor(Color.WHITE);
                                break;
                            case 1:
                                captureImage();
                                imgPreview.setBackgroundColor(Color.WHITE);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                Log.d("aven", "gallery: Image selected");
                Log.d("aven","uri: "+filePath.toString());
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                bitmap = resizeImage(fileUri, bitmap);
                imgPreview.setImageBitmap(bitmap);
                attachImage = true;
                Log.d("aven", "Image set");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                Uri filePath = fileUri;
                try {
                    Log.d("aven", "camera: Image captured");
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                    bitmap = resizeImage(filePath, bitmap);
                    imgPreview.setImageBitmap(bitmap);
                    attachImage = true;
                    Log.d("aven", "Image set");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == getActivity().RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(getActivity().getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getActivity().getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Log.d("aven", "Not Selected");
        }
    }

    public Bitmap resizeImage(Uri filePath, Bitmap bitmap) throws IOException {
        String imagepath = filePath.getPath();
        File file = new File(imagepath);
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
        int Nheight = (int) ((xFactor * height));
        int NWidth = (int) (xFactor * width);
        bitmap = Bitmap.createScaledBitmap(bitmap, NWidth, Nheight, true);
        file.createNewFile();
        Log.d("aven", imagepath);
        FileOutputStream ostream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
        ostream.close();
        return bitmap;
    }

    public Bitmap getImage(Uri filePath, Bitmap bitmap) throws IOException {
        String imagepath = filePath.getPath();
        File file = new File(imagepath);
        file.createNewFile();
        Log.d("aven", imagepath);
        FileOutputStream ostream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
        ostream.close();
        return bitmap;
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public void syncReports() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();
        final String content = etContent.getText().toString().trim();
        final int checkpoint_id = checkpointId;
        final String source_id = String.valueOf(preferences.getInt("id", 0));
        AsyncTask<Void, Void, String> sendReport = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getActivity(), "Please wait", "Syncing...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                //Toast.makeText(getActivity(),"Reports Synced",Toast.LENGTH_SHORT).show();
                Log.d("aven", "Reports Synced");
                tvCheckpoint.setSelected(false);
                etContent.setText("");
                imgPreview.setImageDrawable(null);
                // tvCheckpoint.setSelection(0);
                attachImage = false;
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                HashMap<String, String> param = new HashMap<String, String>();
                reports = dbhelper.getAllReports(source_id);
                for (Report report : reports) {
                    param.put(KEY_CONTENT, report.content);
                    if (!report.image.equals("default.jpg")) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(report.image));
                            param.put(KEY_IMAGE, getStringImage(bitmap));
                        } catch (IOException e) {
                            Log.d("aven", "Error: Syncing reports");
                            e.printStackTrace();
                        }
                    }
                    param.put(KEY_CHECKPOINT_ID, String.valueOf(report.checkpoint_id));
                    param.put(KEY_ID, source_id);
                    String result = rh.sendPostRequest(REPORT_URL, param);
                    dbhelper.deleteReport(report);
                }
                return "done";
            }
        }.execute();
    }

    public void saveReport() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();
        final String content = etContent.getText().toString().trim();
        final int checkpoint_id = checkpointId;
        final String source_id = String.valueOf(preferences.getInt("id", 0));
        AsyncTask<Void, Void, String> saveReport = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                try {
                    resizeImage(fileUri, bitmap);
                } catch (Exception ex) {
                    Log.d("aven", "Image saving error");
                }
                loading = ProgressDialog.show(getActivity(), "Please wait", "Saving...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getActivity(), "Report Saved", Toast.LENGTH_SHORT).show();
                Log.d("aven", "Report Saved");
                tvCheckpoint.setSelected(false);
                etContent.setText("");
                imgPreview.setImageDrawable(null);
                // tvCheckpoint.setSelection(0);
                attachImage = false;
            }

            @Override
            protected String doInBackground(Void... params) {
                Report newReport = new Report();
                newReport.content = content;
                newReport.user = source_id;
                newReport.checkpoint_id = String.valueOf(checkpoint_id);
                if (attachImage) {
                    newReport.image = fileUri.toString();
                } else {
                    newReport.image = "default.jpg";
                }
                newReport.content = content;
                dbhelper.addReport(newReport);
                reports = dbhelper.getAllReports(newReport.user);
                for (Report report : reports) {
                    Log.d("aven", report.id + ": {content: " + report.content + ", user: " + report.user + ", image: " + report.image + ", checkpoint: " + report.checkpoint_id + "}");
                }
                return "done";
            }
        }.execute();
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            // get the file url
            fileUri = savedInstanceState.getParcelable("file_uri");
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public File getOutputMediaFile(int type) {

        // External sdcard location
        //File sdCard = Environment.getExternalStorageDirectory();
//        File dir = new File(sdCard.getAbsolutePath() + "/YourFolderName");
//        File mediaStorageDir = new File(
//                getDataDir(getActivity()),
//                IMAGE_DIRECTORY_NAME);
           File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("aven", "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (isConnected) {
            Log.d("aven", "Connected.");
        } else {
            Log.d("aven", "Not Connected.");
        }
    }
}
