package com.evadroid.calle;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.evadroid.calle.settings.NumberListActivity;
import com.evadroid.calle.settings.SettingsActivity;
import com.evadroid.calle.setupwizard.SetupActivity;
import com.evadroid.calle.utils.AppRater;
import com.evadroid.calle.utils.DateTimeUtils;
import com.evadroid.calle.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomeActivity extends AppCompatActivity {
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
    public static final int DISMISS_PROGRESS_DIALOG = 305;

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

    /** Disable Last Call Feature as it seems not useful **
    //Last call Log card
    private LinearLayout lastCallCard;
    private TextView lastCallNumber;
    private TextView lastCallType;
    private TextView lastCallDuration;

    final Animation fadein = new AlphaAnimation(0.0f, 1.0f);
    final Animation fadeout = new AlphaAnimation(1.0f, 0.0f);
    **/

    CallMinutesCardView mIncomingCard;
    CallMinutesCardView mOutgoingCard;

    //calculation mode views
    private Switch calculationModeSwitch;
    private TextView minuteModeText;
    private TextView secondModeText;

    //most contacted persons
    private LinearLayout mostContactedLayout;
    private LinearLayout mostContactPerson1;
    private LinearLayout mostContactPerson2;
    private LinearLayout mostContactPerson3;
    private static ArrayList<SummarizedCallDetail> mTopTenContacts;

    //----this month tab ends-----

    boolean permissionGranted;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        permissionGranted = AppGlobals.checkForPermissions(HomeActivity.this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.contains(AppGlobals.PKEY_FIRST_TIME)) {
            //first time app is used
            //persistance variables for Broadcast receiver
            SharedPreferences prefs = getSharedPreferences("CallStateReceiver", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("state", -2);
            editor.putInt("prevstate", -2);
            editor.commit();

            SharedPreferences.Editor e = sp.edit();
            e.putBoolean(AppGlobals.PKEY_FIRST_TIME, false);
            e.commit();
        }

        if (!sp.getBoolean(AppGlobals.PKEY_FIRST_TIME, false)) {
            startActivity(new Intent(this, SetupActivity.class));
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
                            //updateLastCall();
                            break;
                        case SHOW_CUG_DIALOG:
                            AppGlobals.log(this, "in HandleMessage : msg =SHOW_CUG_DIALOG");
                            showFirstTimeCUGDialog();
                            break;
                        case DISMISS_PROGRESS_DIALOG:
                            AppGlobals.log(this, "in HandleMessage : msg =DISMISS_PROGRESS_DIALOG");
                            dismissProgressDialog();
                            break;
                    }
                }
            };

            if (!sp.getBoolean(AppGlobals.PKEY_CUG_DIALOG_SHOWN, false)) {
                mHandler.sendEmptyMessageDelayed(SHOW_CUG_DIALOG, 5000);
            }

            addMissingLogsAfterLastLog();

            mLogsHistoryData = AppGlobals.getDataBaseHelper(this).getLogsHistory();
            initUI();
            //updateLastCall();
            updateCallCards();
            updateMostContactPersons();

            //fadein.setDuration(900);
            //fadeout.setDuration(900);

            if (!AppGlobals.isTablet(this)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        if (!permissionGranted) {
            requestPermissions();
        }

        AppRater.app_launched(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            onSettingsClicked(null);
            return true;
        } else if (id == R.id.action_resync) {
            reSyncLogs();
            return true;
        } else if (id == R.id.action_rate_app) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AppRater.APP_PNAME)));
            return true;
        } else if (id == R.id.action_share_app) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.share_text_msg));
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_via)));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addMissingLogsAfterLastLog() {
        if (permissionGranted)
            new MissingLogsWorker(this, false).execute();
    }

    public static List<CallDetails> getLogsHistoryData() {
        return mLogsHistoryData;
    }


    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.READ_CALL_LOG)) {
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS},
                    AppGlobals.MY_PERMISSIONS_REQUEST);
        } else {
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS},
                    AppGlobals.MY_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppGlobals.MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addMissingLogsAfterLastLog();
                    mHandler.sendEmptyMessage(UPDATE_VIEWS);
                } else {
                    Toast.makeText(HomeActivity.this, R.string.permission_error, Toast.LENGTH_SHORT).show();
                    Resources res = getResources();
                    String title = res.getString(R.string.permission_notification_title);
                    String msg = res.getString(R.string.permission_notification_msg);
                    Intent appInfoIntent = new Intent();
                    appInfoIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", "com.evadroid.calle", null);
                    appInfoIntent.setData(uri);
                    PendingIntent pendingIntent = PendingIntent.getActivity(HomeActivity.this, (int) System.currentTimeMillis(), appInfoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(msg)
                            .setAutoCancel(true)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(10, mBuilder.build());
                    finish();
                }
                return;
            }
        }
    }


    private void initUI() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        /** Disable Last Call Feature as it seems not useful **
        lastCallCard = (LinearLayout) findViewById(R.id.home_last_call_card);
        lastCallNumber = (TextView) findViewById(R.id.last_number);
        lastCallType = (TextView) findViewById(R.id.last_call_type);
        lastCallDuration = (TextView) findViewById(R.id.last_duration);
        **/
        mIncomingCard = (CallMinutesCardView) findViewById(R.id.home_incoming_card);
        mOutgoingCard = (CallMinutesCardView) findViewById(R.id.home_outgoing_card);

        mostContactedLayout = (LinearLayout) findViewById(R.id.home_frequent_contacts);
        mostContactPerson1 = (LinearLayout) findViewById(R.id.frequent_caller_1);
        mostContactPerson2 = (LinearLayout) findViewById(R.id.frequent_caller_2);
        mostContactPerson3 = (LinearLayout) findViewById(R.id.frequent_caller_3);

        minuteModeText = (TextView) findViewById(R.id.mode_minutes_text);
        secondModeText = (TextView) findViewById(R.id.mode_seconds_text);
        calculationModeSwitch = (Switch) findViewById(R.id.calculation_mode_switch);
        calculationModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = sp.edit();
                Resources res = getResources();
                if (isChecked) {
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

        if (AppGlobals.isMinuteMode) {
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
        mLogHistorySimpleAdapter = new SimpleRecyclerViewAdapter(this, items, true);
        mLogHistoryRecyclerView.setAdapter(mLogHistorySimpleAdapter);

        tabUsageHistory.setVisibility(View.GONE);
        tabLogsHistory.setVisibility(View.GONE);
        thisMonthButton.setSelected(true);

        //usage history tab
        mUsageHistoryListView = (ListView) findViewById(R.id.usage_list_view);
        mUsageHistoryData = AppGlobals.getDataBaseHelper(this).getUsageHistoryList();
        mUsageHistoryAdapter = new UsageListViewAdapter(this, mUsageHistoryData);
        mUsageHistoryListView.setAdapter(mUsageHistoryAdapter);
        mUsageHistoryListView.setOnItemClickListener(mUsageHistoryAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sp.getBoolean(AppGlobals.PKEY_FIRST_TIME, false)) {  //for app reset
            startActivity(new Intent(this, SetupActivity.class));
            finish();
        }

        if (AppGlobals.isMinuteMode) {
            calculationModeSwitch.setChecked(false);
        } else {
            calculationModeSwitch.setChecked(true);
        }

        simOperator.setText(AppGlobals.simOperator);
        simCircle.setText(AppGlobals.circleNameMap.get(AppGlobals.userState));
        currentBillCycle.setText(AppGlobals.getCurrentBillCycleString());

        if (tabLogsHistory.getVisibility() == View.VISIBLE) {
            if (getLogsHistoryData() == null || getLogsHistoryData().isEmpty()) {
                mLogHistoryRecyclerView.setVisibility(View.GONE);
                noLogsItems.setVisibility(View.VISIBLE);
            } else {
                mLogHistoryRecyclerView.setVisibility(View.VISIBLE);
                noLogsItems.setVisibility(View.GONE);
            }
        }

        if (tabUsageHistory.getVisibility() == View.VISIBLE) {
            if (mUsageHistoryData == null || mUsageHistoryData.isEmpty()) {
                mUsageHistoryListView.setVisibility(View.GONE);
                noLogsItems.setVisibility(View.VISIBLE);
            } else {
                mUsageHistoryListView.setVisibility(View.VISIBLE);
                noLogsItems.setVisibility(View.GONE);
            }
        }
    }

    public void onCloseClicked(View v) {
        finish();
    }

    public void onSettingsClicked(View v) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onTabChanged(View button) {
        if (button.isSelected()) return;

        switch (button.getId()) {
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
        //updateLastCall();
        updateCallCards();
        updateUsageHistory();
        updateMostContactPersons();
    }

    private void updateUsageHistory() {
        mUsageHistoryData.clear();
        mUsageHistoryData.addAll(AppGlobals.getDataBaseHelper(this).getUsageHistoryList());
        mUsageHistoryAdapter.notifyDataSetChanged();
        mUsageHistoryListView.invalidate();
        if (tabUsageHistory.getVisibility() == View.VISIBLE) {
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

    /** Disable Last Call Feature as it seems not useful **
    private void updateLastCall() {
        if (lastCallCard == null) {
            AppGlobals.log(this, "returning from updateLastCall");
            return;
        }
        CallDetails call = AppGlobals.getDataBaseHelper(this).getLastCall();
        if (call != null) {
            lastCallCard.setVisibility(View.VISIBLE);
            if (call.getCachedContactName() != null && !call.getCachedContactName().isEmpty()) {
                lastCallNumber.setText(call.getCachedContactName());
            } else {
                lastCallNumber.setText(call.getPhoneNumber());
            }
            lastCallType.setText(call.getCostAndCallTypeString());
            if (AppGlobals.isMinuteMode) {
                lastCallDuration.setText(DateTimeUtils.timeToRoundedString(call.getDuration()));
            } else {
                lastCallDuration.setText(DateTimeUtils.timeToString(call.getDuration()));
            }
            lastCallNumber.startAnimation(fadein);
            lastCallType.startAnimation(fadein);
            lastCallDuration.startAnimation(fadein);
        } else {
            lastCallCard.setVisibility(View.GONE);
        }
    }
    **/

    private void updateCallCards() {
        Date cycleDates[] = AppGlobals.getCurrentBillCycleDates();
        mIncomingCard.setCycleAndType(cycleDates, CallType.INCOMING);
        mIncomingCard.updateCallMinutesCard();

        mOutgoingCard.setCycleAndType(cycleDates, CallType.OUTGOING);
        mOutgoingCard.updateCallMinutesCard();
        mOutgoingCard.showSetWarningLimitButton(true);
        mOutgoingCard.checkForLimitCross();
    }

    public static ArrayList<SummarizedCallDetail> getTopTenContacts() {
        return mTopTenContacts;
    }

    private void updateMostContactPersons() {
        boolean isFrequentContactsEnable = sp.getBoolean("show_most_contacted", true);
        Date cycleDates[] = AppGlobals.getCurrentBillCycleDates();
        mTopTenContacts = AppGlobals.getDataBaseHelper(this).getTopTenSummary(cycleDates[0].getTime(), cycleDates[1].getTime());
        if (mTopTenContacts.size() == 0 || !isFrequentContactsEnable) {
            mostContactedLayout.setVisibility(View.GONE);
        } else {
            mostContactedLayout.setVisibility(View.VISIBLE);
        }

        if (mTopTenContacts.size() > 0) {
            mostContactPerson1.setVisibility(View.VISIBLE);
            setMostContactDetails(mostContactPerson1, mTopTenContacts.get(0));
        } else {
            mostContactPerson1.setVisibility(View.GONE);
        }

        if (mTopTenContacts.size() > 1) {
            mostContactPerson2.setVisibility(View.VISIBLE);
            setMostContactDetails(mostContactPerson2, mTopTenContacts.get(1));
        } else {
            mostContactPerson2.setVisibility(View.GONE);
        }

        if (mTopTenContacts.size() > 2) {
            mostContactPerson3.setVisibility(View.VISIBLE);
            setMostContactDetails(mostContactPerson3, mTopTenContacts.get(2));
        } else {
            mostContactPerson3.setVisibility(View.GONE);
        }
    }

    private void setMostContactDetails(LinearLayout parent, SummarizedCallDetail details) {
        TextView name;  //shows name or number if name is not avialable
        TextView totalDuration;
        Button contactImageButton; //shows name text
        name = (TextView) parent.findViewById(R.id.caller_name);
        totalDuration = (TextView) parent.findViewById(R.id.total_minutes);
        contactImageButton = (Button) parent.findViewById(R.id.callerImage);
        contactImageButton.setBackgroundResource(AppGlobals.bgColoredImages[AppGlobals.getRandomBackgroundIndex()]);

        if (details.cachedContactName == null || details.cachedContactName.isEmpty()) {
            contactImageButton.setText("?");
            name.setText(details.nationalNumber);
        } else {
            contactImageButton.setText(String.valueOf(details.cachedContactName.toUpperCase().charAt(0)));
            name.setText(details.cachedContactName);
        }
        totalDuration.setText(DateTimeUtils.timeToRoundedString(details.incomingDuration + details.outgoingDuration));
    }

    public void onMoreInfoMostContactedClicked(View v) {
        startActivity(new Intent(this, MostContactedListActivity.class));
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

    private void reSyncLogs() {
        progressDialog = ProgressDialog.show(this, "Please wait!", "Updating Logs...", true);
        new MissingLogsWorker(this, true).execute();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeMessages(SHOW_CUG_DIALOG);
            mHandler = null;
        }
        if (mLogHistorySimpleAdapter != null)
            mLogHistorySimpleAdapter.dismissDialog();
    }

}

