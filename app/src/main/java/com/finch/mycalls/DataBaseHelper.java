package com.finch.mycalls;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class DataBaseHelper extends SQLiteOpenHelper {

    private final Context mContext;

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "postpaidapp";

    //table names
    private static final String TABLE_MOBILE_STATES = "MOBILE_STATES";
    private static final String TABLE_LOCAL_NUMBERS = "LOCAL_NUMBERS";
    private static final String TABLE_EXCLUDED_NUMBERS = "EXCLUDED_NUMBERS";
    private static final String TABLE_STD_NUMBERS = "STD_NUMBERS";
    private static final String TABLE_CALL_USAGE_HISTORY = "CALL_USAGE_HISTORY";
    private static final String TABLE_LOGS_HISTORY = "LOGS_HISTORY";

    private static final String KEY_STATE = "state";

    //usage history
    private static final String KEY_START_DATE = "START_DATE";
    private static final String KEY_END_DATE = "END_DATE";
    private static final String KEY_LOCAL_MIN = "LOCAL_MIN";
    private static final String KEY_STD_MIN = "STD_MIN";
    private static final String KEY_STD_END = "STD_END";
    private static final String KEY_LOCAL_END = "LOCAL_END";
    private static final String KEY_ROAMING_IC = "ROAMING_IC";
    private static final String KEY_ROAMING_OG = "ROAMING_OG";
    private static final String KEY_ROAMING_OG_END = "ROAMING_OG_END";

    private static final String KEY_MOBILE_NUMBER = "MOBILE_NUMBER";

    private static final String KEY_CALL_TYPE = "CALL_TYPE";
    private static final String KEY_CALL_DURATION = "CALL_DURATION";
    private static final String KEY_CALL_ID = "CALL_ID"; //for primary key purpose
    private static final String KEY_DATE = "DATE";

    String userCountryCode="";

    SQLiteDatabase db;


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        db = getWritableDatabase();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        userCountryCode = sp.getString(AppGlobals.PKEY_COUNTRY_CODE,"");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        AppGlobals.log(this,"onCreate()");
        String CREATE_MOBILE_STATE_TABLE = "CREATE TABLE " + TABLE_MOBILE_STATES + "(" +
                        KEY_STATE + " TEXT" + ")";

        String CREATE_CALL_USAGE_HISTORY_TABLE = "CREATE TABLE "+TABLE_CALL_USAGE_HISTORY+" ("+
                        KEY_START_DATE+" TEXT," +
                        KEY_END_DATE+" TEXT,"+
                        KEY_LOCAL_MIN+"	INTEGER,"+
                        KEY_LOCAL_END+" INTEGER,"+
                        KEY_STD_MIN+" INTEGER,"+
                        KEY_STD_END+" INTEGER,"+
                        KEY_ROAMING_IC+" INTEGER,"+
                        KEY_ROAMING_OG+" INTEGER,"+
                        KEY_ROAMING_OG_END+" INTEGER"+
                        ")";

        String CREATE_EXCLUDED_NUMBERS_TABLE = "CREATE TABLE "+TABLE_EXCLUDED_NUMBERS+" ("+
                        KEY_MOBILE_NUMBER+" TEXT PRIMARY KEY)";

        String CREATE_STD_NUMBERS_TABLE = "CREATE TABLE "+TABLE_STD_NUMBERS+" ("+
                        KEY_MOBILE_NUMBER+" TEXT PRIMARY KEY)";

        String CREATE_LOCAL_NUMBERS_TABLE = "CREATE TABLE "+ TABLE_LOCAL_NUMBERS+" ("+
                        KEY_MOBILE_NUMBER+" TEXT PRIMARY KEY)";

        String CREATE_LOGS_HISTORY_TABLE = "CREATE TABLE "+ TABLE_LOGS_HISTORY +" ("+
                        KEY_CALL_ID+" INTEGER PRIMARY KEY, " +
                        KEY_MOBILE_NUMBER+" TEXT, " +
                        KEY_CALL_TYPE+" TEXT, " +
                        KEY_CALL_DURATION+" INTEGER, "+
                        KEY_DATE+" DATETIME DEFAULT CURRENT_TIMESTAMP )";  // default date is saved which means when entry is created that is log time

        db.execSQL(CREATE_MOBILE_STATE_TABLE);
        db.execSQL(CREATE_CALL_USAGE_HISTORY_TABLE);
        db.execSQL(CREATE_EXCLUDED_NUMBERS_TABLE);
        db.execSQL(CREATE_STD_NUMBERS_TABLE);
        db.execSQL(CREATE_LOCAL_NUMBERS_TABLE);
        db.execSQL(CREATE_LOGS_HISTORY_TABLE);

        AppGlobals.log(this,"tableS created");
        initStateTable(db);
    }

    private void initStateTable(SQLiteDatabase db) {
        Resources res = mContext.getResources();
        String[] states = res.getStringArray(R.array.states);

        String sql="INSERT INTO "+TABLE_MOBILE_STATES+" VALUES(?)";
        for(int i=0;i<states.length;i++) {
            db.execSQL(sql,new String[]{states[i]});
        }
        AppGlobals.log(this,"DB Created successfully with "+states.length +" entries");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AppGlobals.log(this,"onUpGrade()");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOBILE_STATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALL_USAGE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXCLUDED_NUMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STD_NUMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCAL_NUMBERS);

        onCreate(db);
    }

    public String getState(String phoneNumber){
        String state=null;
        if(phoneNumber.length()<10) return state;
        if(phoneNumber==null) return state;
        if(phoneNumber.startsWith("0") || phoneNumber.startsWith(userCountryCode)) {
            if(phoneNumber.startsWith("0")) {
                phoneNumber = phoneNumber.substring(1);
            }
            if(phoneNumber.startsWith(userCountryCode)) {
                phoneNumber = phoneNumber.substring(userCountryCode.length());
            }
        }
        else if(phoneNumber.startsWith("+")) return null; //ISD call

        //SQLiteDatabase db = this.getReadableDatabase();

        Integer trimmedNum = Integer.parseInt(phoneNumber.substring(0,4)); //first 4 digits of number
        trimmedNum-=6999;
        String selectQuery = "SELECT * FROM "+TABLE_MOBILE_STATES+" WHERE ROWID=? ";
        Cursor c = db.rawQuery(selectQuery, new String[] { trimmedNum.toString() });
        if (c.moveToFirst()) {
            state = c.getString(0);
        }
        //db.close();
        AppGlobals.log(this,"trimmed Number = "+trimmedNum+ ", state = "+ state);
        Toast.makeText(mContext,"number= "+phoneNumber+", trimmed Number = "+trimmedNum+ ", state = "+ state,Toast.LENGTH_LONG).show();
        return state;
    }

    public boolean isExceptional(String number) {
        String trimmedNumber=number;
        if(number.startsWith("0") || number.startsWith(userCountryCode)) {
            if(number.startsWith("0")) {
                trimmedNumber = number.substring(1);
            }
            if(number.startsWith(userCountryCode)) {
                trimmedNumber = number.substring(userCountryCode.length());
            }
        }
        //SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+TABLE_EXCLUDED_NUMBERS+" WHERE "+KEY_MOBILE_NUMBER+" IN(?,?,?) ";
        Cursor c = db.rawQuery(selectQuery, new String[] { trimmedNumber,"0"+trimmedNumber,userCountryCode+trimmedNumber });
        if(c.getCount()>0) {
            //db.close();
            return true;
        }else {
            //db.close();
            return false;
        }
    }

    public boolean isSTD(String number) {
        String trimmedNumber=number;
        String numberState=null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userState = sp.getString(AppGlobals.PKEY_USER_CIRCLE,null);
        if(number.startsWith("0") || number.startsWith(userCountryCode)) {
            if(number.startsWith("0")) {
                trimmedNumber = number.substring(1);
            }
            if(number.startsWith(userCountryCode)) {
                trimmedNumber = number.substring(userCountryCode.length());
            }
        }
        //SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+TABLE_STD_NUMBERS+" WHERE "+KEY_MOBILE_NUMBER+" IN(?,?,?) ";
        Cursor c = db.rawQuery(selectQuery, new String[] { trimmedNumber,"0"+trimmedNumber,userCountryCode+trimmedNumber });
        if(c.getCount()>0) {
            //db.close();
            return true;
        }else if((numberState=getState(number))!=null && userState!=null && !userState.equals(numberState)){
            //db.close();
            return true;
        }else {
            //db.close();
            return false;
        }
    }

    public boolean isLocal(String number) {
        String trimmedNumber=number;
        String numberState=null;

        if(number.startsWith("0") || number.startsWith(userCountryCode)) {
            if(number.startsWith("0")) {
                trimmedNumber = number.substring(1);
            }
            if(number.startsWith(userCountryCode)) {
                trimmedNumber = number.substring(userCountryCode.length());
            }
        }else if(number.length()<=10 && (number.startsWith("9")||number.startsWith("8")||number.startsWith("7"))){ //call without prefix std code so it is local as call duration is non zero
            AppGlobals.log(this,"call without prefix std code  & it's start with 9|8|7 so it is local as call duration is non zero");
            return true;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userState = sp.getString(AppGlobals.PKEY_USER_CIRCLE,null);
        //AppGlobals.log(this,"number = "+number+",trimmed Local:" + trimmedNumber+"userCountryCode="+userCountryCode);
        //SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+TABLE_LOCAL_NUMBERS+" WHERE "+KEY_MOBILE_NUMBER+" IN(?,?,?) ";
        Cursor c = db.rawQuery(selectQuery, new String[] { trimmedNumber,"0"+trimmedNumber,userCountryCode+trimmedNumber });
        if(c.getCount()>0) {
            //db.close();
            return true;
        }
        else if((numberState=getState(number))!=null && userState!=null && userState.equals(numberState)) {
            //db.close();
            return true;
        }else {
            //db.close();
            return false;
        }
    }

    public void addcycle(){

    }

    public ArrayList<String> getLocalNumbers() {
        String selectQuery = "SELECT * FROM "+TABLE_LOCAL_NUMBERS;
        Cursor c = db.rawQuery(selectQuery,null);
        ArrayList<String> list=new ArrayList<String>(c.getCount());
        if(c.getCount()>0){
            if (c.moveToFirst()) {
                do {
                    list.add(c.getString(0));
                } while (c.moveToNext());
            }
        }
        //db.close();
        return list;
    }

    public ArrayList<String> getExcludedNumbers(){
        //SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+TABLE_EXCLUDED_NUMBERS;
        Cursor c = db.rawQuery(selectQuery,null);
        ArrayList<String> list=new ArrayList<String>(c.getCount());
        if(c.getCount()>0){
            if (c.moveToFirst()) {
                do {
                    list.add(c.getString(0));
                } while (c.moveToNext());
            }
        }
        //db.close();
        return list;
    }

    public ArrayList<String> getSTDNumbers(){
        //SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM "+TABLE_STD_NUMBERS;
        Cursor c = db.rawQuery(selectQuery,null);
        ArrayList<String> list = new ArrayList<String>(c.getCount());
        if(c.getCount()>0){
            if (c.moveToFirst()) {
                do {
                    list.add(c.getString(0));
                } while (c.moveToNext());
            }
        }
        //db.close();
        return list;
    }


    public boolean addLocalNumber(String number){
        //SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MOBILE_NUMBER, number);
        db.insert(TABLE_LOCAL_NUMBERS, null, values);
        //database.close();
        return true;
    }


    public boolean addSTDNumber(String number){
        //SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MOBILE_NUMBER, number);
        db.insert(TABLE_STD_NUMBERS, null, values);
        //database.close();
        return true;
    }

    public boolean addExcludedNumber(String number){
        //SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MOBILE_NUMBER, number);
        db.insert(TABLE_EXCLUDED_NUMBERS, null, values);
        //database.close();
        return true;
    }

    public void finalize() throws Throwable{
        if(null != db)
            db.close();
        super.finalize();
    }

    public void deleteNumberFromSTD(String number) {
        db.delete(TABLE_STD_NUMBERS, KEY_MOBILE_NUMBER + " = ?", new String[]{number});
    }

    public void deleteNumberFromLocal(String number) {
        db.delete(TABLE_LOCAL_NUMBERS, KEY_MOBILE_NUMBER + " = ?", new String[]{number});
    }

    public void deleteNumberFromExcluded(String number) {
        db.delete(TABLE_EXCLUDED_NUMBERS, KEY_MOBILE_NUMBER + " = ?", new String[]{number});
    }

    public void addToLogsHistory(String number, String calltype, int duration){

        /*deleted block for new logic
        String selectQuery = "SELECT * FROM "+ TABLE_LOGS_HISTORY;
        Cursor c = db.rawQuery(selectQuery,null);
        if(c.getCount()==5000){ //maximum size of recet call list 5000
            int callID;
            if (c.moveToFirst()) {
                callID=c.getInt(0);
                db.delete(TABLE_LOGS_HISTORY, KEY_CALL_ID + " = ?",new String[]{String.valueOf(callID)});
            }
        }*/
        ContentValues cv = new ContentValues();
        cv.put(KEY_MOBILE_NUMBER,number);
        cv.put(KEY_CALL_DURATION,duration);
        cv.put(KEY_CALL_TYPE,calltype);
        db.insert(TABLE_LOGS_HISTORY, null, cv);
        Toast.makeText(mContext,"Added "+duration/60+" Mins to "+calltype,Toast.LENGTH_LONG).show();
    }

    public ArrayList<CallDetails> getLogsHistory(){
        String selectQuery = "SELECT * FROM "+ TABLE_LOGS_HISTORY;
        Cursor c = db.rawQuery(selectQuery,null);
        ArrayList<CallDetails> list = new ArrayList<CallDetails>();
        if(c.moveToFirst()){
            do{
                list.add(new CallDetails(c.getInt(0),c.getString(1),c.getString(2),c.getInt(3),c.getLong(4)));
            }while(c.moveToNext());
        }
        Collections.reverse(list);
        return list;
    }

    public CallDetails getLastCall(){
        String selectQuery = "SELECT * FROM "+ TABLE_LOGS_HISTORY;
        Cursor c = db.rawQuery(selectQuery,null);
        if(c.getCount()>0 && c.moveToLast()){
            return new CallDetails(c.getInt(0),c.getString(1),c.getString(2),c.getInt(3));
        }else{
            return null;
        }
    }
    public void deleteLastCall(){
        String selectQuery = "SELECT * FROM "+ TABLE_LOGS_HISTORY;
        Cursor c = db.rawQuery(selectQuery,null);
        if(c.getCount()>0 && c.moveToLast()){
            int callID = c.getInt(0);
            db.delete(TABLE_LOGS_HISTORY, KEY_CALL_ID + " = ?",new String[]{String.valueOf(callID)});
        }
    }

    public void deleteNumberRecentCalls(long callID) {
        db.delete(TABLE_LOGS_HISTORY, KEY_CALL_ID + " = ?",new String[]{String.valueOf(callID)});
    }
}