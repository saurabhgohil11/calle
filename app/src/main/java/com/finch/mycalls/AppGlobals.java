package com.finch.mycalls;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AppGlobals {
    public static Context mContext;
    public static DataBaseHelper dbHelper;
    public static String userState = "";
    public static boolean isDualSim;
    public String userCountryCode = "";
    public static String userCountry = "null";
    public static String simOperator = "";

    public static final String LOG_TAG = "PostPaidCallUsage";

    public static final String PKEY_BILL_CYCLE="bill_cycle";
    public static final String PKEY_TOTAL_LOCAL_MINS="total_local_mins";
    public static final String PKEY_TOTAL_STD_MINS="total_std_mins";
    public static final String PKEY_TOTAL_ROAMING_MINS="total_roaming_mins";
    public static final String PKEY_USED_LOCAL_MINS="used_local_mins";
    public static final String PKEY_USED_STD_MINS="used_std_mins";
    public static final String PKEY_USED_ROAMING_IC_MINS="used_roaming_ic_mins";
    public static final String PKEY_USED_ROAMING_OG_MINS="used_roaming_og_mins";
    public static final String PKEY_USER_CIRCLE="user_circle";
    public static final String PKEY_ENABLE_NOTIFICATION="enable_limit_notification";
    public static final String PKEY_MODE_OF_CALCULATION="mode_of_calcualation";
    public static final String PKEY_FIRST_TIME = "first_time";
    public static final String PKEY_COUNTRY_CODE = "country_code";
    public static final int MODE_MINUTES = 1000;
    public static final int MODE_SECONDS = 1001;
    public static final int MODE_UNKNOWN = 1002;

    private static String dates;
    private static SharedPreferences preferences;

    AppGlobals(Context context){
        mContext = context;
        dbHelper = new DataBaseHelper(context);

        //initialize userstate,dualsim from shared pref

        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        userState = preferences.getString("user_circle","NULL");
        log(this,userState);

        isDualSim=false;

        userCountryCode = preferences.getString(PKEY_COUNTRY_CODE,"null");
        try {
            TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            userCountry = manager.getSimCountryIso().toUpperCase();
            simOperator = manager.getSimOperatorName().toUpperCase();
        }catch (Exception e){
            Toast.makeText(mContext,"Error retrieving Network info.",Toast.LENGTH_SHORT).show();
        }
        log(this,userState+", "+userCountryCode+", "+simOperator+", "+userCountry);
    }

    public static void updateUserState(String Number){
        userState = dbHelper.getState(Number);
        if(userState == null || userState.isEmpty()){
            Toast.makeText(mContext,"Can't find your operator circle",Toast.LENGTH_LONG).show();
            showUserCircleSelectionDialog();
        }
    }

    public static void showUserCircleSelectionDialog(){
        Resources res = mContext.getResources();
        final String[] operator_states = res.getStringArray(R.array.circle_states);
        final String[] opertator_state_codes = res.getStringArray(R.array.circle_state_codes);

        AlertDialog.Builder builder2=new AlertDialog.Builder(mContext).setTitle("Select you Operator Circle")
                .setSingleChoiceItems(operator_states, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext,"The selected operator circle is "+operator_states[which], Toast.LENGTH_LONG).show();
                        userState = opertator_state_codes[which];
                        dialog.dismiss();
                    }
                });
        builder2.create().show();
    }

    public static String getCurrentBillCycle() {
        int startdate=preferences.getInt(PKEY_BILL_CYCLE,-1);

        //getting months
        Calendar c = Calendar.getInstance();
        int endmonth = c.get(Calendar.MONTH);  //current month
        int day = c.get(Calendar.DAY_OF_MONTH);
        int startmonth = endmonth;
        if(day<startdate){
            if(endmonth!=0) startmonth=endmonth-1;
            else startmonth=11;
        }else if(startdate!=1){
            if(endmonth!=11) endmonth++;
            else endmonth = 0;
        }
        c.set(Calendar.MONTH,startmonth);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM");
        simpleDateFormat.setCalendar(c);
        String startmonthName = simpleDateFormat.format(c.getTime());
        c.set(Calendar.MONTH,endmonth);
        String endmonthName = simpleDateFormat.format(c.getTime());

        int enddate=0;
        //getting dates
        if(startdate==1){
            enddate = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        }else if(startdate<29){
            enddate=startdate-1;
        }else{
            //*************YET TO BE IMPLEMENTED*************
        }
        dates = String.valueOf(startdate) + " " +startmonthName +" - "+String.valueOf(enddate) + " " +endmonthName;
        return dates;
    }

    public static StringBuffer durationToString(int duration){
        StringBuffer bf = new StringBuffer();

        return bf;
    }

    public static void log(Object o,String msg){
        Log.d(LOG_TAG,o.getClass().getSimpleName()+": "+msg);
    }
}
