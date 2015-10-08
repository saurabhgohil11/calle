package com.evadroid.calle.setupwizard;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.evadroid.calle.AppGlobals;
import com.evadroid.calle.CallDetails;
import com.evadroid.calle.CallType;
import com.evadroid.calle.DataBaseHelper;
import com.evadroid.calle.HomeActivity;
import com.evadroid.calle.PhoneNumber;
import com.evadroid.calle.R;

import java.util.ArrayList;


public class LogAnalyzerActivity extends ActionBarActivity {
    LogsWorker logsWorker;
    final Animation fadein = new AlphaAnimation(0.0f, 1.0f);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_analyzer);
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        final TextView progresstextview = (TextView)findViewById(R.id.analyzing_text);
        final TextView unknwontextview = (TextView)findViewById(R.id.unknown_logs_detected);
        final TextView unknwontextview2 = (TextView)findViewById(R.id.unknown_logs_detected_2);
        final Button categorizeButton = (Button) findViewById(R.id.categorize_button);
        final Button skipButton = (Button) findViewById(R.id.skip_button);

        logsWorker = new LogsWorker(this, progressBar, progresstextview,unknwontextview,unknwontextview2,categorizeButton,skipButton);
        logsWorker.execute();
        progresstextview.startAnimation(fadein);
    }

    @Override
    public void onBackPressed()
    {
        if(logsWorker.getStatus() == AsyncTask.Status.RUNNING) {
            Toast.makeText(this,R.string.please_wait_logs_analysing,Toast.LENGTH_LONG).show();
            return;
        }
        super.onBackPressed();  // optional depending on your needs
    }
}

class LogsWorker extends AsyncTask<Void, Integer, Void> {

    private final Activity parent;
    private final ProgressBar progressBar;
    private final TextView progressTextView;
    private final TextView categorizeTextView;
    private final TextView categorizeTextView2;
    private final Button categorizeButton;
    private final Button skipButton;
    AppGlobals appGlobals;
    DataBaseHelper dbHelper;
    final Animation fadein = new AlphaAnimation(0.0f, 1.0f);

    public LogsWorker(final Activity parent, final ProgressBar progressBar, final TextView progressTextView, final TextView categorizeTextView, final TextView categorizeTextView2, final Button categorizeButton, Button skipButton) {
        this.parent = parent;
        this.progressBar = progressBar;
        this.progressTextView = progressTextView;
        this.categorizeButton = categorizeButton;
        this.categorizeTextView = categorizeTextView;
        this.categorizeTextView2 = categorizeTextView2;
        this.skipButton = skipButton;
        fadein.setDuration(900);
    }

    @Override
    protected void onPreExecute() {
        appGlobals = AppGlobals.getInstance(parent);
        dbHelper = AppGlobals.getDataBaseHelper(parent);
    }

    @Override
    protected Void doInBackground(final Void... params) {
        CallDetails callDetails = new CallDetails();
        Uri contacts = CallLog.Calls.CONTENT_URI;
        String whereClause = CallLog.Calls.TYPE+ " IN("+CallLog.Calls.INCOMING_TYPE+","+CallLog.Calls.OUTGOING_TYPE+") AND "+
                CallLog.Calls.DURATION+">"+0;
        String sortOrder = CallLog.Calls.DATE+ " ASC";
        Cursor managedCursor = parent.getContentResolver().query(contacts, null, null, null, sortOrder);
        int numberid = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int typeid = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int cachedNameid = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int durationid = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        //call log fields at java.lang.Thread.run(Thread.java:818)
        //Caused by: android.database.sqlite.SQLiteException: near "AESC": syntax error (code 1): , while compiling: SELECT contactid, logtype, sim_id, real_phone_number, presentation, remind_me_later_set, e164_number, call_out_duration, vvm_id, countryiso, dormant_set, photo_id, type, is_read, address, number, photoring_uri, cityid, m_content, sdn_alpha_id, subscription_component_name, name, normalized_number, sec_custom1, raw_contact_id, simnum, country_code, fname, formatted_number, numbertype, sec_custom2, sns_tid, duration, account_id, geocoded_location, transcription, lookup_uri, cdnip_number, sns_pkey, frequent, messageid, subscription_id, _id, bname, sns_receiver_count, sp_type, pinyin_name, cnap_name, features, voicemail_uri, new, sec_custom3, date, data_usage, numberlabel, reject_flag, service_type, m_subject, spam_report, matched_number, account_name, lname FROM logs WHERE (logs.logtype=100 OR logs.logtype=150 OR logs.logtype=110 OR logs.logtype=900 OR logs.logtype=500 OR logs.logtype=800 OR logs.logtype=120 OR logs.logtype=510 OR logs.logtype=1000 OR logs.logtype=1150 OR logs.logtype=150 OR ((logs.sec_custom3 IS NULL ) AND logs.logtype=200 AND number NOT IN (SELECT number FROM logs WHERE number LIKE '%@%')) OR (logs.logtype=300 AND number NOT IN (SELECT number FROM logs WHERE number LIKE '%@%')) OR (logs.logtype=1200 AND number NOT IN (SELECT number FROM logs WHERE number LIKE '%@%')) OR logs.logtype=950 OR logs.logtype=1300 OR logs.logtype=1250) AND ((((type != 4)) AND (logtype=100 OR logtype=500 OR logtype=800 OR logtype=950 OR logtype=1000))) ORDER BY date AESC

        progressBar.setMax(managedCursor.getCount());
        AppGlobals.log(this," total logs to be read:"+managedCursor.getCount());

        int i=0;
        while (managedCursor.moveToNext()) {
            i++;
            int duration = managedCursor.getInt(durationid);
            /*if (duration <= 0) {
                Log.e(AppGlobals.LOG_TAG, "duration is null don't add to log");
                continue;
            }*/
            callDetails.duration = duration;
            callDetails.cachedContactName = managedCursor.getString(cachedNameid);
            callDetails.phoneNumber = managedCursor.getString(numberid);
            callDetails.date = managedCursor.getLong(date);

            String callType = managedCursor.getString(typeid);
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    callDetails.callType = CallType.OUTGOING;
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    callDetails.callType = CallType.INCOMING;
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    callDetails.callType = CallType.MISSED;
                    break;
            }
            PhoneNumber n = new PhoneNumber(parent,dbHelper, callDetails.phoneNumber);
            callDetails.costType = n.getCostType();
            callDetails.nationalNumber = n.getNationalNumber();
            callDetails.phoneNumberType = n.getPhoneNumberType();
            callDetails.numberLocation = n.getPhoneNumberLocation();
            callDetails.isHidden = false;
            callDetails.isRoaming = false;
            dbHelper.addToLogsHistory(callDetails,false);
            progressBar.setProgress(i);
        }
        managedCursor.close();
        return null;
    }


    @Override
    protected void onProgressUpdate(final Integer... values) {

    }

    @Override
    protected void onPostExecute(final Void result) {

        progressTextView.setText(R.string.analyzing_complete);
        progressTextView.startAnimation(fadein);
        progressBar.setVisibility(View.GONE);

        fadein.setDuration(1400);
        ArrayList<CallDetails> unknwonList = dbHelper.getUnknownLogsHistory();
        if(unknwonList!=null && !unknwonList.isEmpty()) {
            categorizeTextView.setVisibility(View.VISIBLE);
            categorizeTextView2.setVisibility(View.VISIBLE);
            categorizeButton.setVisibility(View.VISIBLE);
            skipButton.setVisibility(View.VISIBLE);
            categorizeTextView.startAnimation(fadein);
            categorizeTextView2.startAnimation(fadein);
            fadein.setDuration(1900);
            categorizeButton.startAnimation(fadein);
            skipButton.startAnimation(fadein);
            categorizeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parent.startActivity(new Intent(parent, UnknownNumbersActivity.class));
                }
            });

            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parent.startActivity(new Intent(parent, HomeActivity.class));
                    parent.finish();
                }
            });

        } else {
            parent.startActivity(new Intent(parent, HomeActivity.class));
            parent.finish();
        }
    }
}