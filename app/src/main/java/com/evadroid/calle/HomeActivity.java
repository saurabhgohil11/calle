package com.evadroid.calle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.evadroid.calle.settings.NumberListActivity;
import com.evadroid.calle.settings.SettingsActivity;
import com.evadroid.calle.setupwizard.SetupActivity;
import com.evadroid.calle.utils.DateTimeUtils;
import com.evadroid.calle.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomeActivity extends ActionBarActivity {
    Button thisMonthButton;
    Button logsButton;
    Button usageHistoryButton;
    LinearLayout tabThisMonth;
    LinearLayout tabLogsHistory;
    TextView noLogsItems;
    LinearLayout tabUsageHistory;

    public static Handler mHandler;
    public static final int UPDATE_VIEWS = 301;
    public static final int UPDATE_FLIPPERS = 302;
    public static final int UPDATE_LAST_CALL = 303;
    public static final int SHOW_CUG_DIALOG = 304;

    LinearLayout tabContainer;
    LinearLayout tabs;
    FrameLayout tabsLayoutContainer;

    //LinearLayout summaryContainer;
    //RelativeLayout mActionBar;
    //TextView mActionBarTitleView;

    public static AppGlobals appGlobals;
    SharedPreferences sp;

    //log history tab
    private RecyclerView mLogHistoryRecyclerView;
    private SimpleRecyclerViewAdapter mLogHistorySimpleAdapter;

    private static List<CallDetails> mLogsHistoryData;

    //usage History tab
    private ListView mUsageHistoryListView;
    private UsageListViewAdapter mUsageHistoryAdapter;
    private ArrayList<UsageDetail> mUsageHistoryData;

    //----this month tab starts------
    private TextView simOperator;
    private TextView simCircle;
    private TextView currentBillCycle;

    //Last call Log card
    private LinearLayout lastCallCard;
    private TextView lastCallNumber;
    private TextView lastCallType;
    private TextView lastCallDuration;

    final Animation fadein = new AlphaAnimation(0.0f, 1.0f);
    final Animation fadeout = new AlphaAnimation(1.0f, 0.0f);

    CallMinutesCardView mIncomingCard;
    CallMinutesCardView mOutgoingCard;

    //calculation mode views
    private Switch calculationModeSwitch;
    private TextView minuteModeText;
    private TextView secondModeText;

    //most contacted persons

    //----this month tab ends-----

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sp.contains(AppGlobals.PKEY_FIRST_TIME))
        {
            //first time app is used
            //persistance variables for Broadcast receiver
            SharedPreferences prefs = getSharedPreferences("CallStateReceiver", Context.MODE_PRIVATE );
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("state", -2 );
            editor.putInt("prevstate",-2);
            editor.commit();

            SharedPreferences.Editor e = sp.edit();
            e.putBoolean(AppGlobals.PKEY_FIRST_TIME,false);
            e.commit();
        }

        if(!sp.getBoolean(AppGlobals.PKEY_FIRST_TIME,false)){
            startActivity(new Intent(this,SetupActivity.class));
            finish();
        } else {
            appGlobals = AppGlobals.getInstance(this);

            mHandler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case UPDATE_VIEWS:
                            AppGlobals.log(this, "in HandleMessage : msg =UPDATE_VIEWS");
                            updateViews();
                            break;
                        case UPDATE_FLIPPERS:
                            AppGlobals.log(this, "in HandleMessage : msg =UPDATE_FLIPPERS");
                            //updateFlippers();
                            break;
                        case UPDATE_LAST_CALL:
                            AppGlobals.log(this, "in HandleMessage : msg =UPDATE_LAST_CALL");
                            updateLastCall();
                            break;
                        case SHOW_CUG_DIALOG:
                            AppGlobals.log(this, "in HandleMessage : msg =SHOW_CUG_DIALOG");
                            showFirstTimeCUGDialog();
                            break;
                    }
                }
            };

            if(!sp.getBoolean(AppGlobals.PKEY_CUG_DIALOG_SHOWN,false)) {
                mHandler.sendEmptyMessageDelayed(SHOW_CUG_DIALOG,5000);
            }

            mLogsHistoryData = AppGlobals.getDataBaseHelper(this).getLogsHistory();
            initUI();
            updateLastCall();
            updateCallCards();

            fadein.setDuration(900);
            fadeout.setDuration(900);

            if (!AppGlobals.isTablet(this)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }



    public static List<CallDetails> getLogsHistoryData() {
        return mLogsHistoryData;
    }

    private void initUI() {

        thisMonthButton = (Button) findViewById(R.id.this_month_tab_button);
        logsButton = (Button) findViewById(R.id.logs_tab_button);
        usageHistoryButton = (Button) findViewById(R.id.usage_history_tab_button);

        tabThisMonth = (LinearLayout) findViewById(R.id.this_month_tab);
        tabLogsHistory = (LinearLayout) findViewById(R.id.logs_history_tab);
        noLogsItems = (TextView) findViewById(R.id.no_items_text_view);
        tabUsageHistory = (LinearLayout) findViewById(R.id.usage_history_tab);

        tabContainer = (LinearLayout) findViewById(R.id.tabs_container);
        tabs = (LinearLayout) findViewById(R.id.tabs);
        tabsLayoutContainer = (FrameLayout) findViewById(R.id.tabs_layout_container);

        //for version 2---
        //summaryContainer = (LinearLayout) findViewById(R.id.today_summary);

        //mActionBar = (RelativeLayout) findViewById(R.id.actionbar);
        //mActionBarTitleView = (TextView) findViewById(R.id.toolbar_title);
        //mActionBarTitleView.setText(new SimpleDateFormat("E, MMM d").format(new Date()));


        //this month tab
        currentBillCycle = (TextView) findViewById(R.id.bill_cycle_dates);
        simOperator = (TextView) findViewById(R.id.sim_operator);
        simCircle = (TextView) findViewById(R.id.sim_circle);

        lastCallCard = (LinearLayout) findViewById(R.id.home_last_call_card);
        lastCallNumber = (TextView) findViewById(R.id.last_number);
        lastCallType = (TextView) findViewById(R.id.last_call_type);
        lastCallDuration = (TextView) findViewById(R.id.last_duration);

        mIncomingCard = (CallMinutesCardView) findViewById(R.id.home_incoming_card);
        mOutgoingCard = (CallMinutesCardView) findViewById(R.id.home_outgoing_card);

        minuteModeText = (TextView) findViewById(R.id.mode_minutes_text);
        secondModeText = (TextView) findViewById(R.id.mode_seconds_text);
        calculationModeSwitch = (Switch) findViewById(R.id.calculation_mode_switch);
        calculationModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = sp.edit();
                Resources res = getResources();
                if(isChecked) {
                    minuteModeText.setTextColor(res.getColor(R.color.funky_grey));
                    secondModeText.setTextColor(res.getColor(R.color.switch_second));
                    e.putString(AppGlobals.PKEY_MODE_OF_CALCULATION, AppGlobals.MODE_SECONDS);
                    AppGlobals.isMinuteMode = false;
                } else {
                    secondModeText.setTextColor(res.getColor(R.color.funky_grey));
                    minuteModeText.setTextColor(res.getColor(R.color.switch_minute));
                    e.putString(AppGlobals.PKEY_MODE_OF_CALCULATION, AppGlobals.MODE_MINUTES);
                    AppGlobals.isMinuteMode = true;
                }
                e.commit();
                AppGlobals.sendUpdateMessage();
            }
        });

        if(AppGlobals.isMinuteMode) {
            secondModeText.setTextColor(getResources().getColor(R.color.funky_grey));
            minuteModeText.setTextColor(getResources().getColor(R.color.switch_minute));
        } else {
            minuteModeText.setTextColor(getResources().getColor(R.color.funky_grey));
            secondModeText.setTextColor(getResources().getColor(R.color.switch_second));
        }

        //logs historytab
        mLogHistoryRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        mLogHistoryRecyclerView.setLayoutManager(layoutManager);
        // allows for optimizations if all item views are of the same size:
        mLogHistoryRecyclerView.setHasFixedSize(true);

        //item divider
        //mLogHistoryRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));
        mLogHistoryRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getResources()));


        // this call is actually only necessary with custom ItemAnimators
        mLogHistoryRecyclerView.setItemAnimator(new DefaultItemAnimator());

        List<CallDetails> items = HomeActivity.getLogsHistoryData();
        mLogHistorySimpleAdapter = new SimpleRecyclerViewAdapter(this,items,true);
        mLogHistoryRecyclerView.setAdapter(mLogHistorySimpleAdapter);

        tabUsageHistory.setVisibility(View.GONE);
        tabLogsHistory.setVisibility(View.GONE);
        thisMonthButton.setSelected(true);

        //usage history tab
        mUsageHistoryListView = (ListView) findViewById(R.id.usage_list_view);
        mUsageHistoryData = AppGlobals.getDataBaseHelper(this).getUsageHistoryList();
        mUsageHistoryAdapter = new UsageListViewAdapter(this,mUsageHistoryData);
        mUsageHistoryListView.setAdapter(mUsageHistoryAdapter);
        mUsageHistoryListView.setOnItemClickListener(mUsageHistoryAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!sp.getBoolean(AppGlobals.PKEY_FIRST_TIME,false)){  //for app reset
            startActivity(new Intent(this,SetupActivity.class));
            finish();
        }

        if(AppGlobals.isMinuteMode) {
            calculationModeSwitch.setChecked(false);
        } else {
            calculationModeSwitch.setChecked(true);
        }

        simOperator.setText(AppGlobals.simOperator);
        simCircle.setText(AppGlobals.circleNameMap.get(AppGlobals.userState));
        currentBillCycle.setText(AppGlobals.getCurrentBillCycleString());

        if(tabLogsHistory.getVisibility() == View.VISIBLE) {
            if (getLogsHistoryData() == null || getLogsHistoryData().isEmpty()) {
                mLogHistoryRecyclerView.setVisibility(View.GONE);
                noLogsItems.setVisibility(View.VISIBLE);
            } else {
                mLogHistoryRecyclerView.setVisibility(View.VISIBLE);
                noLogsItems.setVisibility(View.GONE);
            }
        }

        if(tabUsageHistory.getVisibility() == View.VISIBLE) {
            if (mUsageHistoryData == null || mUsageHistoryData.isEmpty()) {
                mUsageHistoryListView.setVisibility(View.GONE);
                noLogsItems.setVisibility(View.VISIBLE);
            } else {
                mUsageHistoryListView.setVisibility(View.VISIBLE);
                noLogsItems.setVisibility(View.GONE);
            }
        }
    }

    public void onCloseClicked(View v){
        finish();
    }

    public void onSettingsClicked(View v){
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onTabChanged(View button){
        if(button.isSelected()) return;

        switch(button.getId()) {
            case R.id.this_month_tab_button:
                logsButton.setSelected(false);
                usageHistoryButton.setSelected(false);
                tabThisMonth.setVisibility(View.VISIBLE);
                tabUsageHistory.setVisibility(View.GONE);
                tabLogsHistory.setVisibility(View.GONE);
                break;
            case R.id.logs_tab_button:
                thisMonthButton.setSelected(false);
                usageHistoryButton.setSelected(false);
                tabThisMonth.setVisibility(View.GONE);
                tabUsageHistory.setVisibility(View.GONE);
                tabLogsHistory.setVisibility(View.VISIBLE);
                if (getLogsHistoryData() == null || getLogsHistoryData().isEmpty()) {
                    mLogHistoryRecyclerView.setVisibility(View.GONE);
                    noLogsItems.setVisibility(View.VISIBLE);
                } else {
                    mLogHistoryRecyclerView.setVisibility(View.VISIBLE);
                    noLogsItems.setVisibility(View.GONE);
                }
                break;
            case R.id.usage_history_tab_button:
                thisMonthButton.setSelected(false);
                logsButton.setSelected(false);
                //tabUsageHistory.bringToFront();// Prior to KITKAT this method should be followed by calls to requestLayout() and invalidate() on the view's parent to force the parent to redraw with the new child ordering.
                tabThisMonth.setVisibility(View.GONE);
                tabUsageHistory.setVisibility(View.VISIBLE);
                tabLogsHistory.setVisibility(View.GONE);
                break;
        }
        button.setSelected(true);
    }

    private void updateViews() {
        updateLogHistoryList();
        updateLastCall();
        updateCallCards();
        updateUsageHistory();
    }

    private void updateUsageHistory() {
        mUsageHistoryData.clear();
        mUsageHistoryData.addAll(AppGlobals.getDataBaseHelper(this).getUsageHistoryList());
        mUsageHistoryAdapter.notifyDataSetChanged();
        mUsageHistoryListView.invalidate();
        if(tabUsageHistory.getVisibility() == View.VISIBLE) {
            if (mUsageHistoryData == null || mUsageHistoryData.isEmpty()) {
                mUsageHistoryListView.setVisibility(View.GONE);
                noLogsItems.setVisibility(View.VISIBLE);
            } else {
                mUsageHistoryListView.setVisibility(View.VISIBLE);
                noLogsItems.setVisibility(View.GONE);
            }
        }
    }

    private void updateLogHistoryList() {
        mLogsHistoryData.clear();
        mLogsHistoryData.addAll(AppGlobals.getDataBaseHelper(this).getLogsHistory());
        mLogHistorySimpleAdapter.notifyDataSetChanged();
        mLogHistoryRecyclerView.invalidate();
        if (getLogsHistoryData() == null || getLogsHistoryData().isEmpty()) {
            mLogHistoryRecyclerView.setVisibility(View.GONE);
            noLogsItems.setVisibility(View.VISIBLE);
        } else {
            mLogHistoryRecyclerView.setVisibility(View.VISIBLE);
            noLogsItems.setVisibility(View.GONE);
        }
    }


    private void updateLastCall() {
        if (lastCallCard == null) {
            AppGlobals.log(this,"returning from updateLastCall");
            return;
        }
        CallDetails call = AppGlobals.getDataBaseHelper(this).getLastCall();
        if(call!=null){
            lastCallCard.setVisibility(View.VISIBLE);
            if(call.getCachedContactName() != null && !call.getCachedContactName().isEmpty()) {
                lastCallNumber.setText(call.getCachedContactName());
            } else {
                lastCallNumber.setText(call.getPhoneNumber());
            }
            lastCallType.setText(call.getCostAndCallTypeString());
            if(AppGlobals.isMinuteMode) {
                lastCallDuration.setText(DateTimeUtils.timeToRoundedString(call.getDuration()));
            } else {
                lastCallDuration.setText(DateTimeUtils.timeToString(call.getDuration()));
            }
            lastCallNumber.startAnimation(fadein);
            lastCallType.startAnimation(fadein);
            lastCallDuration.startAnimation(fadein);
        }else{
            lastCallCard.setVisibility(View.GONE);
        }
    }

    private void updateCallCards() {
        Date cycleDates[] = AppGlobals.getCurrentBillCycleDates();
        mIncomingCard.setCycleAndType(cycleDates,CallType.INCOMING);
        mOutgoingCard.setCycleAndType(cycleDates,CallType.OUTGOING);
        mIncomingCard.updateCallMinutesCard();
        mOutgoingCard.updateCallMinutesCard();
    }

    private void showFirstTimeCUGDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        AlertDialog alertDialog;
        Resources res = getResources();
        alertDialogBuilder.setTitle(res.getString(R.string.title_want_to_add_cug));
        alertDialogBuilder.setMessage(res.getString(R.string.message_want_to_add_cug));

        alertDialogBuilder.setPositiveButton(res.getString(R.string.add_now),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(HomeActivity.this, NumberListActivity.class);
                        i.setAction("com.evadroid.action.EXCLUDED_NUMBERS");
                        startActivity(i);
                        SharedPreferences.Editor e = sp.edit();
                        e.putBoolean(AppGlobals.PKEY_CUG_DIALOG_SHOWN, true);
                        e.commit();
                    }
                });
        alertDialogBuilder.setNegativeButton(res.getString(R.string.add_later),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor e = sp.edit();
                        e.putBoolean(AppGlobals.PKEY_CUG_DIALOG_SHOWN, true);
                        e.commit();
                    }
                });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void onShowMinutesHelp(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        AlertDialog alertDialog;
        Resources res = getResources();
        alertDialogBuilder.setTitle(res.getString(R.string.dialog_title_mode_of_calculation));
        alertDialogBuilder.setMessage(res.getString(R.string.help_mode_of_calculation));

        alertDialogBuilder.setPositiveButton(res.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeMessages(SHOW_CUG_DIALOG);
            mHandler = null;
        }
        if(mLogHistorySimpleAdapter != null)
            mLogHistorySimpleAdapter.dismissDialog();
    }

}

