package com.example.antematix.grams;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by MholzSerrano on 2/2/2017.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG ="MyFirebaseInsIDService";

    @Override
    public void onTokenRefresh() {
        //get update token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "New Token: " + refreshedToken);


        //you can save the token to the server to do what you want
    }


}
