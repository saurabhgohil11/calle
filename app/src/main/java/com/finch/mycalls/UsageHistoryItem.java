package com.finch.mycalls;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by Saurabh on 21-03-2015.
 */
public class UsageHistoryItem implements Parcelable{
    long startDate;
    long endDate;
    long localSecs;
    long stdSecs;
    long roamingOutgoingSecs;
    long roamingIncoimgSecs;
    long ISDSecs; //future expansion

    public UsageHistoryItem() {
    }

    public UsageHistoryItem(Parcel source) {
        this.startDate = source.readLong();;
        this.endDate = source.readLong();
        this.localSecs = source.readLong();
        this.stdSecs = source.readLong();
        this.roamingOutgoingSecs = source.readLong();
        this.roamingIncoimgSecs = source.readLong();
        this.ISDSecs = source.readLong();
    }

    public UsageHistoryItem(long startDate, long endDate, long localSecs, long stdSecs, long roamingOutgoingSecs, long roamingIncoimgSecs) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.localSecs = localSecs;
        this.stdSecs = stdSecs;
        this.roamingOutgoingSecs = roamingOutgoingSecs;
        this.roamingIncoimgSecs = roamingIncoimgSecs;
    }


    public long getISDSecs() {
        return ISDSecs;
    }

    public void setISDSecs(long ISDSecs) {
        this.ISDSecs = ISDSecs;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getLocalSecs() {
        return localSecs;
    }

    public void setLocalSecs(long localSecs) {
        this.localSecs = localSecs;
    }

    public long getStdSecs() {
        return stdSecs;
    }

    public void setStdSecs(long stdSecs) {
        this.stdSecs = stdSecs;
    }

    public long getRoamingOutgoingSecs() {
        return roamingOutgoingSecs;
    }

    public void setRoamingOutgoingSecs(long roamingOutgoingSecs) {
        this.roamingOutgoingSecs = roamingOutgoingSecs;
    }

    public long getRoamingIncoimgSecs() {
        return roamingIncoimgSecs;
    }

    public void setRoamingIncoimgSecs(long roamingIncoimgSecs) {
        this.roamingIncoimgSecs = roamingIncoimgSecs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.v(AppGlobals.LOG_TAG, this.getClass().getSimpleName() + ": writeToParcel..." + flags);
        dest.writeLong(startDate);
        dest.writeLong(endDate);
        dest.writeLong(localSecs);
        dest.writeLong(stdSecs);
        dest.writeLong(roamingIncoimgSecs);
        dest.writeLong(roamingOutgoingSecs);
        dest.writeLong(ISDSecs);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UsageHistoryItem createFromParcel(Parcel in) {
            return new UsageHistoryItem(in);
        }

        public UsageHistoryItem[] newArray(int size) {
            return new UsageHistoryItem[size];
        }
    };

    @Override
    public String toString() {
        return new String("startDate="+startDate+", endDate="+endDate+", localSecs="+localSecs+", stdSecs="+stdSecs
                +", roamingIncoimgSecs="+roamingIncoimgSecs+", roamingOutgoingSecs="+roamingOutgoingSecs+", ISDSecs="+ISDSecs);
    }
}
