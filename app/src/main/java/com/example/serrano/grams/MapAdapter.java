package com.example.serrano.grams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paolo on 2/9/2017.
 */

public class MapAdapter extends ArrayAdapter {

    MapHolder mapHolder;
    List list = new ArrayList<>();

    public MapAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void add(Maps object) {
        super.add(object);
        list.add(object);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row;
        row = convertView;
        if (row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.single_row_map,parent,false);
            mapHolder = new MapHolder();
            //mapHolder.txName = (TextView) row.findViewById(R.id.txName);
            mapHolder.tvMap = (TextView) row.findViewById(R.id.tvMap);
            row.setTag(mapHolder);
        }else{
            mapHolder = (MapHolder) row.getTag();
        }

        Maps maps = (Maps) this.getItem(position);

        mapHolder.tvMap.setText(maps.getMapName());
        return row;


    }

    static class MapHolder{
        //TextView txName;
        TextView tvMap;
    }

}
