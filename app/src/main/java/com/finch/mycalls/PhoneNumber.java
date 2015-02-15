package com.finch.mycalls;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PhoneNumber {
    public static final int TYPE_LOCAL = 10;
    public static final int TYPE_STD = 11;
    public static final int TYPE_EXCEPTIONAL = 12;
    public static final int TYPE_ISD = 13;
    public static final int TYPE_UNKNOWN = 14;

    DataBaseHelper mdbHelper;

    int type;
    String number;

    PhoneNumber(Context c,DataBaseHelper dbHelper,String number){
        this.number=number;
        type=TYPE_UNKNOWN;
        this.mdbHelper=dbHelper;


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        String userCountryCode = sp.getString(AppGlobals.PKEY_COUNTRY_CODE,"");

        if(dbHelper.isExceptional(number)) {
            type=TYPE_EXCEPTIONAL;
            AppGlobals.log(this, "type is Exceptional");
        }else if(dbHelper.isLocal(number)) {
            type=TYPE_LOCAL;
            AppGlobals.log(this, "type is Local");
        }else if(dbHelper.isSTD(number)) {
            type=TYPE_STD;
            AppGlobals.log(this, "type is STD");
        }else if(number.startsWith("+")) {
            if(!number.startsWith(userCountryCode))
                type=TYPE_ISD;
            AppGlobals.log(this, "type is ISD");
        }else {
            //unkown number ask user to add it or not
            type=TYPE_UNKNOWN;
            AppGlobals.log(this, "type is Unknown");
        }
    }

    public int getType(){
        return type;
    }

    @Override
    public String toString() {
        return number;
    }
}
