package com.evadroid.calle;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;

/*class that adds missing logs if any logs are not added after the last log
  missing logs happens to be very rare.. this worker has very less work*/
public class MissingLogsWorker extends AsyncTask<Void, Integer, Void> {
    Context mContext;
    DataBaseHelper dbHelper;
    CallDetails lastCallLog;
    boolean isResyncTask;

    MissingLogsWorker(Context c, boolean reSync) {
        mContext = c;
        dbHelper = AppGlobals.getDataBaseHelper(c);
        lastCallLog = dbHelper.getLastCall();
        isResyncTask = reSync;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (HomeActivity.mHandler != null)
            HomeActivity.mHandler.sendEmptyMessage(HomeActivity.DISMISS_PROGRESS_DIALOG);
        if (mContext != null && isResyncTask)
            Toast.makeText(mContext, R.string.logs_resynced, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (mContext == null) {
            AppGlobals.log(this, "mContext null Here");
            return null;
        }
        if (lastCallLog == null && !isResyncTask) {
            AppGlobals.log(this, "lastLog null Here");
            return null;
        }
        if (AppGlobals.showLogs)
            AppGlobals.log(this, "Adding Missing Logs");
        CallDetails callDetails = new CallDetails();
        Uri contacts = CallLog.Calls.CONTENT_URI;

        String whereClause = null;
        if (!isResyncTask) {
            whereClause = CallLog.Calls.DATE + ">" + lastCallLog.date;
        }

        String sortOrder = CallLog.Calls.DATE + " ASC";
        Cursor managedCursor = mContext.getContentResolver().query(contacts, null, whereClause, null, sortOrder);
        int numberid = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int typeid = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int cachedNameid = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int durationid = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        AppGlobals.log(this, " total logs to be read:" + managedCursor.getCount());


        while (managedCursor.moveToNext()) {
            int duration = managedCursor.getInt(durationid);
            /*if (duration < 0) {
                Log.e(AppGlobals.LOG_TAG, "MissingLogsWorker : duration is null don't add to log");
                continue;
            }*/
            callDetails.setDuration(duration);
            callDetails.setCachedContactName(managedCursor.getString(cachedNameid));
            callDetails.setPhoneNumber(managedCursor.getString(numberid));
            callDetails.setDate(managedCursor.getLong(date));

            String callType = managedCursor.getString(typeid);
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    callDetails.setCallType(CallType.OUTGOING);
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    callDetails.setCallType(CallType.INCOMING);
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    callDetails.setCallType(CallType.MISSED);
                    break;

                default:  //calltype5 in samsung when reject incoming call
                    callDetails.setCallType(CallType.MISSED);
                    break;
            }
            PhoneNumber n = null;
            try {
                n = new PhoneNumber(mContext, dbHelper, callDetails.phoneNumber);
            } catch (NumberParseException e) {
                AppGlobals.log(mContext, "NumberParseException was thrown: " + callDetails.phoneNumber + e.toString());
                e.printStackTrace();
                continue;
            }
            callDetails.setCostType(n.getCostType());
            callDetails.setNationalNumber(n.getNationalNumber());
            callDetails.setPhoneNumberType(n.getPhoneNumberType());
            callDetails.setNumberLocation(n.getPhoneNumberLocation());
            callDetails.setHidden(false);
            /*currently assuming the next call user recives/dials is in very short time
            like lesser time than he moves into / out from roaming area so adding missing logs accordingly
            remember this missing logs are found very rarely*/
            if (!isResyncTask) {
                TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                callDetails.setRoaming(manager.isNetworkRoaming());
            }
            dbHelper.addToLogsHistory(callDetails, true);
        }
        managedCursor.close();
        return null;
    }
}
