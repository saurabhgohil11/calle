package com.finch.calle;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class CallDetails implements Parcelable{
    public long callID;
    public String phoneNumber;
    public String nationalNumber;  //for grouping purpose
    public String cachedContactName;
    public CallType callType; //incoming or outgoing
    public boolean isRoaming;
    public PhoneNumberUtil.PhoneNumberType phoneNumberType;  //fixed line or mobile
    public String numberLocation;  //geo location of the phonenumber
    public CostType costType; // std,isd,exceptional
    public int duration;  //duration in seconds
    public long date;
    public boolean isHidden;

    public CallDetails() {}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.v(AppGlobals.LOG_TAG,this.getClass().getSimpleName()+ ": writeToParcel..." + flags);
        dest.writeLong(callID);
        dest.writeString(phoneNumber);
        dest.writeString(cachedContactName);
        dest.writeSerializable(callType);
        dest.writeByte((byte) (isRoaming ? 1 : 0));
        dest.writeSerializable(phoneNumberType);
        dest.writeString(numberLocation);
        dest.writeSerializable(costType);
        dest.writeInt(duration);
        dest.writeLong(date);
    }

    public CallDetails(Parcel source) {
        this.callID=source.readLong();
        this.phoneNumber =source.readString();
        this.cachedContactName = source.readString();
        this.callType = (CallType) source.readSerializable();
        this.isRoaming = source.readByte() != 0;
        this.phoneNumberType = (PhoneNumberUtil.PhoneNumberType) source.readSerializable();
        this.numberLocation = source.readString();
        this.costType = (CostType) source.readSerializable();
        this.duration = source.readInt();
        this.date = source.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CallDetails createFromParcel(Parcel in) {
            return new CallDetails(in);
        }

        public CallDetails[] newArray(int size) {
            return new CallDetails[size];
        }
    };

    public long getCallID() {
        return callID;
    }

    public void setCallID(long callID) {
        this.callID = callID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getNationalNumber() {
        return nationalNumber;
    }

    public void setNationalNumber(String nationalNumber) {
        this.nationalNumber = nationalNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCachedContactName() {
        return cachedContactName;
    }

    public void setCachedContactName(String cachedContactName) {
        this.cachedContactName = cachedContactName;
    }

    public CallType getCallType() {
        return callType;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public boolean isRoaming() {
        return isRoaming;
    }

    public void setRoaming(boolean isRoaming) {
        this.isRoaming = isRoaming;
    }

    public PhoneNumberUtil.PhoneNumberType getPhoneNumberType() {
        return phoneNumberType;
    }

    public void setPhoneNumberType(PhoneNumberUtil.PhoneNumberType phoneNumberType) {
        this.phoneNumberType = phoneNumberType;
    }

    public String getNumberLocation() {
        return numberLocation;
    }

    public void setNumberLocation(String numberLocation) {
        this.numberLocation = numberLocation;
    }

    public CostType getCostType() {
        return costType;
    }

    public String getCostTypeString() {
        Resources res = AppGlobals.mContext.getResources();
        if(res == null) {
            AppGlobals.log(this,"getCostTypeString: resources null");
            return "";
        }
        switch (costType) {
            case UNKNOWN:
                return res.getString(R.string.unknown);
            case FREE:
                return res.getString(R.string.free);
            case ISD:
                return res.getString(R.string.isd);
            case LOCAL:
                return res.getString(R.string.local);
            case STD:
                return res.getString(R.string.std);
            default:
                return "";
        }
    }

    public void setCostType(CostType costType) {
        this.costType = costType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public String getCostTypeToDisplay() {
        StringBuffer sb = new StringBuffer();
        Resources res = AppGlobals.mContext.getResources();
        if(res == null) {
            AppGlobals.log(this,"getCostTypeToDisplay: resources null");
            return "";
        }
        if(isRoaming){
            sb.append(res.getString(R.string.roaming));
        } else if (costType == CostType.LOCAL) {
            sb.append(res.getString(R.string.local));
        } else if (costType == CostType.STD) {
            sb.append(res.getString(R.string.std));
        } else if (costType == CostType.ISD) {
            sb.append(res.getString(R.string.isd));
        } else if (costType == CostType.UNKNOWN) {
            sb.append(res.getString(R.string.unknown));
        } else if(costType == CostType.FREE) {
            sb.append(res.getString(R.string.free));
        }
        sb.append(" ");
        if(callType == CallType.INCOMING) {
            sb.append(res.getString(R.string.incoming));
        } else if (callType == CallType.OUTGOING) {
            sb.append(res.getString(R.string.outgoing));
        }

        return sb.toString();
    }

    public String getPhoneNumberTypeToDisplay() {
        Resources res = AppGlobals.mContext.getResources();
        if(res == null) {
            AppGlobals.log(this,"getPhoneNumberTypeToDisplay: resources null");
            return "";
        }
        switch (phoneNumberType) {
            case MOBILE:
                return res.getString(R.string.mobile);
            case FIXED_LINE:
                return res.getString(R.string.landline);
            case FIXED_LINE_OR_MOBILE:
                return res.getString(R.string.mobile_or_landline);
            case PAGER:
                return res.getString(R.string.pager);
            case PERSONAL_NUMBER:
                return res.getString(R.string.personal_number);
            case TOLL_FREE:
                return res.getString(R.string.toll_free);
            case UNKNOWN:
                return res.getString(R.string.unknown);
            case VOICEMAIL:
                return res.getString(R.string.voicemail);
            default:
                return res.getString(R.string.unknown);
        }
    }

    public String getNumberRegionToDisplay() {
        if(numberLocation == null || numberLocation.isEmpty()) {
            return AppGlobals.mContext.getString(R.string.unknown);
        }
        return numberLocation;
    }

    @Override
    public String toString() {
        return "["+callID+","+phoneNumber+","+nationalNumber+","+cachedContactName+","+callType.toString()+","+isRoaming+","+phoneNumberType.toString()+
                ","+ numberLocation +","+costType+","+duration+","+date+","+isHidden;
    }
}
