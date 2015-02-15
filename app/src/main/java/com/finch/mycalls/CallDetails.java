package com.finch.mycalls;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class CallDetails implements Parcelable{
    public long callID;
    public String number;
    public String calltype;
    public int duration;
    public static final String CALL_TYPE_ROAMING= "Roaming";
    public static final String CALL_TYPE_LOCAL = "Local";
    public static final String CALL_TYPE_STD = "STD";
    public static final String CALL_TYPE_ISD = "ISD";

    public static final String CALL_TYPE_ROAMING_IC= "Roaming Incoming";
    public static final String CALL_TYPE_LOCAL_OG = "Local Outgoing";
    public static final String CALL_TYPE_STD_OG = "STD Outgoing";
    public static final String CALL_TYPE_ROAMING_OG = "Roaming Outgoing";


    public CallDetails(long callID,String number,String calltype, int duration) {
        this.callID=callID;
        this.number=number;
        this.calltype = calltype;
        this.duration = duration;
    }

    public CallDetails(Parcel source){
        this.callID=source.readLong();
        this.number=source.readString();
        this.calltype = source.readString();
        this.duration = source.readInt();
    }

    public CallDetails() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.v(AppGlobals.LOG_TAG,this.getClass().getSimpleName()+ ": writeToParcel..." + flags);
        dest.writeLong(callID);
        dest.writeString(number);
        dest.writeString(calltype);
        dest.writeInt(duration);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CallDetails createFromParcel(Parcel in) {
            return new CallDetails(in);
        }

        public CallDetails[] newArray(int size) {
            return new CallDetails[size];
        }
    };
}
