package com.example.serrano.grams;

import android.content.Context;
import android.graphics.Color;
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

public class ScheduleAdapter extends ArrayAdapter {

    List list = new ArrayList<>();

    public ScheduleAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void add(Schedule object) {
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
        ScheduleHolder scheduleHolder;
        if (row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.row_layout,parent,false);
            scheduleHolder = new ScheduleHolder();
            scheduleHolder.txName = (TextView) row.findViewById(R.id.txName);
            scheduleHolder.txTime = (TextView) row.findViewById(R.id.txTime);
            scheduleHolder.txStatus = (TextView) row.findViewById(R.id.txStatus);
            row.setTag(scheduleHolder);
        }else{
            scheduleHolder = (ScheduleHolder)row.getTag();
        }

        Schedule schedules = (Schedule) this.getItem(position);
        scheduleHolder.txName.setText(schedules.getChkName());
        scheduleHolder.txTime.setText(schedules.getChkStart()+" - "+schedules.getChkEnd());

        if (schedules.getChkStatus().equalsIgnoreCase("1")){
            scheduleHolder.txStatus.setText("Done");
            scheduleHolder.txStatus.setTextColor(Color.rgb(0, 60, 5));
        }else{
            scheduleHolder.txStatus.setText("----");
            scheduleHolder.txStatus.setTextColor(Color.RED);
        }

        return row;
    }

    static class ScheduleHolder{
        TextView txName, txTime, txStatus;
    }
}
