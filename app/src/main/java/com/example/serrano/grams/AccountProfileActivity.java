package com.example.serrano.grams;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.support.v4.content.ContextCompat.getDataDir;

/**
 * Created by MholzSerrano on 10/30/2016.
 */

public class AccountProfileActivity extends Fragment {
    View myView;
    public static int userId = -1;
    public static String imgName = "avatar.jpg";
    public static TextView txtName, txtContact, txtAddress, txtBdate;
    public static Button btnSave, btnCancel;
    public static ImageView imgProfile;
    public static String timeStamp = "";
    public static final String IMAGE_DIRECTORY_NAME = "Images/User";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_ID = "id";
    public static final String UPLOAD_URL = "http://grams.antematix.com/user/saveprofile/";
    public static final String IMAGE_URL = "http://grams.antematix.com/images/usermobile/";
    public static final String MAP_URL = "";
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static boolean attachImage = false;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    private int PICK_IMAGE_REQUEST = 1;
    private Bitmap bitmap;
    //ListView infoList;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("Account Profile");
        myView = inflater.inflate(R.layout.account_profile,container,false);
        txtName = (TextView) myView.findViewById(R.id.txtName);

        txtAddress = (TextView) myView.findViewById(R.id.txtAddress);
        txtBdate = (TextView) myView.findViewById(R.id.txtBdate);
        txtContact = (TextView) myView.findViewById(R.id.txtContact);
        btnSave = (Button) myView.findViewById(R.id.btnSave);
        btnCancel = (Button) myView.findViewById(R.id.btnCancel);
        imgProfile = (ImageView) myView.findViewById(R.id.imgProfile);
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        txtName.setText(pref.getString("firstname","")+" "+pref.getString("middlename","")+" "+pref.getString("lastname",""));
        txtAddress.setText(pref.getString("address", ""));
        txtContact.setText("0"+pref.getString("contact",""));
        txtBdate.setText(pref.getString("birthdate",""));
        userId = pref.getInt("id", 0);
        new DownloadImage().execute((IMAGE_URL + pref.getString("image","avatar.jpg")));
        Log.d("aven", "URL: " + getImgProfile(1));
        btnSave.setEnabled(false);
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelProfile();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attachImage) {
                    saveChanges();
                }
            }
        });
        return myView;
    }

    public void onNetworkChange(NetworkInfo ni){
        boolean conn = ni.isConnectedOrConnecting();
        if(conn){
            Toast.makeText(getActivity().getApplicationContext(),
                    "Network Available", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Network Unavailable", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void selectImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.image_select)
                .setItems(R.array.image_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                chooseImage();
                                break;
                            case 1:
                                captureImage();
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void chooseImage() {
        imgProfile.setBackgroundColor(Color.WHITE);
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
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                if(bitmap != null) {
                    if (bitmap.getWidth() > 480) {
                        bitmap = resizeImage(fileUri, bitmap);
                    }
                    imgProfile.setImageBitmap(bitmap);
                    attachImage = true;
                    Log.d("aven", "Image set");
                    btnSave.setEnabled(true);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "The file selected is not an image.", Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (IOException e) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "The file selected is not an image.", Toast.LENGTH_SHORT)
                        .show();
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                Uri filePath = fileUri;
                try {
                    Log.d("aven", "camera: Image selected");
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                    if(bitmap != null) {
                        bitmap = resizeImage(filePath, bitmap);
                        imgProfile.setImageBitmap(bitmap);
                        attachImage = true;
                        Log.d("aven", "Image set");
                        btnSave.setEnabled(true);
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "An error occured while saving the image.", Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (IOException e) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Error saving the image.", Toast.LENGTH_SHORT)
                            .show();
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imagepath=filePath.getPath();
        File file = new File(imagepath);
        double xFactor = 0;
        double width = Double.valueOf(bitmap.getWidth());
        Log.v("WIDTH", String.valueOf(width));
        double height = Double.valueOf(bitmap.getHeight());
        Log.v("HEIGHT", String.valueOf(height));
        if(width>height){
            xFactor = 480/width;
        }
        else{
            xFactor = 480/height;
        }
        Log.v("Nheight", String.valueOf(height * xFactor));
        Log.v("Nweight", String.valueOf(width * xFactor));
        int Nheight = (int) ((xFactor*height));
        int NWidth =(int) (xFactor * width) ;
        bitmap = Bitmap.createScaledBitmap( bitmap,NWidth, Nheight, true);
        file.createNewFile();
        FileOutputStream ostream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
        ostream.close();
        return bitmap;
    }

    public Bitmap setLocalProfile(Uri filePath, Bitmap bitmap) throws IOException {
        String imagepath=filePath.getPath();
        File file = new File(imagepath);
        file.createNewFile();
        FileOutputStream ostream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
        ostream.close();
        return bitmap;
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public void saveChanges(){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        class UploadImage extends AsyncTask<Void,Void,String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getActivity(), "Please wait", "Uploading...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getActivity(),"Profile image updated",Toast.LENGTH_LONG).show();
                Log.d("aven", "Item uploaded");
                attachImage = false;
                btnSave.setEnabled(false);
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                HashMap<String,String> param = new HashMap<String,String>();
                if(attachImage){
                    param.put(KEY_IMAGE,getStringImage(bitmap));
                }
                param.put(KEY_ID,String.valueOf(userId));
                try {
                    setLocalProfile(getImgProfile(1), bitmap);
                }catch (Exception ex){
                    Toast.makeText(getActivity(),"Failed to Upload",Toast.LENGTH_LONG).show();
                    Log.d("aven", "Image Uploaded");
                }
                String result = rh.sendPostRequest(UPLOAD_URL, param);
                return result;
            }
        }
        UploadImage u = new UploadImage();
        u.execute();
    }

    private void cancelProfile() {
        Intent intent = new Intent(getActivity(), NavigationDrawer.class);
        startActivity(intent);
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
        if(savedInstanceState != null) {
            // get the file url
            fileUri = savedInstanceState.getParcelable("file_uri");
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public Uri getImgProfile(int type) {
        File mediaStorageDir = new File(
                getDataDir(getActivity()),
                IMAGE_DIRECTORY_NAME);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("aven", "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + userId + ".jpg");
        } else {
            return null;
        }
        return Uri.fromFile(mediaFile);
    }

    public File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                getDataDir(getActivity()),
                IMAGE_DIRECTORY_NAME);
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
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void setImage(Drawable drawable)
    {
        imgProfile.setBackground(drawable);
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
            setImage(image);
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

}

