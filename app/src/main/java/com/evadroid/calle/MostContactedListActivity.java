package com.evadroid.calle;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.evadroid.calle.utils.DateTimeUtils;
import com.evadroid.calle.widget.ContactOptionsPopup;

import java.util.ArrayList;
import java.util.Date;

public class MostContactedListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_most_contacted_list);
        if (!AppGlobals.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        ListView listView = (ListView) findViewById(R.id.most_contacted_list_view);
        listView.setAdapter(new MostContactedListViewAdapter(this, HomeActivity.getTopTenContacts()));
        TextView billCycle = (TextView) findViewById(R.id.bill_cycle);
        if (HomeActivity.getTopTenContacts() != null && HomeActivity.getTopTenContacts().size() > 0) {
            Date[] cycleDates = HomeActivity.getTopTenContacts().get(0).cycleDates;
            billCycle.setText(AppGlobals.getBillCycleString(cycleDates));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class MostContactedListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private final Context context;
        private final ArrayList<SummarizedCallDetail> data;
        String outgoingStr;
        String incomingStr;

        public MostContactedListViewAdapter(Context context, ArrayList<SummarizedCallDetail> detailList) {
            super();
            this.context = context;
            this.data = detailList;
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
            View rowView = inflater.inflate(R.layout.list_item_most_contacted, parent, false);
            TextView name = (TextView) rowView.findViewById(R.id.nameorNumberItem);
            TextView outgoingMins = (TextView) rowView.findViewById(R.id.outgoing_item);
            TextView incomingMins = (TextView) rowView.findViewById(R.id.incoming_item);
            TextView totalMins = (TextView) rowView.findViewById(R.id.total_mins_item);
            Button contactImageButton = (Button) rowView.findViewById(R.id.contactImageButton);

            final SummarizedCallDetail details = data.get(position);

            if (details.cachedContactName == null || details.cachedContactName.isEmpty()) {
                contactImageButton.setText("?");
                name.setText(details.nationalNumber);
            } else {
                contactImageButton.setText(String.valueOf(details.cachedContactName.toUpperCase().charAt(0)));
                name.setText(details.cachedContactName);
            }

            totalMins.setText(DateTimeUtils.timeToRoundedString(details.outgoingDuration + details.incomingDuration));
            outgoingMins.setText(outgoingStr + " " + DateTimeUtils.timeToRoundedString(details.outgoingDuration));
            incomingMins.setText(incomingStr + " " + DateTimeUtils.timeToRoundedString(details.incomingDuration));

            int backgroundIndex = AppGlobals.getRandomBackgroundIndex();
            contactImageButton.setBackgroundResource(AppGlobals.bgColoredImages[backgroundIndex]);

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactOptionsPopup.getInstance(getBaseContext()).show(v, details.phoneNumber, true);
                }
            });
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
            i.putExtra("national_number", data.get(position).nationalNumber);
            context.startActivity(i);
        }
    }
}

