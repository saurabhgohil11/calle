package com.evadroid.calle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AppGlobals {

    public static final boolean showLogs = false;
    public static final boolean showDevOptions = false;  //don't forget to change permission

    public static volatile AppGlobals instance;

    public static Context mContext;
    private static DataBaseHelper dbHelper;
    public static String userState = "";
    public static boolean isDualSim;
    public static int userCountryCodeNumber = -1;
    public static String userCountryCodeNumberString = "null";
    public static String userCountryCode = "null";
    public static String simOperator = "null";
    public static boolean isMinuteMode = true;

    public static final String LOG_TAG = "Call-E";

    public static final String PKEY_BILL_CYCLE="bill_cycle";
    public static final String PKEY_USER_CIRCLE="user_circle";
    public static final String PKEY_INSTALLATION_DATE="installation_date";
    public static final String PKEY_MODE_OF_CALCULATION="mode_of_calcualation";
    public static final String PKEY_FIRST_TIME = "first_time";
    public static final String PKEY_CUG_DIALOG_SHOWN = "cug_dialog_shown";
    public static final String PKEY_COUNTRY_CODE_NUMBER = "country_code_number";
    public static final String PKEY_COUNTRY_CODE = "country_code";
    public static final String MODE_MINUTES = "5000";
    public static final String MODE_SECONDS = "5001";

    private static SharedPreferences preferences;

    public static int funkyColors [] ={ R.color.funky_blue, R.color.funky_green, R.color.funky_grey,
                                        R.color.funky_orange, R.color.funky_pink, R.color.funky_purple,
                                        R.color.funky_red,R.color.funky_violet};


    public static int bgColoredImages []= {
            R.drawable.contact_image_bg_blue, R.drawable.contact_image_bg_green, R.drawable.contact_image_bg_grey,
            R. drawable.contact_image_bg_orange, R.drawable.contact_image_bg_pink, R.drawable.contact_image_bg_purple,
            R.drawable.contact_image_bg_red, R.drawable.contact_image_bg_violet};

    public static HashMap<String,String> circleNameMap = new HashMap<>();
    public static HashMap<String,String[]> includedRegionsMap = new HashMap<>();
    public static HashMap<String,String[]> excludedRegionsMap = new HashMap<>();


    public static AppGlobals getInstance(Context c) {
        if(instance == null) {
            instance = new AppGlobals(c);
        }
        mContext = c; //update context when ever your homeactivity is relaunched else the context will be dead and
                        //we can't create new dialogs with dead activity context
        if(dbHelper != null)
            dbHelper = new DataBaseHelper(c);
        return instance;
    }

    public static AppGlobals getInstance() {
        if(instance == null) {
            if(mContext == null) {
                Log.d(LOG_TAG,"AppGlobals : mContext null here");
            }
            instance = new AppGlobals(mContext);
        }
        return instance;
    }

    public static DataBaseHelper getDataBaseHelper(Context c) {
        if(dbHelper != null) {
            return dbHelper;
        }
        dbHelper = new DataBaseHelper(c);
        return dbHelper;
    }

    public static DataBaseHelper getDataBaseHelper() {
        if(dbHelper != null) {
            return dbHelper;
        }
        if(mContext != null) {
            dbHelper = new DataBaseHelper(mContext);
            return dbHelper;
        } else {
            Log.d(LOG_TAG, "AppGlobals :  mContext is null and dbhHelper is null");
            return dbHelper;
        }
    }

    AppGlobals(Context context){
        mContext = context;
        dbHelper = new DataBaseHelper(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        //initialize userstate,dualsim from shared pref

        isDualSim=false;
        isMinuteMode = AppGlobals.MODE_MINUTES.equals(preferences.getString(AppGlobals.PKEY_MODE_OF_CALCULATION, AppGlobals.MODE_MINUTES));

        initUserCountryCode();
        initMaps();

        log(this, userState + ", " + userCountryCodeNumber + ", " + simOperator + ", " + userCountryCode);
    }

    public void initUserCountryCode() {
        SharedPreferences.Editor e = preferences.edit();
        AppGlobals.userCountryCode = preferences.getString(AppGlobals.PKEY_COUNTRY_CODE, "XX");
        if(AppGlobals.userCountryCode.equals("XX")) {
            try {
                TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                userCountryCode = manager.getSimCountryIso().toUpperCase();
                if(userCountryCode == null || userCountryCode.isEmpty()) {
                    userCountryCode = "IN";
                    Toast.makeText(mContext, "Error retrieving Network info setting Country to India", Toast.LENGTH_SHORT).show();
                }
                simOperator = manager.getSimOperatorName().toUpperCase();
                e.putString(AppGlobals.PKEY_COUNTRY_CODE, AppGlobals.userCountryCode);
                if (mContext != null) {
                    String[] rl = mContext.getResources().getStringArray(R.array.CountryCodes);
                    for (int i = 0; i < rl.length; i++) {
                        String[] g = rl[i].split(",");
                        if (g[1].trim().equals(AppGlobals.userCountryCode.trim())) {
                            AppGlobals.userCountryCodeNumber = Integer.parseInt(g[0]);
                            AppGlobals.userCountryCodeNumberString = g[0];
                            break;
                        }
                    }
                    e.putInt(AppGlobals.PKEY_COUNTRY_CODE_NUMBER, AppGlobals.userCountryCodeNumber);
                } else {
                    Log.d(LOG_TAG, "AppGlobals :  mContext is null");
                }
            } catch (Exception ex) {
                AppGlobals.userCountryCodeNumber = 91;
                AppGlobals.userCountryCodeNumberString = "IN";
                Toast.makeText(mContext, "Error retrieving Network info setting Country to India", Toast.LENGTH_SHORT).show();
            }
            e.commit();
        } else {
            userCountryCodeNumber = preferences.getInt(AppGlobals.PKEY_COUNTRY_CODE_NUMBER, -1);
            userCountryCodeNumberString = String.valueOf(userCountryCodeNumber);
        }
        userState = preferences.getString("user_circle","NULL");
        TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        simOperator = manager.getSimOperatorName().toUpperCase();
    }

    public static void initMaps() {
        Resources res = mContext.getResources();
        final String[] operator_states = res.getStringArray(R.array.circle_states_short_forms);
        final String[] operator_state_codes = res.getStringArray(R.array.circle_state_codes);
        final String[] included_list = res.getStringArray(R.array.included_regions);
        final String[] excluded_list = res.getStringArray(R.array.excluded_regions);

        for(int i=0;i<operator_states.length;i++){
            circleNameMap.put(operator_state_codes[i],operator_states[i]);
            String[] included = included_list[i].split(",");
            String[] excluded = excluded_list[i].split(",");
            if(included.length>0) {
                includedRegionsMap.put(operator_state_codes[i],included);
            }
            if(!excluded_list[i].isEmpty() && excluded.length>0) {
                excludedRegionsMap.put(operator_state_codes[i],excluded);
            }
        }
    }

    public static String getCurrentBillCycleString() {
        Date cycle[] = getCurrentBillCycleDates();
        Date startDate = cycle[0];
        Date endDate = cycle[1];
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM");
        return sdf.format(startDate) + " - " + sdf.format(endDate);
    }

    public static String getBillCycleString(Date cycleDates[]) {
        if(cycleDates[0] == null || cycleDates[1]==null)
            return "";
        Date startDate = cycleDates[0];
        Date endDate = cycleDates[1];
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM");
        return sdf.format(startDate) + " - " + sdf.format(endDate);
    }

    public static Date[] getCurrentBillCycleDates() {
        int startDay=preferences.getInt(PKEY_BILL_CYCLE, -1);
        Calendar c = Calendar.getInstance();
        if(c.get(Calendar.DATE)<startDay)
            c.add(Calendar.MONTH, -1);
        c.set(Calendar.DATE, startDay);
        c.set(Calendar.HOUR_OF_DAY,0);  //HOUR is stricly 12 Hours
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);

        Date dates[] = new Date[2];
        dates[0] = new Date(c.getTimeInMillis());

        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DATE, -1);
        c.set(Calendar.HOUR_OF_DAY, 23);  //HOUR is stricly 12 Hours
        c.set(Calendar.MINUTE,59);
        c.set(Calendar.SECOND,59);
        c.set(Calendar.MILLISECOND, 999);
        dates[1] = new Date(c.getTimeInMillis());
        return dates;
    }

    public static ArrayList<Date []> getUsageCycleArray() {
        ArrayList<Date []> cycles = new ArrayList<>();
        long installationDate  = preferences.getLong(PKEY_INSTALLATION_DATE, -1);
        long temp;
        if((temp=dbHelper.getOldestLogDate())!=-1) {
            installationDate = temp;
        }
        //installationDate = 1262344139000L;  //Fri, 01 Jan 2010 11:08:59 GMT
        if(installationDate == -1) {
            return cycles;
        }

        int startDay=preferences.getInt(PKEY_BILL_CYCLE, -1);
        Calendar c = Calendar.getInstance();
        boolean firstloop=true;
        //android.util.Log.d("AppGlobals"," installationdate:" +installationDate+"currentDate="+c.getTimeInMillis());
        while (c.getTimeInMillis()>installationDate) {
            if (c.get(Calendar.DATE)<startDay && firstloop) {
                c.add(Calendar.MONTH, -1);
                firstloop=false;
            }
            c.set(Calendar.DATE, startDay);
            c.set(Calendar.HOUR_OF_DAY, 0);  //HOUR is stricly 12 Hours
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            Date dates[] = new Date[2];
            dates[0] = new Date(c.getTimeInMillis());

            c.add(Calendar.MONTH, 1);
            c.add(Calendar.DATE, -1);
            c.set(Calendar.HOUR_OF_DAY, 23);  //HOUR is stricly 12 Hours
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 999);
            dates[1] = new Date(c.getTimeInMillis());
            cycles.add(dates);
            //Log.d("AppGlobals","cycle:"+getBillCycleString(dates));
            c.add(Calendar.MONTH, -1);
            if (c.get(Calendar.DATE)<startDay) {
                c.add(Calendar.MONTH, -1);
            }
        }

        if (c.get(Calendar.DATE)<startDay) { //add one more last cycle if list is more that 1
            c.set(Calendar.DATE, startDay);
            c.set(Calendar.HOUR_OF_DAY, 0);  //HOUR is stricly 12 Hours
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            Date dates[] = new Date[2];
            dates[0] = new Date(c.getTimeInMillis());

            c.add(Calendar.MONTH, 1);
            c.add(Calendar.DATE, -1);
            c.set(Calendar.HOUR_OF_DAY, 23);  //HOUR is stricly 12 Hours
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 999);
            dates[1] = new Date(c.getTimeInMillis());
            cycles.add(dates);
            //Log.d("AppGlobals", "cycle:" + getBillCycleString(dates));
        }

        return cycles;
    }

    public static boolean isTablet(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float yInches= metrics.heightPixels/metrics.ydpi;
        float xInches= metrics.widthPixels/metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
        if (diagonalInches < 6.5){
            return false;
        }
        return true;
    }

    public static void reset() {
        instance = null;
        mContext = null;
        dbHelper = null;
        userState = "";
        isDualSim = false;
        userCountryCodeNumber = -1;
        userCountryCodeNumberString = "null";
        userCountryCode = "null";
        simOperator = "null";
        isMinuteMode = true;
    }

    public static void log(Object o,String msg){
        Log.d(LOG_TAG,o.getClass().getSimpleName()+": "+msg);
    }

    public static void sendUpdateMessage() {
        if(HomeActivity.mHandler != null)
            HomeActivity.mHandler.sendEmptyMessage(HomeActivity.UPDATE_VIEWS);
        if(LogListActivity.mHandler != null)
            LogListActivity.mHandler.sendEmptyMessage(LogListActivity.UPDATE_VIEWS);
    }

    public static boolean isEnableToast(Context c) {
        if(mContext == null) {
            mContext = c;
        }
        if(mContext != null) {
            return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("enable_mins_toast", false);
        } else {
            Log.d(LOG_TAG,"AppGlobals : mContext null in isEnableToast");
            return false;
        }
    }

}
