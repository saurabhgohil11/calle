package com.finch.calle;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.finch.calle.setupwizard.UnknownNumbersActivity;
import com.finch.calle.utils.DateTimeUtils;
import com.finch.calle.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LogListActivity extends ActionBarActivity {

    private RecyclerView recyclerView;
    private SimpleRecyclerViewAdapter simpleAdapter;
    private TextView billCycle;
    private TextView totalMinutes;

    private static List<CallDetails> logListData;
    int seconds;

    CallType callType;
    CostType costType;
    boolean showCostType;
    Date billCycleDates[];

    public static Handler mHandler;
    public static final int UPDATE_VIEWS = 301;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_list);

        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_VIEWS:
                        AppGlobals.log(this, "in HandleMessage : msg =UPDATE_VIEWS");
                        updateViews();
                        break;
                }
            }
        };

        int i = getIntent().getExtras().getInt("costtype",-1);
        if(i>=0) {
            costType = CostType.values()[i];
            showCostType = false;
        } else {
            costType = null;
            showCostType =true;
        }
        i = getIntent().getExtras().getInt("calltype");
        callType = CallType.values()[i];

        billCycleDates = new Date[2];
        billCycleDates[0] = new Date(getIntent().getExtras().getLong("startdate"));
        billCycleDates[1] = new Date(getIntent().getExtras().getLong("enddate"));

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        if(!AppGlobals.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        StringBuffer sb = new StringBuffer();
        Resources res = getResources();
        if(costType!=null) {
            switch (costType) {
                case LOCAL:
                    sb.append(res.getString(R.string.local));
                    break;
                case STD:
                    sb.append(res.getString(R.string.std));
                    break;
                case FREE:
                    sb.append(res.getString(R.string.free));
                    break;
                case ROAMING:
                    sb.append(res.getString(R.string.roaming));
                    break;
                case ISD:
                    sb.append(res.getString(R.string.isd));
                    break;
                case UNKNOWN:
                    sb.append(res.getString(R.string.unknown));
                    findViewById(R.id.categorize_button).setVisibility(View.VISIBLE);
                    break;
            }
            sb.append(" ");
        }
        switch (callType) {
            case INCOMING:
                sb.append(res.getString(R.string.incoming_calls));
                break;
            case OUTGOING:
                sb.append(res.getString(R.string.outgoing_calls));
                break;
        }
        actionbar.setTitle(sb.toString());

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        billCycle = (TextView) findViewById(R.id.bill_cycle);
        totalMinutes = (TextView) findViewById(R.id.minutes);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        // allows for optimizations if all item views are of the same size:
        recyclerView.setHasFixedSize(true);

        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getResources()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        logListData = new ArrayList<>();

        initListData();

        simpleAdapter = new SimpleRecyclerViewAdapter(this,logListData,showCostType);
        recyclerView.setAdapter(simpleAdapter);
        billCycle.setText(getResources().getString(R.string.bill_cycle) + " : " + AppGlobals.getBillCycleString(billCycleDates));
        if (AppGlobals.isMinuteMode) {
            totalMinutes.setText(DateTimeUtils.timeToRoundedString(seconds));
        } else {
            totalMinutes.setText(DateTimeUtils.timeToString(seconds));
        }
    }

    private void updateViews() {
        initListData();
        if(logListData.isEmpty()) finish();
        simpleAdapter.notifyDataSetChanged();
        recyclerView.invalidate();
        billCycle.setText(getResources().getString(R.string.bill_cycle) + " : " + AppGlobals.getBillCycleString(billCycleDates));
        if (AppGlobals.isMinuteMode) {
            totalMinutes.setText(DateTimeUtils.timeToRoundedString(seconds));
        } else {
            totalMinutes.setText(DateTimeUtils.timeToString(seconds));
        }
    }

    private void initListData(){
        DataBaseHelper dbHelper = AppGlobals.dbHelper;
        if(dbHelper == null){
            AppGlobals.log(this,"returning from initListData due to dbHelper null");
            return;
        }
        logListData.clear();
        logListData.addAll(dbHelper.getLogList(billCycleDates[0].getTime(), billCycleDates[1].getTime(), callType, costType));
        seconds = dbHelper.getTotalSeconds(billCycleDates[0].getTime(), billCycleDates[1].getTime(), callType, costType);
    }

    public void onCategorizeClicked(View v) {
        startActivity(new Intent(this, UnknownNumbersActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler = null;
    }

}
