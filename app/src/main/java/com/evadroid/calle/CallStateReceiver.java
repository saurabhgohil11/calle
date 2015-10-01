package com.evadroid.calle;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.evadroid.calle.utils.DateTimeUtils;

public class CallStateReceiver extends BroadcastReceiver {
    private static final int UPDATE_LOGS_DB = 10101;
    public static Context mContext;
    static boolean isOutgoing=false;
    static boolean isIncoming=false;
    static String curState="lol";
    static String phoneNumber;
    static int callCount; // to keep track of multiple call at a time. use preference to save same as call state
    //return calldetaillist in retive call summurry

    //static int state,prevState;

    /** TO DO : add callcount for multiple calls n++ onReceive retrive last n calls onStateIDLE and make n=0;*/

    final String TAG2 = "CallStateReceiver : ";

    SharedPreferences vsp;//sahared pref for variable
    SharedPreferences.Editor ve;
    DataBaseHelper dbHelper;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case UPDATE_LOGS_DB:
                    CallDetails lastCallDetails = retrieveCallSummary();
                    if(lastCallDetails==null) {
                        Log.e(AppGlobals.LOG_TAG, TAG2 +"lastCallDetail is null");
                    } else {
                        dbHelper.addToLogsHistory(lastCallDetails);
                        if(AppGlobals.isEnableToast) {
                            String minStr;
                            if(AppGlobals.isMinuteMode) {
                                minStr = DateTimeUtils.timeToRoundedString(lastCallDetails.getDuration());
                            } else {
                                minStr = DateTimeUtils.timeToString(lastCallDetails.getDuration());
                            }
                            String toastMsg = String.format(mContext.getResources().getString(R.string.added_toast),minStr)+ " "+ lastCallDetails.getCostTypeString()+" "+lastCallDetails.getCallType().toString().toLowerCase();
                            Toast.makeText(mContext, toastMsg, Toast.LENGTH_LONG).show();
                        }

                    }
                    break;
            }
        }
    };

    public CallStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext=context;
        dbHelper = AppGlobals.getInstance(context).getDataBaseHelper();  //init both appglobalinstance and dbhelper if they are null
        vsp = mContext.getSharedPreferences("CallStateReceive", Context.MODE_PRIVATE);
        ve=vsp.edit();
        //AppGlobals.log(this, "onReceive()");

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(!sp.getBoolean(AppGlobals.PKEY_FIRST_TIME,false)) {
            AppGlobals.log(this, "App Not started yet returning");
            return;
        }

        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            isOutgoing=true;
            //AppGlobals.log(this, "isOGtrue");
        }else{
            curState=intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (curState.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                ve.putInt("state",TelephonyManager.CALL_STATE_RINGING);
                ve.commit();
                //state = TelephonyManager.CALL_STATE_RINGING;
                isIncoming=true;
                phoneNumber=intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            }
            if(curState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                ve.putInt("state",TelephonyManager.CALL_STATE_OFFHOOK);
                ve.commit();
                //state = TelephonyManager.CALL_STATE_OFFHOOK;
            }
            if(curState.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                ve.putInt("state",TelephonyManager.CALL_STATE_IDLE);
                ve.commit();
                //state = TelephonyManager.CALL_STATE_IDLE;
            }

            try {
                onCallStateChanged(vsp.getInt("state",-1),phoneNumber);
                //onCallStateChanged(state,phoneNumber);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void onCallStateChanged(int state, String number) throws InterruptedException {
        AppGlobals.log(this, ""+curState);
        if(vsp.getInt("prevstate",-2)==vsp.getInt("state",-1)){
            //AppGlobals.log(this, "return from onCallStateChanged due to same states");
            return;
        }
        switch(state){
            case TelephonyManager.CALL_STATE_RINGING:
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_LOGS_DB),3000);  //send message after it is updated in DB
                break;
        }
        ve.putInt("prevstate",vsp.getInt("state",-3));
        ve.commit();
    }

    CallDetails retrieveCallSummary() {
        AppGlobals.log(this, "retrieveCallSummary()");
        CallDetails callDetails =new CallDetails();
        Uri contacts = CallLog.Calls.CONTENT_URI;
        Cursor managedCursor = CallStateReceiver.mContext.getContentResolver().query(contacts, null, null, null, null);
        int numberid = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int typeid = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int cachedNameid =  managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int durationid = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        if (managedCursor.moveToFirst()) {
            //do {
                int duration = managedCursor.getInt(durationid);
                //int duration = Integer.parseInt(callDuration);
                if (duration <= 0) {
                    Log.e(AppGlobals.LOG_TAG, TAG2 + "duration is null");
                    return null;
                }
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
                    /*
                    case CallLog.Calls.MISSED_TYPE:
                        c.callCostType = "Missed";
                        break;*/
                }

                TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                callDetails.isRoaming = manager.isNetworkRoaming();

                PhoneNumber n = new PhoneNumber(mContext,dbHelper,callDetails.phoneNumber);
                callDetails.costType = n.getCostType();
                callDetails.nationalNumber = n.getNationalNumber();
                callDetails.phoneNumberType = n.getPhoneNumberType();
                callDetails.numberLocation = n.getPhoneNumberLocation();
                callDetails.isHidden = false;
                //callCount --;
                managedCursor.moveToNext();
            //} while (callCount>0);
        }
        managedCursor.close();
        return callDetails;
    }
}