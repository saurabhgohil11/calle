package com.finch.mycalls;


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

public class CallStateReceiver extends BroadcastReceiver {
    private static final int UPDATE_LOGS_DB = 101;
    public static Context mContext;
    static boolean isOutgoing=false;
    static boolean isIncoming=false;
    static String curState="lol";
    static String phoneNumber;
    //static int state,prevState;

    final String TAG2 = "CallStateReceiver : ";

    SharedPreferences vsp;//sahared pref for variable
    SharedPreferences.Editor ve;
    DataBaseHelper dbHelper;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case UPDATE_LOGS_DB:
                    CallDetails lastCallDetails = retriveCallSummary();
                    if(lastCallDetails==null){
                        Log.e(AppGlobals.LOG_TAG, TAG2 +"lastCallDetail is null");
                    }else {
                        TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                        if(isIncoming && !manager.isNetworkRoaming()) break;
                        if(manager.isNetworkRoaming() ){
                            addToRoaming(lastCallDetails);
                        }else {
                            processNumber(lastCallDetails);
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
        dbHelper = new DataBaseHelper(mContext);
        vsp = mContext.getSharedPreferences("CallStateReceive",Context.MODE_PRIVATE);
        ve=vsp.edit();
        AppGlobals.log(this, "onReceive()");

        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            isOutgoing=true;
            AppGlobals.log(this, "isOGtrue");
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
        AppGlobals.log(this, "inOnCallStateChanged()");
        if(vsp.getInt("prevstate",-2)==vsp.getInt("state",-1)){
            AppGlobals.log(this, "retrun from onCallStateChanged due to same states");
            return;
        }
        switch(state){
            case TelephonyManager.CALL_STATE_RINGING:
                AppGlobals.log(this, "state=" + curState);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                AppGlobals.log(this, "state=" + curState);
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                AppGlobals.log(this, "state=" + curState);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_LOGS_DB),3000);  //send message after it is updated in DB
                break;
        }
        ve.putInt("prevstate",vsp.getInt("state",-3));
        ve.commit();
    }

    private void addToRoaming(CallDetails lastCallDetails) {
        AppGlobals.log(this, "addToToaming()");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(isIncoming){
            int used_ric = sp.getInt(AppGlobals.PKEY_USED_ROAMING_IC_MINS,-1);
            if(used_ric!=-1){
                SharedPreferences.Editor e = sp.edit();
                e.putInt(AppGlobals.PKEY_USED_ROAMING_IC_MINS,used_ric+lastCallDetails.duration);
                e.commit();
            }
        }else if(isOutgoing){
            int used_rog = sp.getInt(AppGlobals.PKEY_USED_ROAMING_OG_MINS,-1);
            if(used_rog!=-1){
                SharedPreferences.Editor e = sp.edit();
                e.putInt(AppGlobals.PKEY_USED_ROAMING_OG_MINS,used_rog+lastCallDetails.duration);
                e.commit();
            }
        }
        lastCallDetails.calltype = CallDetails.CALL_TYPE_ROAMING +" "+ lastCallDetails.calltype;
        dbHelper.addToRecentCalls(lastCallDetails.number,lastCallDetails.calltype,lastCallDetails.duration);
    }

    private void processNumber(CallDetails lastCallDetails) {
        AppGlobals.log(this, "processNumber()");
        if(lastCallDetails.calltype.equals("Incoming")){
            AppGlobals.log(this, "Incoming calls are free dost");
            return;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        PhoneNumber n = new PhoneNumber(mContext,dbHelper,lastCallDetails.number);
        switch (n.getType()){
            case PhoneNumber.TYPE_LOCAL:
                int used_local = sp.getInt(AppGlobals.PKEY_USED_LOCAL_MINS,-1);
                if(used_local!=-1){
                    SharedPreferences.Editor e = sp.edit();
                    e.putInt(AppGlobals.PKEY_USED_LOCAL_MINS,used_local+lastCallDetails.duration);
                    e.commit();
                }
                lastCallDetails.calltype = CallDetails.CALL_TYPE_LOCAL +" "+ lastCallDetails.calltype;
                dbHelper.addToRecentCalls(lastCallDetails.number,lastCallDetails.calltype,lastCallDetails.duration);
                break;
            case PhoneNumber.TYPE_STD:
                int used_std = sp.getInt(AppGlobals.PKEY_USED_STD_MINS,-1);
                if(used_std!=-1){
                    SharedPreferences.Editor e = sp.edit();
                    e.putInt(AppGlobals.PKEY_USED_STD_MINS,used_std+lastCallDetails.duration);
                    e.commit();
                }
                lastCallDetails.calltype = CallDetails.CALL_TYPE_STD +" "+ lastCallDetails.calltype;
                dbHelper.addToRecentCalls(lastCallDetails.number,lastCallDetails.calltype,lastCallDetails.duration);
                break;
            case PhoneNumber.TYPE_EXCEPTIONAL:
                //do not add entry as they are free
                break;
            case PhoneNumber.TYPE_ISD:
                // currently we do not keep ISD call track
                break;
            case PhoneNumber.TYPE_UNKNOWN:
                //Change of UI
                //istead of showing dialog after each unknown call, just ask to review those numbers
                //on next startup of app
                //TODO:*****create a new logic for saving minutes : fetch directly from database save date in database too. ****
                //showUnknownNumberDialog(lastCallDetails);

                break;
        }
    }

    CallDetails retriveCallSummary() {
        AppGlobals.log(this, "retriveCallSummary()");
        CallDetails c =new CallDetails();
        Uri contacts = CallLog.Calls.CONTENT_URI;
        Cursor managedCursor = CallStateReceiver.mContext.getContentResolver().query(contacts, null, null, null, null);
        int numberid = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int typeid = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        //int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int durationid = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        if (managedCursor.moveToFirst()) {

            String callDuration = managedCursor.getString(durationid);
            int duration = Integer.parseInt(callDuration);
            if(duration==0){
                Log.e(AppGlobals.LOG_TAG, TAG2 +"duration is null");
                return null;
            }
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            int mode = Integer.parseInt(sp.getString(AppGlobals.PKEY_MODE_OF_CALCULATION, String.valueOf(AppGlobals.MODE_UNKNOWN)));
            switch (mode){
                case AppGlobals.MODE_MINUTES:
                    c.duration = (duration/60+1)*60; //in seconds
                    break;
                case AppGlobals.MODE_SECONDS:
                    c.duration = duration;
                    break;
                case AppGlobals.MODE_UNKNOWN:
                    Log.e(AppGlobals.LOG_TAG, TAG2 +"time mode is unknown");
                    return null;
            }

            c.number = managedCursor.getString(numberid);

            String callType = managedCursor.getString(typeid);
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    c.calltype = "Outgoing";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    c.calltype = "Incoming";
                    break;
/*
                case CallLog.Calls.MISSED_TYPE:
                    c.calltype = "Missed";
                    break;*/
            }
            //String callDate = managedCursor.getString(date);
            //String callDayTime = new Date(Long.valueOf(callDate)).toString();
            // long timestamp = convertDateToTimestamp(callDayTime);
        }
        managedCursor.close();
        AppGlobals.log(this, "---" + c.number + ", " + c.calltype + ", " + String.valueOf(c.duration));
        return c;
    }
}