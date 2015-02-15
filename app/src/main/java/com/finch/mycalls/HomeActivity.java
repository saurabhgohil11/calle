package com.finch.mycalls;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

        thisMonthButton = (Button) findViewById(R.id.this_month_button);
        logsButton = (Button) findViewById(R.id.logs_button);
        usageHistoryButton = (Button) findViewById(R.id.usage_history_button);

        tabThisMonth = (LinearLayout) findViewById(R.id.this_month_tab);
        tabLogsHistory = (LinearLayout) findViewById(R.id.logs_history_tab);
        tabUsageHistory = (LinearLayout) findViewById(R.id.usage_history_tab);

        thisMonthButton.setSelected(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListView numberlistView = (ListView) findViewById(R.id.list_logs);
        SwipeListViewAdapter sw = new SwipeListViewAdapter(this);
        numberlistView.setAdapter(sw);
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
                tabThisMonth.bringToFront();
                break;
            case R.id.logs_button:
                thisMonthButton.setSelected(false);
                usageHistoryButton.setSelected(false);
                tabLogsHistory.bringToFront();
                break;
            case R.id.usage_history_button:
                thisMonthButton.setSelected(false);
                logsButton.setSelected(false);
                tabUsageHistory.bringToFront();// Prior to KITKAT this method should be followed by calls to requestLayout() and invalidate() on the view's parent to force the parent to redraw with the new child ordering.
                break;
        }
        button.setSelected(true);
    }
}
