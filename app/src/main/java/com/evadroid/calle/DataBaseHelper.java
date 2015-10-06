package com.evadroid.calle;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Date;

public class DataBaseHelper extends SQLiteOpenHelper {

    private final Context mContext;

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "calleapp";

    //table names
    private static final String TABLE_MOBILE_STATES = "MOBILE_STATES";
    private static final String TABLE_LOGS_HISTORY = "LOGS_HISTORY";
    private static final String TABLE_USER_SPECIFIED_NUMBERS = "USER_SPECIFIED_NUMBERS";

    private static final String KEY_STATE = "state";

    private static final String KEY_PHONE_NUMBER = "PHONE_NUMBER";
    private static final String KEY_NATIONAL_NUMBER = "NATIONAL_NUMBER";
    private static final String KEY_CALL_TYPE = "CALL_TYPE";
    private static final String KEY_CALL_DURATION = "CALL_DURATION";
    private static final String KEY_CALL_ID = "CALL_ID"; //for primary key purpose
    private static final String KEY_COST_TYPE = "COST_TYPE";
    private static final String KEY_IS_ROAMING = "IS_ROAMING";
    private static final String KEY_PHONE_NUMBER_TYPE = "PHONE_NUMBER_TYPE";
    private static final String KEY_CACHED_CONTACT_NAME = "CACHED_CONTACT_NAME";
    private static final String KEY_DATE = "DATE";
    private static final String KEY_IS_HIDDEN = "IS_HIDDEN";
    private static final String KEY_GEO_LOCATION = "GEO_LOCATION";

    SQLiteDatabase db;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        db = getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        AppGlobals.log(this,"onCreate()");
        String CREATE_MOBILE_STATE_TABLE = "CREATE TABLE " + TABLE_MOBILE_STATES + "(" +
                        KEY_STATE + " TEXT" + ")";

        String CREATE_LOGS_HISTORY_TABLE = "CREATE TABLE "+ TABLE_LOGS_HISTORY +" ("+
                        KEY_CALL_ID+" INTEGER PRIMARY KEY, " +
                        KEY_CACHED_CONTACT_NAME+" TEXT, "+
                        KEY_PHONE_NUMBER +" TEXT, " +
                        KEY_NATIONAL_NUMBER +" TEXT, " +
                        KEY_CALL_TYPE+" INTEGER, " +
                        KEY_COST_TYPE+" INTEGER, " +
                        KEY_IS_ROAMING+" BOOL, " +
                        KEY_PHONE_NUMBER_TYPE+" INTEGER, "+
                        KEY_CALL_DURATION+" INTEGER, "+
                        KEY_DATE+" DATETIME, "+
                        KEY_IS_HIDDEN+" BOOL, "+
                        KEY_GEO_LOCATION+" TEXT )";  // default date is saved which means when entry is created that is log time

        String CREATE_USER_SPECIFIED_NUMBERS_TABLE = "CREATE TABLE "+TABLE_USER_SPECIFIED_NUMBERS+" ("+
                KEY_PHONE_NUMBER +" TEXT PRIMARY KEY, "+
                KEY_COST_TYPE + " INTEGER )";

        db.execSQL(CREATE_MOBILE_STATE_TABLE);
        db.execSQL(CREATE_LOGS_HISTORY_TABLE);
        db.execSQL(CREATE_USER_SPECIFIED_NUMBERS_TABLE);

        AppGlobals.log(this, "tableS created");
        initStateTable(db);
    }

    private void initStateTable(SQLiteDatabase db) {
        Resources res = mContext.getResources();
        String[] states = res.getStringArray(R.array.states);

        String sql="INSERT INTO "+TABLE_MOBILE_STATES+" VALUES(?)";
        for(int i=0;i<states.length;i++) {
            db.execSQL(sql,new String[]{states[i]});
        }
        AppGlobals.log(this, "DB Created successfully with " + states.length + " entries");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AppGlobals.log(this, "onUpGrade()");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOBILE_STATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_SPECIFIED_NUMBERS);
        onCreate(db);
    }

    public String getMobileNumberState (long phoneNumber) {
        String state=null;
        while (phoneNumber > 9999) {
            // while "more than 4 digits", "throw out last digit"
            phoneNumber /= 10;
        }
        phoneNumber-=6999;
        String selectQuery = "SELECT * FROM "+TABLE_MOBILE_STATES+" WHERE ROWID=? ";
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(phoneNumber)});
        if (c.moveToFirst()) {
            state = c.getString(0);
        }
        //db.close();
        if(AppGlobals.showLogs)
            AppGlobals.log(this,"trimmed Number = "+phoneNumber+ ", state = "+ state);
        c.close();
        return state;
    }

    public void addUserSpecifiedNumber(String number,CostType costType){
        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_NUMBER, number);
        values.put(KEY_COST_TYPE,costType.ordinal());
        String existingNumber = isUserSpecifiedNumberExists(number);
        if(AppGlobals.showLogs)
            AppGlobals.log(this, "addUserSpecifiedNumber existingNumber"+existingNumber);
        if(existingNumber!=null){
            ContentValues newValues = new ContentValues();
            newValues.put(KEY_PHONE_NUMBER, number);
            newValues.put(KEY_COST_TYPE, costType.ordinal());
            db.update(TABLE_USER_SPECIFIED_NUMBERS, newValues, KEY_PHONE_NUMBER+"='"+existingNumber+"'", null);
        } else {
            db.insert(TABLE_USER_SPECIFIED_NUMBERS, null, values);
        }
        updateLogsHistory(number, costType);
    }

    private void updateLogsHistory(String numberStr, CostType costType) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            phoneNumber = phoneUtil.parse(numberStr, AppGlobals.userCountryCode);
        } catch (NumberParseException e) {
            e.printStackTrace();
            return;
        }

        String nationalNumber = String.valueOf(phoneNumber.getNationalNumber());
        String withCountryCode = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_COST_TYPE, costType.ordinal());
        db.update(TABLE_LOGS_HISTORY, newValues, KEY_PHONE_NUMBER + " IN(?,?,?)", new String[]{nationalNumber, "0" + nationalNumber, withCountryCode});
        AppGlobals.sendUpdateMessage();
    }

    public void deleteUserSpecifiedNumber(String phoneNumber) {
        db.delete(TABLE_USER_SPECIFIED_NUMBERS, KEY_PHONE_NUMBER + " = ?", new String[]{phoneNumber});
        PhoneNumber n = new PhoneNumber(mContext,this,phoneNumber);
        updateLogsHistory(phoneNumber, n.getCostType());
    }

    public ArrayList<String> getUserSpecifiedNumbers(CostType costType) {
        String selectQuery = "SELECT * FROM "+TABLE_USER_SPECIFIED_NUMBERS+
                             " WHERE "+KEY_COST_TYPE+" = "+costType.ordinal();
        Cursor c = db.rawQuery(selectQuery, null);
        ArrayList<String> list=new ArrayList<>(c.getCount());
        if(c.getCount()>0){
            if (c.moveToFirst()) {
                do {
                    list.add(c.getString(0));
                } while (c.moveToNext());
            }
        }
        c.close();
        return list;
    }

    public CostType getUserSpecifiedNumberType(String numberStr) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            phoneNumber = phoneUtil.parse(numberStr, AppGlobals.userCountryCode);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        String nationalNumber = String.valueOf(phoneNumber.getNationalNumber());
        String withCountryCode = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);

        String selectQuery = "SELECT * FROM "+TABLE_USER_SPECIFIED_NUMBERS+" WHERE "+ KEY_PHONE_NUMBER +" IN(?,?,?) ";
        Cursor c = db.rawQuery(selectQuery, new String[] { nationalNumber,"0"+nationalNumber,withCountryCode });
        if(c.getCount()>0) {
            c.moveToFirst();
            CostType costType = CostType.values()[c.getInt(1)];
            c.close();
            return costType;
        } else {
            c.close();
            return null;
        }
    }

    public String isUserSpecifiedNumberExists(String numberStr){  //returns in which format usernumber exists

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber;
        try {
            phoneNumber = phoneUtil.parse(numberStr, AppGlobals.userCountryCode);
        } catch (NumberParseException e) {
            e.printStackTrace();
            return null;
        }
        //Log.d("DBHelper",""+phoneNumber.getNationalNumber());
        String nationalNumber = String.valueOf(phoneNumber.getNationalNumber());
        String withCountryCode = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);

        String selectQuery = "SELECT * FROM "+TABLE_USER_SPECIFIED_NUMBERS+" WHERE "+ KEY_PHONE_NUMBER +" IN(?,?,?) ";
        Cursor c = db.rawQuery(selectQuery, new String[] { nationalNumber,"0"+nationalNumber,withCountryCode });
        if(c.getCount()>0) {
            c.moveToFirst();
            String number = c.getString(0);
            c.close();
            return number;
        } else {
            c.close();
            return null;
        }
    }


    public void addToLogsHistory(CallDetails callDetails) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_PHONE_NUMBER,callDetails.getPhoneNumber());
        cv.put(KEY_NATIONAL_NUMBER,callDetails.getNationalNumber());
        cv.put(KEY_PHONE_NUMBER_TYPE,callDetails.getPhoneNumberType().ordinal());
        cv.put(KEY_CALL_DURATION,callDetails.getDuration());
        cv.put(KEY_COST_TYPE,callDetails.getCostType().ordinal());
        cv.put(KEY_CALL_TYPE,callDetails.getCallType().ordinal());
        cv.put(KEY_IS_ROAMING,callDetails.isRoaming());
        cv.put(KEY_CACHED_CONTACT_NAME, callDetails.getCachedContactName());
        cv.put(KEY_DATE,callDetails.getDate());
        cv.put(KEY_IS_HIDDEN,callDetails.isHidden());
        cv.put(KEY_GEO_LOCATION,callDetails.getNumberLocation());
        db.insert(TABLE_LOGS_HISTORY, null, cv);
        AppGlobals.sendUpdateMessage();
    }

    //retrives logs for HomeActivity
    public ArrayList<CallDetails> getLogsHistory(){
        String selectQuery = "SELECT * FROM "+ TABLE_LOGS_HISTORY + " WHERE " + KEY_IS_HIDDEN + "=0"+" ORDER BY "+KEY_DATE+" DESC";
        Cursor c = db.rawQuery(selectQuery,null);
        ArrayList<CallDetails> list = new ArrayList<>();
        if(c.moveToFirst()){
            do {
                CallDetails cd = new CallDetails();
                cd.setCallID(c.getInt(0));
                cd.setCachedContactName(c.getString(1));
                cd.setPhoneNumber(c.getString(2));
                cd.setNationalNumber(c.getString(3));
                cd.setCallType(CallType.values()[c.getInt(4)]);
                cd.setCostType(CostType.values()[c.getInt(5)]);
                cd.setRoaming(c.getInt(6) == 1);
                cd.setPhoneNumberType(PhoneNumberUtil.PhoneNumberType.values()[c.getInt(7)]);
                cd.setDuration(c.getInt(8));
                cd.setDate(c.getLong(9));
                cd.setHidden(c.getInt(10)==1);
                cd.setNumberLocation(c.getString(11));
                list.add(cd);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public ArrayList<CallDetails> getUnknownLogsHistory() {
        String selectQuery = "SELECT DISTINCT "+KEY_NATIONAL_NUMBER+","+ KEY_PHONE_NUMBER+","+ KEY_CACHED_CONTACT_NAME+" FROM "+TABLE_LOGS_HISTORY+
                             " WHERE "+ KEY_COST_TYPE +" = "+CostType.UNKNOWN.ordinal();
        Cursor c = db.rawQuery(selectQuery,null);

        ArrayList<CallDetails> list = new ArrayList<>();
        if(c.moveToFirst()) {
            do {
                CallDetails cd = new CallDetails();
                cd.setNationalNumber(c.getString(0));
                cd.setPhoneNumber(c.getString(1));
                cd.setCachedContactName(c.getString(2));
                list.add(cd);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean isDuplicatewithLastLog(CallDetails c) {
        //To-Do :modified method to checke from DB is there any other exact similar log
        CallDetails lastCallLog = getLastCall();
        boolean isEqual = lastCallLog.date == c.date && lastCallLog.nationalNumber.equals(c.nationalNumber);
        if (AppGlobals.showLogs)
            AppGlobals.log(this,"isDuplicatewithLastLog:\n1."+c+"\n2."+lastCallLog+"\nisEqual"+isEqual);
        return isEqual;
    }

    public CallDetails getLastCall(){
        String selectQuery = "SELECT * FROM "+ TABLE_LOGS_HISTORY + " WHERE " + KEY_IS_HIDDEN + "=0" +" ORDER BY "+KEY_DATE+" DESC";
        Cursor c = db.rawQuery(selectQuery,null);
        if (c.getCount()>0 && c.moveToFirst()) {
            CallDetails cd = new CallDetails();
            cd.setCallID(c.getInt(0));
            cd.setCachedContactName(c.getString(1));
            cd.setPhoneNumber(c.getString(2));
            cd.setNationalNumber(c.getString(3));
            cd.setCallType(CallType.values()[c.getInt(4)]);
            cd.setCostType(CostType.values()[c.getInt(5)]);
            cd.setRoaming(c.getInt(6) == 1);
            cd.setPhoneNumberType(PhoneNumberUtil.PhoneNumberType.values()[c.getInt(7)]);
            cd.setDuration(c.getInt(8));
            cd.setDate(c.getLong(9));
            cd.setHidden(c.getInt(10)==1);
            cd.setNumberLocation(c.getString(11));
            c.close();
            return cd;
        } else {
            c.close();
            return null;
        }
    }

    public int deleteNumberFromLogs(long callID) {
        return db.delete(TABLE_LOGS_HISTORY, KEY_CALL_ID + " = ?", new String[]{String.valueOf(callID)});
    }

    public CostType getMobileCostType(Phonenumber.PhoneNumber phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        boolean isValid = phoneUtil.isValidNumber(phoneNumber);

        if(!isValid) {
            AppGlobals.log(this, "getMobileCostType() phoneNumber is not valid:" + phoneNumber.getRawInput());
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userState = sp.getString(AppGlobals.PKEY_USER_CIRCLE, null);
        String numberState = getMobileNumberState(phoneNumber.getNationalNumber());
        if(numberState == null || userState == null || numberState.isEmpty() || userState.isEmpty()) {
            return CostType.UNKNOWN;
        } else if (userState.equals(numberState)) {
            return CostType.LOCAL;
        }else {
            return CostType.STD;
        }
    }

    public int getTotalSeconds(long startDate,long endDate,CallType callType,CostType costType) { //if costtype is null give total
        boolean isMinuteMode = AppGlobals.isMinuteMode;

        StringBuffer query = new StringBuffer();
        /*if(isMinuteMode) {
            query.append("SELECT sum("+KEY_CALL_DURATION+"/60+1) FROM "+TABLE_LOGS_HISTORY);
        } else {
            query.append("SELECT sum(" + KEY_CALL_DURATION + ")/60+1 FROM " + TABLE_LOGS_HISTORY);
        }*/

        query.append("SELECT "+KEY_CALL_DURATION+" FROM "+TABLE_LOGS_HISTORY);

        query.append(" WHERE "+KEY_DATE+" BETWEEN "
                +startDate+" AND "+endDate+" AND "+KEY_CALL_TYPE+"="+callType.ordinal());

        if(costType == null) { //total except free for outgoing
            if(callType == CallType.OUTGOING)
                query.append(" AND " + KEY_COST_TYPE + "<>" + CostType.FREE.ordinal());
        } else if(costType == CostType.ROAMING){
            query.append(" AND "+KEY_IS_ROAMING+"="+1);
        } else {
            query.append(" AND "+KEY_COST_TYPE+"="+costType.ordinal()+" AND "+KEY_IS_ROAMING+"="+0);
        }

        Cursor c = db.rawQuery(query.toString(), null);
        if(c.getCount()>0) {
            c.moveToFirst();
            if(isMinuteMode) {
                int minutes=0;
                do {
                    minutes += c.getInt(0)/60;
                    if(c.getInt(0)%60!=0) minutes++;
                }while (c.moveToNext());
                c.close();
                return minutes*60;
            } else {
                int seconds=0;
                do {
                    seconds += c.getInt(0);
                }while (c.moveToNext());
                c.close();
                return seconds;
                /*if(seconds%60==0)
                    return seconds/60;
                else
                    return seconds/60+1;*/
            }
        } else {
            c.close();
            return 0;
        }
    }

    //retrives log list for LogListActivity
    public ArrayList<CallDetails>  getLogList(long startDate,long endDate,CallType callType,CostType costType) { //if costtype is null give total
        boolean isMinuteMode = AppGlobals.isMinuteMode;

        StringBuffer query = new StringBuffer();
        if(isMinuteMode) {
            query.append("SELECT * FROM "+TABLE_LOGS_HISTORY);
        } else {
            query.append("SELECT * FROM "+TABLE_LOGS_HISTORY);
        }

        query.append(" WHERE "+ KEY_IS_HIDDEN + "=0 AND "+KEY_DATE+" BETWEEN "
                +startDate+" AND "+endDate+" AND "+KEY_CALL_TYPE+"="+callType.ordinal());

        if(costType == null) { //total except free for outgoing
            if(callType == CallType.OUTGOING)
                query.append(" AND " + KEY_COST_TYPE + "<>" + CostType.FREE.ordinal());
        } else if(costType == CostType.ROAMING){
            query.append(" AND "+KEY_IS_ROAMING+"="+1);
        } else {
            query.append(" AND "+KEY_COST_TYPE+"="+costType.ordinal()+" AND "+KEY_IS_ROAMING+"="+0);
        }
        query.append(" ORDER BY "+KEY_DATE+" DESC");

        Cursor c = db.rawQuery(query.toString(),null);
        ArrayList<CallDetails> list = new ArrayList<>();
        if(c.moveToFirst()){
            do {
                CallDetails cd = new CallDetails();
                cd.setCallID(c.getInt(0));
                cd.setCachedContactName(c.getString(1));
                cd.setPhoneNumber(c.getString(2));
                cd.setNationalNumber(c.getString(3));
                cd.setCallType(CallType.values()[c.getInt(4)]);
                cd.setCostType(CostType.values()[c.getInt(5)]);
                cd.setRoaming(c.getInt(6) == 1);
                cd.setPhoneNumberType(PhoneNumberUtil.PhoneNumberType.values()[c.getInt(7)]);
                cd.setDuration(c.getInt(8));
                cd.setDate(c.getLong(9));
                cd.setHidden(c.getInt(10)==1);
                cd.setNumberLocation(c.getString(11));
                list.add(cd);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }


    public int getTotalSeconds(long startDate,long endDate,CallType callType,CostType costType,PhoneNumberUtil.PhoneNumberType phoneNumberType) {
        boolean isMinuteMode = AppGlobals.isMinuteMode;

        StringBuffer query = new StringBuffer();
        /*if(isMinuteMode) {
            query.append("SELECT sum("+KEY_CALL_DURATION+"/60+1) FROM "+TABLE_LOGS_HISTORY+" WHERE "+KEY_DATE+" BETWEEN "
                    +startDate+" AND "+endDate+" AND "+KEY_IS_ROAMING+"=0 AND "+KEY_COST_TYPE+"="+costType.ordinal()+
                    " AND "+KEY_CALL_TYPE+"="+callType.ordinal());

        } else {
            query.append("SELECT sum("+KEY_CALL_DURATION+")/60+1 FROM "+TABLE_LOGS_HISTORY+" WHERE "+KEY_DATE+" BETWEEN "
                    +startDate+" AND "+endDate+" AND "+KEY_IS_ROAMING+"=0 AND "+KEY_COST_TYPE+"="+costType.ordinal()+
                    " AND "+KEY_CALL_TYPE+"="+callType.ordinal());
        }*/
        query.append("SELECT "+KEY_CALL_DURATION+" FROM "+TABLE_LOGS_HISTORY+" WHERE "+KEY_DATE+" BETWEEN "
                +startDate+" AND "+endDate+" AND "+KEY_IS_ROAMING+"=0 AND "+KEY_COST_TYPE+"="+costType.ordinal()+
                " AND "+KEY_CALL_TYPE+"="+callType.ordinal());

        if(phoneNumberType == null) { //get other not mobile not fixed
            query.append(" AND " + KEY_PHONE_NUMBER_TYPE + "<>" + PhoneNumberUtil.PhoneNumberType.MOBILE.ordinal() +
                    " AND " + KEY_PHONE_NUMBER_TYPE + "<>" + PhoneNumberUtil.PhoneNumberType.FIXED_LINE.ordinal());

        } else { //for mobile or fixed line
            query.append(" AND "+KEY_PHONE_NUMBER_TYPE+"="+phoneNumberType.ordinal());
        }

        Cursor c = db.rawQuery(query.toString(), null);
        if(c.getCount()>0) {
            c.moveToFirst();
            if(isMinuteMode) {
                int minutes=0;
                do {
                    minutes += c.getInt(0)/60;
                    if(c.getInt(0)%60!=0) minutes++;
                }while (c.moveToNext());
                c.close();
                return minutes*60;
            } else {
                int seconds=0;
                do {
                    seconds += c.getInt(0);
                }while (c.moveToNext());
                c.close();
                return seconds;
                /*if(seconds%60==0)
                    return seconds/60;
                else
                    return seconds/60+1;*/
            }
        } else {
            c.close();
            return 0;
        }
    }

    public long getOldestLogDate() {
        String query = "SELECT "+KEY_DATE+" FROM "+TABLE_LOGS_HISTORY+" ORDER BY "+KEY_DATE+" LIMIT 1";
        Cursor c = db.rawQuery(query, null);
        if(c.getCount()>0) {
            c.moveToFirst();
            long date = c.getLong(0);
            c.close();
            return date;
        } else {
            c.close();
            return -1;
        }
    }

    public ArrayList<UsageDetail> getUsageHistoryList() {
        ArrayList<UsageDetail> list = new ArrayList<>();
        ArrayList<Date []> cycleList = AppGlobals.getUsageCycleArray();
        for(Date dates[] :cycleList) {
            UsageDetail u = new UsageDetail();
            u.cycleDates = dates;
            u.outgoingSeconds = getTotalSeconds(dates[0].getTime(),dates[1].getTime(),CallType.OUTGOING,null);
            u.incomingSeconds = getTotalSeconds(dates[0].getTime(),dates[1].getTime(),CallType.INCOMING,null);
            list.add(u);
        }
        return list;
    }

    public void updateLogsOnCircleChange() {
        String selectQuery = "SELECT DISTINCT "+ KEY_PHONE_NUMBER+" FROM "+TABLE_LOGS_HISTORY;
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            do {
                String phoneNumber = c.getString(0);
                PhoneNumber n = new PhoneNumber(mContext,this,phoneNumber);
                updateLogsHistory(phoneNumber,n.getCostType());
            } while (c.moveToNext());
        }
        c.close();
    }

    public void finalize() throws Throwable{
        if(null != db)
            db.close();
        super.finalize();
    }


}