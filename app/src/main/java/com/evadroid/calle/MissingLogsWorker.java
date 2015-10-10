package com.evadroid.calle;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.telephony.TelephonyManager;

/*class that adds missing logs if any logs are not added after the last log
  missing logs happens to be very rare.. this worker has very less work*/
public class MissingLogsWorker extends AsyncTask<Void, Integer, Void> {
    Context mContext;
    DataBaseHelper dbHelper;
    CallDetails lastCallLog;
    MissingLogsWorker(Context c){
        mContext = c;
        dbHelper = AppGlobals.getDataBaseHelper(c);
        lastCallLog = dbHelper.getLastCall();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (mContext == null) {
            AppGlobals.log(this,"mContext null Here");
            return null;
        }
        if (lastCallLog == null) {
            AppGlobals.log(this,"lastLog null Here");
            return null;
        }
        if(AppGlobals.showLogs)
            AppGlobals.log(this,"Adding Missing Logs");
        CallDetails callDetails = new CallDetails();
        Uri contacts = CallLog.Calls.CONTENT_URI;
        String whereClause = CallLog.Calls.DATE+">"+lastCallLog.date;
        String sortOrder = CallLog.Calls.DATE+ " ASC";
        Cursor managedCursor = mContext.getContentResolver().query(contacts, null, whereClause, null, sortOrder);
        int numberid = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int typeid = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int cachedNameid = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int durationid = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        AppGlobals.log(this," total logs to be read:"+managedCursor.getCount());


        while (managedCursor.moveToNext()) {
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

                default:  //calltype5 in samsung when reject incoming call
                    callDetails.callType = CallType.MISSED;
                    break;
            }
            PhoneNumber n = new PhoneNumber(mContext,dbHelper, callDetails.phoneNumber);
            callDetails.costType = n.getCostType();
            callDetails.nationalNumber = n.getNationalNumber();
            callDetails.phoneNumberType = n.getPhoneNumberType();
            callDetails.numberLocation = n.getPhoneNumberLocation();
            callDetails.isHidden = false;
            /*currently assuming the next call user recives/dials is in very short time
            like lesser time than he moves into / out from roaming area so adding missing logs accordingly
            remember this missing logs are found very rarely*/
            TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            callDetails.isRoaming = manager.isNetworkRoaming();
            dbHelper.addToLogsHistory(callDetails,true);
        }
        managedCursor.close();
        return null;
    }
}
