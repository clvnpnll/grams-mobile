package com.example.antematix.grams;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class PatrolDetailsActivity extends Fragment {

    View myView;
    private ListView lstSchedule;
    public  String SCHEDULE_URL = "http://grams.antematix.com/user/getSchedule/";
    JSONObject jsonObject;
    JSONArray jsonArray;
    ScheduleAdapter scheduleAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("Assignment");
        myView = inflater.inflate(R.layout.patrol_details, container, false);

        lstSchedule = (ListView) myView.findViewById(R.id.listSchedule);
        scheduleAdapter = new ScheduleAdapter(getActivity(),R.layout.row_layout);
        lstSchedule.setAdapter(scheduleAdapter);



        getSchedule();
        return myView;
    }



    // SCHEDULE FEATURE

    public void getSchedule(){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();


        final String source_id = String.valueOf(preferences.getInt("id", 0));
        AsyncTask<Void,Void,String> sendReport = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getActivity(), "Please wait", "Getting schedule", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                //Toast.makeText(myView.getContext(),"PUMASOK SA POST EXECUTE",Toast.LENGTH_LONG).show();
                if(s != null) {
                    try {
                        jsonObject = new JSONObject(s);
                        jsonArray = jsonObject.getJSONArray("schedule");
                        int count = 0;
                        String chkName, chkStart, chkEnd, chkStatus;

                        while (count < jsonArray.length()) {
                            JSONObject JO = jsonArray.getJSONObject(count);
                            chkName = JO.getString("checkpoint_id");
                            chkStart = JO.getString("start_time");
                            chkEnd = JO.getString("end_time");
                            chkStatus = JO.getString("tag_status");
                            Schedule schedules = new Schedule(chkName,chkStart,chkEnd,chkStatus);
                            scheduleAdapter.add(schedules);
                            count++;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                   // txtSched.setText(s);
                    Log.d("sched",s);
                }else{
                    Toast.makeText(myView.getContext(),"No schedules found",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                HashMap<String,String> param = new HashMap<String,String>();
                    param.put("id",source_id);
                    String result = rh.sendPostRequest(SCHEDULE_URL,param);
                Log.d("result",result);
                return result;
            }
        }.execute();
    }
}
