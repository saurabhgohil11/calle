package com.evadroid.calle;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.Date;


public class UsageHistoryItemDetailActivity extends AppCompatActivity {

    private Date[] billCycleDates;
    CallMinutesCardView mIncomingCard;
    CallMinutesCardView mOutgoingCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_history_item_detail);

        if (!AppGlobals.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        billCycleDates = new Date[2];
        billCycleDates[0] = new Date(getIntent().getExtras().getLong("startdate"));
        billCycleDates[1] = new Date(getIntent().getExtras().getLong("enddate"));

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle(AppGlobals.getBillCycleString(billCycleDates));

        mIncomingCard = (CallMinutesCardView) findViewById(R.id.usage_incoming_card);
        mOutgoingCard = (CallMinutesCardView) findViewById(R.id.usage_outgoing_card);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIncomingCard.setCycleAndType(billCycleDates, CallType.INCOMING);
        mOutgoingCard.setCycleAndType(billCycleDates, CallType.OUTGOING);
        mIncomingCard.updateCallMinutesCard();
        mOutgoingCard.updateCallMinutesCard();
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
}
