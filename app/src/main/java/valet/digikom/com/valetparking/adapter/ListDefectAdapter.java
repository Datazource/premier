package valet.digikom.com.valetparking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import valet.digikom.com.valetparking.R;

/**
 * Created by DIGIKOM-EX4 on 12/26/2016.
 */

public class ListDefectAdapter extends ArrayAdapter<String> {

    ArrayList<Integer> positions = new ArrayList<>();

    public ListDefectAdapter(Context context,List<String> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_defects, parent, false);
        }

        TextView textDefects = (TextView) convertView.findViewById(R.id.text_defect);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_defect);
        if (!positions.isEmpty()) {
            for (Integer i : positions) {
                if (i == position) {
                    checkBox.setChecked(true);
                }
            }
        }
        textDefects.setText(getItem(position));

        return convertView;
    }

    public void setSelectedPosition(ArrayList<Integer> positions) {
        this.positions = positions;

    }

}
