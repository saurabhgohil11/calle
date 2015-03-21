package com.finch.mycalls;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomeActivity extends ActionBarActivity {
    Button thisMonthButton;
    Button logsButton;
    Button usageHistoryButton;
    LinearLayout tabThisMonth;
    LinearLayout tabLogsHistory;
    LinearLayout tabUsageHistory;

    public static Handler mHandler;
    public static final int UPDATE_VIEWS = 301;
    public static final int UPDATE_FLIPPERS = 302;
    public static final int UPDATE_LAST_CALL = 303;

    LinearLayout tabContainer;
    LinearLayout tabs;
    LinearLayout summaryContainer;
    FrameLayout tabsLayoutContainer;
    RelativeLayout actionBar;
    TextView actionBarTitleView;

    Animation animation;

    boolean firstTimeStart; //for hiding option menu
    public static AppGlobals appGlobals;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    private static List<CallDetails> logsHistoryData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        appGlobals = new AppGlobals(this);
        
        firstTimeStart = false;

        mHandler = new Handler(Looper.getMainLooper()){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_VIEWS:
                        AppGlobals.log(this, "in HandleMessage : msg =UPDATE_VIEWS");
                        //updateViews();
                        break;
                    case UPDATE_FLIPPERS:
                        AppGlobals.log(this, "in HandleMessage : msg =UPDATE_FLIPPERS");
                        //updateFlippers();
                        break;
                    case UPDATE_LAST_CALL:
                        AppGlobals.log(this, "in HandleMessage : msg =UPDATE_LAST_CALL");
                        //updateLastCall();
                        break;
                }
            }
        };

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sp.contains(AppGlobals.PKEY_FIRST_TIME))
        {
            //first time app is used
            firstTimeStart = true;
            //persistance variables for Broadcast receiver
            SharedPreferences prefs = getSharedPreferences("CallStateReceiver", Context.MODE_PRIVATE );
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("state", -2 );
            editor.putInt("prevstate",-2);
            editor.commit();

            SharedPreferences.Editor e = sp.edit();
            e.putBoolean(AppGlobals.PKEY_FIRST_TIME,false);

            try {
                TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                AppGlobals.userCountry = manager.getSimCountryIso().toUpperCase();
                String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
                for (int i = 0; i < rl.length; i++) {
                    String[] g = rl[i].split(",");
                    if (g[1].trim().equals(appGlobals.userCountry.trim())) {
                        appGlobals.userCountryCode = "+" + g[0];
                        break;
                    }
                }
                e.putString(AppGlobals.PKEY_COUNTRY_CODE,appGlobals.userCountryCode);
                AppGlobals.simOperator = manager.getSimOperatorName().toUpperCase();
            }catch (Exception ex){
                Toast.makeText(this, "Error retrieving Network info.", Toast.LENGTH_SHORT).show();
            }
            e.commit();
        }

        if(!sp.getBoolean(AppGlobals.PKEY_FIRST_TIME,false)){
            firstTimeStart = true;
            startActivity(new Intent(this,SetupActivity.class));
            finish();
        } else {
            logsHistoryData = AppGlobals.dbHelper.getLogsHistory();
            initUI();
        }
    }

    public static final List<CallDetails> getLogsHistoryData() {
        return new ArrayList<CallDetails>(logsHistoryData);
    }

    private void initUI() {

        thisMonthButton = (Button) findViewById(R.id.this_month_button);
        logsButton = (Button) findViewById(R.id.logs_button);
        usageHistoryButton = (Button) findViewById(R.id.usage_history_button);

        tabThisMonth = (LinearLayout) findViewById(R.id.this_month_tab);
        tabLogsHistory = (LinearLayout) findViewById(R.id.logs_history_tab);
        tabUsageHistory = (LinearLayout) findViewById(R.id.usage_history_tab);

        tabContainer = (LinearLayout) findViewById(R.id.tabs_container);
        tabs = (LinearLayout) findViewById(R.id.tabs);
        tabsLayoutContainer = (FrameLayout) findViewById(R.id.tabs_layout_container);
        summaryContainer = (LinearLayout) findViewById(R.id.today_summary);

        actionBar = (RelativeLayout) findViewById(R.id.actionbar);

        actionBarTitleView = (TextView) findViewById(R.id.toolbar_title);
        actionBarTitleView.setText(new SimpleDateFormat("E, MMM d").format(new Date()));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        // allows for optimizations if all item views are of the same size:
        recyclerView.setHasFixedSize(true);

        /*RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);*/

        // this is the default;
        // this call is actually only necessary with custom ItemAnimators
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        List<CallDetails> items = HomeActivity.getLogsHistoryData();
        adapter = new RecyclerViewAdapter(items);
        recyclerView.setAdapter(adapter);

        thisMonthButton.setSelected(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

       /* numberlistView.setOnScrollListener(new PixelScrollDetector(new PixelScrollDetector.PixelScrollListener() {

            @Override
            public void onScroll(AbsListView view, float deltaY) {
                if(Math.abs(deltaY)<3) return;
                int current = (int) (tabContainer.getTop()+deltaY);
                int tabsTop = (int) (tabs.getTop()+deltaY);
                //AppGlobals.log(this,"view:"+view.getClass().getSimpleName()+"dy="+current+","+tabsTop+","+actionBar.getHeight()+","+tabContainer.getHeight());
            }
        })); */
    }

    public void onCloseClicked(View v){
        finish();
    }

    public void onSettingsClicked(View v){
        startActivity(new Intent(this,SettingsActivity.class));
    }

    public void onTabChanged(View button){
        if(button.isSelected()) return;

        switch(button.getId()) {
            case R.id.this_month_button:
                logsButton.setSelected(false);
                usageHistoryButton.setSelected(false);
                tabThisMonth.setVisibility(View.VISIBLE);
                tabUsageHistory.setVisibility(View.GONE);
                tabLogsHistory.setVisibility(View.GONE);
                break;
            case R.id.logs_button:
                thisMonthButton.setSelected(false);
                usageHistoryButton.setSelected(false);
                tabThisMonth.setVisibility(View.GONE);
                tabUsageHistory.setVisibility(View.GONE);
                tabLogsHistory.setVisibility(View.VISIBLE);
                break;
            case R.id.usage_history_button:
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

    public void openChartWebView(View listItem) {
        UsageHistoryItem i = new UsageHistoryItem(12,15,18,40,50,10);
        Intent intent = new Intent(this, ChartWebViewActivity.class);
        intent.putExtra("USAGE",i);
        startActivity(intent);
    }

    //for RecyclerView

}
