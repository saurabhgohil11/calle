package com.evadroid.calle;

import android.content.Context;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;

import java.util.Locale;

public class PhoneNumber {

    private boolean showLogs = false;

    DataBaseHelper mdbHelper;

    CostType costType;
    PhoneNumberUtil.PhoneNumberType phoneNumberType;
    Phonenumber.PhoneNumber phoneNumber;
    String phoneNumberLocation;
    String nationalNumber; //for ISD it is kept with ISD code other numbers are without country code

    public PhoneNumber(Context c, DataBaseHelper dbHelper, String numberStr){
        this.mdbHelper = dbHelper;
        costType = CostType.UNKNOWN;
        phoneNumberType = PhoneNumberUtil.PhoneNumberType.UNKNOWN;

        if (AppGlobals.userCountryCode.equals("null")) {
            AppGlobals.getInstance(c).initUserCountryCode();
        }
        //AppGlobals.log(this,"-----------------PhoneNumber is:"+numberStr);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            this.phoneNumber = phoneUtil.parse(numberStr, AppGlobals.userCountryCode);
        } catch (NumberParseException e) {
            AppGlobals.log(this,"NumberParseException was thrown: " + numberStr +e.toString());
            return;
        }

        if(phoneUtil.isValidNumber(phoneNumber)) {
            phoneNumberType = phoneUtil.getNumberType(phoneNumber);
            PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
            phoneNumberLocation = geocoder.getDescriptionForNumber(phoneNumber, Locale.ENGLISH);
        }

        costType = mdbHelper.getUserSpecifiedNumberType(String.valueOf(phoneNumber.getNationalNumber()));
        if (costType!=null) { // it is user specified no.
            nationalNumber = String.valueOf(phoneNumber.getNationalNumber());
            if (phoneNumberType == PhoneNumberUtil.PhoneNumberType.MOBILE) {
                String locationStateCode = mdbHelper.getMobileNumberState(phoneNumber.getNationalNumber());
                phoneNumberLocation = AppGlobals.circleNameMap.get(locationStateCode);
            }
            if(showLogs)
                AppGlobals.log(this, "costType is "+costType.toString());
        } else if (phoneNumber.getCountryCode() != AppGlobals.userCountryCodeNumber) {
            costType = CostType.ISD;
            nationalNumber = numberStr;
            if (showLogs)
                AppGlobals.log(this, "costType is ISD");
        } else {
            costType = CostType.UNKNOWN;
            nationalNumber = String.valueOf(phoneNumber.getNationalNumber());
            findCostType();
        }


    }

    private void findCostType() {

        switch (AppGlobals.userCountryCodeNumber) {
            case 91:
                if (phoneNumberType == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE) { //first check for mobile if not found check for landline
                    if(findCostToMobile()) {
                        phoneNumberType = PhoneNumberUtil.PhoneNumberType.MOBILE;
                    } else {
                        findCostToFixedLine();
                        phoneNumberType = PhoneNumberUtil.PhoneNumberType.FIXED_LINE;
                    }
                }else if (phoneNumberType == PhoneNumberUtil.PhoneNumberType.MOBILE) { //self categorization for mobiles
                    phoneNumberLocation = null;
                    findCostToMobile();
                } else if (phoneNumberType == PhoneNumberUtil.PhoneNumberType.FIXED_LINE) { // landlines
                    findCostToFixedLine();
                } else if (phoneNumberType == PhoneNumberUtil.PhoneNumberType.TOLL_FREE) {
                    costType = CostType.FREE;
                    if(showLogs)
                        AppGlobals.log(this, "costType is toll Free");
                }
                if(showLogs)
                    AppGlobals.log(this, "phoneNumberLocation is "+phoneNumberLocation);
                break;
            default:

        }
    }

    private void findCostToFixedLine() {
        if(phoneNumberLocation == null || phoneNumberLocation.isEmpty()) {
            costType = CostType.UNKNOWN;
            return;
        }
        String[] includedRegions,excludedRegions;
        includedRegions = AppGlobals.includedRegionsMap.get(AppGlobals.userState);
        excludedRegions = AppGlobals.excludedRegionsMap.get(AppGlobals.userState);
        String phoneNumberRegion = phoneNumberLocation.toLowerCase();

        boolean isLocal=false;
        for(String s:includedRegions){
            if(phoneNumberRegion.contains(s)){
                isLocal=true;
                break;
            }
        }
        if(isLocal && excludedRegions!=null){
            for(String s:excludedRegions){
                if(phoneNumberRegion.contains(s)){
                    isLocal=false;
                }
            }
        }
        if(isLocal){
            costType=CostType.LOCAL;
        }else{
            costType=CostType.STD;
        }
    }

    public String getPhoneNumberLocation() {
        return phoneNumberLocation;
    }

    private boolean findCostToMobile() {
        costType = mdbHelper.getMobileCostType(phoneNumber);
        if(costType==CostType.UNKNOWN) {
            return false;
        } else {
            String locationstateCode = mdbHelper.getMobileNumberState(phoneNumber.getNationalNumber());
            phoneNumberLocation = AppGlobals.circleNameMap.get(locationstateCode);
            return true;
        }
    }

    public CostType getCostType(){
        return costType;
    }

    public PhoneNumberUtil.PhoneNumberType getPhoneNumberType() {
        return phoneNumberType;
    }

    public Phonenumber.PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public String getNationalNumber() {
        return nationalNumber;
    }

    @Override
    public String toString() {
        return phoneNumber.toString();
    }
}
