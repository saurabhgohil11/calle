package com.finch.calle;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class UsageListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{
    private final Context context;



    private final ArrayList<UsageDetail> data;
    String minStr;
    String outgoingStr;
    String incomingStr;

    public UsageListViewAdapter(Context context, ArrayList<UsageDetail> usageDetailList) {
        super();
        this.context = context;
        this.data = usageDetailList;
        Resources res = context.getResources();
        minStr = res.getString(R.string.mins);
        outgoingStr = res.getString(R.string.outgoing);
        incomingStr = res.getString(R.string.incoming);

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_usage_history, parent, false);
        TextView billCycle = (TextView) rowView.findViewById(R.id.billCycleItem);
        TextView outgoingMins = (TextView) rowView.findViewById(R.id.outgoingItem);
        TextView incomingMins = (TextView) rowView.findViewById(R.id.incomingItem);

        billCycle.setText(data.get(position).getCycleString());
        outgoingMins.setText(outgoingStr+" "+data.get(position).getOutgoingMinutes()+" "+minStr);
        incomingMins.setText(incomingStr+" "+data.get(position).getIncomingMinutes()+" "+minStr);

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(context,UsageHistoryItemDetailActivity.class);
        i.putExtra("startdate",data.get(position).cycleDates[0].getTime());
        i.putExtra("enddate",data.get(position).cycleDates[1].getTime());
        context.startActivity(i);
    }
}

