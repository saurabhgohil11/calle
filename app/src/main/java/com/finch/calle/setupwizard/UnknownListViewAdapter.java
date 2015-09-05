package com.finch.calle.setupwizard;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.finch.calle.CallDetails;
import com.finch.calle.CostType;
import com.finch.calle.R;
import com.finch.calle.UsageHistoryItemDetailActivity;

import java.util.ArrayList;

public class UnknownListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private final Context context;


    private final ArrayList<CallDetails> data;


    public UnknownListViewAdapter(Context context, ArrayList<CallDetails> unknwonNumberList) {
        super();
        this.context = context;
        this.data = unknwonNumberList;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_unknown, parent, false);
        TextView nameOrNumber = (TextView) rowView.findViewById(R.id.nameorNumberItem);
        TextView number = (TextView) rowView.findViewById(R.id.phoneNumberItem);

        if(data.get(position).getCachedContactName()!=null && !data.get(position).getCachedContactName().isEmpty()) {
            nameOrNumber.setText(data.get(position).getCachedContactName());
            number.setVisibility(View.VISIBLE);
            number.setText(data.get(position).getPhoneNumber());
        } else {
            nameOrNumber.setText(data.get(position).getPhoneNumber());
            number.setVisibility(View.GONE);
        }

        final Button free = (Button) rowView.findViewById(R.id.free_button);
        final Button local = (Button) rowView.findViewById(R.id.local_button);
        final Button std = (Button) rowView.findViewById(R.id.std_button);
        final Button unknown = (Button) rowView.findViewById(R.id.unknown_button);
        unknown.setSelected(true);

        class OnButtonClickListener implements View.OnClickListener {
            int position;

            OnButtonClickListener(int position) {
                this.position = position;
            }
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.free_button:
                        free.setSelected(true);
                        local.setSelected(false);
                        std.setSelected(false);
                        unknown.setSelected(false);
                        data.get(position).setCostType(CostType.FREE);
                        break;
                    case R.id.std_button:
                        free.setSelected(false);
                        local.setSelected(false);
                        std.setSelected(true);
                        unknown.setSelected(false);
                        data.get(position).setCostType(CostType.STD);
                        break;
                    case R.id.local_button:
                        free.setSelected(false);
                        local.setSelected(true);
                        std.setSelected(false);
                        unknown.setSelected(false);
                        data.get(position).setCostType(CostType.LOCAL);
                        break;
                    case R.id.unknown_button:
                        free.setSelected(false);
                        local.setSelected(false);
                        std.setSelected(false);
                        unknown.setSelected(true);
                        data.get(position).setCostType(CostType.UNKNOWN);
                        break;
                }
            }
        }

        OnButtonClickListener onClickListener =  new OnButtonClickListener(position);
        free.setOnClickListener(onClickListener);
        local.setOnClickListener(onClickListener);
        std.setOnClickListener(onClickListener);
        unknown.setOnClickListener(onClickListener);
        return rowView;
    }


    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public ArrayList<CallDetails> getAllData() {
        return data;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(context, UsageHistoryItemDetailActivity.class);

        context.startActivity(i);
    }
}
