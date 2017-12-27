package com.example.antematix.grams;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paolo on 3/4/2017.
 */

public class CheckpointAdapter extends ArrayAdapter {

    List list = new ArrayList<>();

    public CheckpointAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void add(Checkpoint object) {
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
}
