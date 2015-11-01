package com.evadroid.calle;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.evadroid.calle.utils.DateTimeUtils;

import java.util.ArrayList;

public class UsageListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private final Context context;
    private final ArrayList<UsageDetail> data;
    String outgoingStr;
    String incomingStr;

    public UsageListViewAdapter(Context context, ArrayList<UsageDetail> usageDetailList) {
        super();
        this.context = context;
        this.data = usageDetailList;
        Resources res = context.getResources();
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
        if (AppGlobals.isMinuteMode) {
            outgoingMins.setText(outgoingStr + "\n" + DateTimeUtils.timeToRoundedString(data.get(position).getOutgoingSeconds()));
            incomingMins.setText(incomingStr + "\n" + DateTimeUtils.timeToRoundedString(data.get(position).getIncomingSeconds()));
        } else {
            outgoingMins.setText(outgoingStr + "\n" + DateTimeUtils.timeToString(data.get(position).getOutgoingSeconds()));
            incomingMins.setText(incomingStr + "\n" + DateTimeUtils.timeToString(data.get(position).getIncomingSeconds()));
        }
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
        Intent i = new Intent(context, UsageHistoryItemDetailActivity.class);
        i.putExtra("startdate", data.get(position).cycleDates[0].getTime());
        i.putExtra("enddate", data.get(position).cycleDates[1].getTime());
        context.startActivity(i);
    }
}

