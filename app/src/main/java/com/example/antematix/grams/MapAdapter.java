package com.example.antematix.grams;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
