package com.example.serrano.grams;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by MholzSerrano on 10/30/2016.
 */



public class PatrolMapActivity extends Fragment {

    View myView;
    JSONObject jsonObject;
    JSONArray jsonArray;
    MapAdapter mapAdapter;
    ListView mapList;

        public  String MAP_URL = "http://grams.antematix.com/user/getMaps/";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("Patrol Maps");
        myView = inflater.inflate(R.layout.patrol_map,container,false);

        mapList = (ListView) myView.findViewById(R.id.listMap);
        mapAdapter = new MapAdapter(getActivity(),R.layout.maps);
        mapList.setAdapter(mapAdapter);

        mapList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object;
                object = mapAdapter.getItem(position);
                Maps map = (Maps) object;
                String name = map.getMapName();
                String image = map.getMapImage();
                int map_id = map.getMapId();
                Toast.makeText(getActivity(),"Name: " + name + "\nImage: " + image + "\nID: " + map_id, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getActivity(),MapActivity.class);
                i.putExtra("mapName",name);
                i.putExtra("imageName",image);
                i.putExtra("mapId",map_id);
                startActivity(i);
            }
        });


        getMaps();
        return myView;
    }

    // MAPS FEATURE

    public void getMaps(){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = preferences.edit();


        final String source_id = String.valueOf(preferences.getInt("id", 0));
        AsyncTask<Void,Void,String> sendReport = new AsyncTask<Void, Void, String>() {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(getActivity(), "Please wait", "Getting Maps", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Log.i("Maps", "Map JSON: " + s);

                if(s != null) {
                    try {
                        jsonObject = new JSONObject(s);
                        jsonArray = jsonObject.getJSONArray("map");
                        int count = 0;
                        String mapName, mapImage;
                        int mapId;
                        //Toast.makeText(myView.getContext(),"PUMASOK SA PAGKUHA NG JSON OBJECT",Toast.LENGTH_LONG).show();
                        while (count < jsonArray.length()) {
                            JSONObject JO = jsonArray.getJSONObject(count);
                            mapId = JO.getInt("id");
                            mapName = JO.getString("name");
                            mapImage = JO.getString("map_image");
                            Maps maps = new Maps(mapName,mapImage,mapId);
                            mapAdapter.add(maps);
                            count++;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // txtSched.setText(s);
                    Log.d("sched",s);
                }else{
                    Toast.makeText(myView.getContext(),"No maps found",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                HashMap<String,String> param = new HashMap<String,String>();
                param.put("id",source_id);
                String result = rh.sendPostRequest(MAP_URL,param);
                Log.d("result",result);
                return result;
            }
        }.execute();
    }

}
