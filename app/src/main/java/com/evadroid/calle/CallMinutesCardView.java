package com.evadroid.calle;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.evadroid.calle.utils.DateTimeUtils;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.Date;

public class CallMinutesCardView extends LinearLayout implements View.OnClickListener {

    private RelativeLayout mTitleLayout;
    private LinearLayout mLocalLayout;
    private LinearLayout mStdLayout;
    private LinearLayout mRoamingLayout;
    private LinearLayout mISDLayout;
    private LinearLayout mFreeLayout;
    private LinearLayout mUnknownLayout;
    private TableLayout mLocalSubMinutesLayout;
    private TableLayout mSTDSubMinutesLayout;

    private TextView mTitleLabel;

    private TextView mTitleMins;
    private TextView mLocalMins;
    private TextView mStdMins;
    private TextView mRoamingMins;
    private TextView mISDMins;
    private TextView mFreeMins;
    private TextView mUnknownMins;
    private TextView mLocalMobileMins;
    private TextView mLocalFixedLineMins;
    private TextView mLocalOtherMins;
    private TextView mSTDMobileMins;
    private TextView mSTDFixedLineMins;
    private TextView mSTDOtherMins;

    private TextView mFreeMinsNoteAdded;

    private Button mWarningLimitsButton;

    private Context mContext;
    private CallType mCallType;
    private Date cycleDates[];

    int totalSeconds, localSeconds, stdSeconds, roamingSeconds, isdSeconds, unknownSeconds, freeSeconds;

    public CallMinutesCardView(Context context) {
        this(context, null);
    }

    public CallMinutesCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.call_minutes_card, this, true);
        initView(rootView);
    }


    public void setCycleAndType(Date cycleDates[], CallType callType) {
        this.mCallType = callType;
        this.cycleDates = cycleDates;
    }

    private void initView(View rootView) {

        mTitleLayout = (RelativeLayout) rootView.findViewById(R.id.card_title_layout);
        mLocalLayout = (LinearLayout) rootView.findViewById(R.id.local_layout);
        mStdLayout = (LinearLayout) rootView.findViewById(R.id.std_layout);
        mRoamingLayout = (LinearLayout) rootView.findViewById(R.id.roaming_layout);
        mISDLayout = (LinearLayout) rootView.findViewById(R.id.isd_layout);
        mFreeLayout = (LinearLayout) rootView.findViewById(R.id.free_layout);
        mUnknownLayout = (LinearLayout) rootView.findViewById(R.id.unknown_layout);
        mLocalSubMinutesLayout = (TableLayout) rootView.findViewById(R.id.sub_local_min_layout);
        mSTDSubMinutesLayout = (TableLayout) rootView.findViewById(R.id.sub_std_min_layout);

        mTitleLabel = (TextView) rootView.findViewById(R.id.card_title);

        mTitleMins = (TextView) rootView.findViewById(R.id.card_title_mins);
        mLocalMins = (TextView) rootView.findViewById(R.id.local_total_mins);
        mStdMins = (TextView) rootView.findViewById(R.id.std_total_mins);
        mRoamingMins = (TextView) rootView.findViewById(R.id.roaming_total_mins);
        mISDMins = (TextView) rootView.findViewById(R.id.isd_total_mins);
        mFreeMins = (TextView) rootView.findViewById(R.id.free_total_mins);
        mUnknownMins = (TextView) rootView.findViewById(R.id.unknown_total_mins);
        mLocalMobileMins = (TextView) rootView.findViewById(R.id.local_mobile_mins);
        mLocalFixedLineMins = (TextView) rootView.findViewById(R.id.local_fixedline_mins);
        mLocalOtherMins = (TextView) rootView.findViewById(R.id.local_other_mins);
        mSTDMobileMins = (TextView) rootView.findViewById(R.id.std_mobile_mins);
        mSTDFixedLineMins = (TextView) rootView.findViewById(R.id.std_fixedline_mins);
        mSTDOtherMins = (TextView) rootView.findViewById(R.id.std_other_mins);

        mFreeMinsNoteAdded = (TextView) rootView.findViewById(R.id.free_mins_not_added_text);

        mWarningLimitsButton = (Button) rootView.findViewById(R.id.warning_limits_button);

        mTitleLayout.setOnClickListener(this);
        mLocalLayout.setOnClickListener(this);
        mStdLayout.setOnClickListener(this);
        mRoamingLayout.setOnClickListener(this);
        mISDLayout.setOnClickListener(this);
        mFreeLayout.setOnClickListener(this);
        mUnknownLayout.setOnClickListener(this);
        mWarningLimitsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetWarningLimitsDialog();
            }
        });
    }

    private void showSetWarningLimitsDialog() {
        mContext.startActivity(new Intent(mContext, WarningLimitsActivity.class));
    }

    public void updateCallMinutesCard() {
        if (mTitleLabel == null) {
            AppGlobals.log(this, "returning from updateCallMinutesCard");
            return;
        }
        if (mCallType == CallType.OUTGOING) {
            mTitleLabel.setText(R.string.outgoing);
            mTitleLabel.setTextColor(ContextCompat.getColor(mContext, R.color.funky_green));
            mTitleMins.setTextColor(ContextCompat.getColor(mContext, R.color.funky_green));
        } else {
            mTitleLabel.setText(R.string.incoming);
            mTitleLabel.setTextColor(ContextCompat.getColor(mContext, R.color.funky_orange));
            mTitleMins.setTextColor(ContextCompat.getColor(mContext, R.color.funky_orange));
        }

        DataBaseHelper dbHelper = AppGlobals.getDataBaseHelper(getContext());
        if (dbHelper == null) {
            AppGlobals.log(this, "returning from updateCallMinutesCard due to null dbHelper");
            return;
        }

        int tempSeconds;

        //total seconds
        totalSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, null);
        setText(mTitleMins, totalSeconds);

        //local Minutes
        localSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL);
        setVisibleAndSetText(mLocalLayout, mLocalMins, localSeconds);

        if (localSeconds > 0 && mCallType == CallType.OUTGOING) {
            mLocalSubMinutesLayout.setVisibility(View.VISIBLE);
        } else {
            mLocalSubMinutesLayout.setVisibility(View.GONE);
        }

        tempSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL, PhoneNumberUtil.PhoneNumberType.MOBILE);
        setText(mLocalMobileMins, tempSeconds);

        tempSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL, PhoneNumberUtil.PhoneNumberType.FIXED_LINE);
        setText(mLocalFixedLineMins, tempSeconds);

        tempSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL, null);
        setText(mLocalOtherMins, tempSeconds);

        //std seconds
        stdSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD);
        setVisibleAndSetText(mStdLayout, mStdMins, stdSeconds);

        if (stdSeconds > 0 && mCallType == CallType.OUTGOING) {
            mSTDSubMinutesLayout.setVisibility(View.VISIBLE);
        } else {
            mSTDSubMinutesLayout.setVisibility(View.GONE);
        }

        tempSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD, PhoneNumberUtil.PhoneNumberType.MOBILE);
        setText(mSTDMobileMins, tempSeconds);

        tempSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD, PhoneNumberUtil.PhoneNumberType.FIXED_LINE);
        setText(mSTDFixedLineMins, tempSeconds);

        tempSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD, null);
        setText(mSTDOtherMins, tempSeconds);

        //isd seconds
        isdSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.ISD);
        setVisibleAndSetText(mISDLayout, mISDMins, isdSeconds);

        //Unknown seconds
        unknownSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.UNKNOWN);
        setVisibleAndSetText(mUnknownLayout, mUnknownMins, unknownSeconds);

        //Roaming seconds
        roamingSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.ROAMING);
        setVisibleAndSetText(mRoamingLayout, mRoamingMins, roamingSeconds);

        //free seconds
        freeSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.FREE);
        setVisibleAndSetText(mFreeLayout, mFreeMins, freeSeconds);

        if (freeSeconds > 0 && mCallType == CallType.OUTGOING) {
            mFreeMinsNoteAdded.setVisibility(View.VISIBLE);
            mFreeMins.setTextColor(Color.GRAY);
        } else {
            mFreeMinsNoteAdded.setVisibility(View.GONE);
        }
    }

    private void setText(TextView minuteView, int seconds) {
        if (AppGlobals.isMinuteMode)
            minuteView.setText(DateTimeUtils.timeToRoundedString(seconds));
        else
            minuteView.setText(DateTimeUtils.timeToString(seconds));
    }

    private void setVisibleAndSetText(View layout, TextView minuteView, int seconds) {
        if (layout == null || minuteView == null) return;
        if (seconds > 0) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
        if (AppGlobals.isMinuteMode)
            minuteView.setText(DateTimeUtils.timeToRoundedString(seconds));
        else
            minuteView.setText(DateTimeUtils.timeToString(seconds));

    }

    public void showSetWarningLimitButton(boolean show) {
        if (show) {
            mWarningLimitsButton.setVisibility(VISIBLE);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean isSetLimit = sp.contains(AppGlobals.PKEY_STD_LIMIT) ||
                    sp.contains(AppGlobals.PKEY_LOCAL_LIMIT) ||
                    sp.contains(AppGlobals.PKEY_STD_LOCAL_LIMIT) ||
                    sp.contains(AppGlobals.PKEY_ROAMING_LIMIT) ||
                    sp.contains(AppGlobals.PKEY_ISD_LIMIT);
            if (isSetLimit)
                mWarningLimitsButton.setText(mContext.getResources().getString(R.string.modify_warning_limits));
            else
                mWarningLimitsButton.setText(mContext.getResources().getString(R.string.set_warning_limits));
        } else {
            mWarningLimitsButton.setVisibility(GONE);
        }
    }

    public void checkForLimitCross() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int allowedStd = sp.getInt(AppGlobals.PKEY_STD_LIMIT, -1) * 60;
        int allowedLocal = sp.getInt(AppGlobals.PKEY_LOCAL_LIMIT, -1) * 60;
        int allowedLocalStd = sp.getInt(AppGlobals.PKEY_STD_LOCAL_LIMIT, -1) * 60;
        int allowedRoaming = sp.getInt(AppGlobals.PKEY_ROAMING_LIMIT, -1) * 60;
        int allowedIsd = sp.getInt(AppGlobals.PKEY_ROAMING_LIMIT, -1) * 60;

        boolean localCrossed = WarningCrossNotifier.compareSeconds(allowedLocal, localSeconds) ||
                WarningCrossNotifier.compareSeconds(allowedLocalStd, localSeconds + stdSeconds);
        setWarningColor(mLocalMins, localCrossed);
        boolean stdCrossed = WarningCrossNotifier.compareSeconds(allowedStd, stdSeconds) ||
                WarningCrossNotifier.compareSeconds(allowedLocalStd, localSeconds + stdSeconds);
        setWarningColor(mStdMins, stdCrossed);
        setWarningColor(mRoamingMins, WarningCrossNotifier.compareSeconds(allowedRoaming, roamingSeconds));
        setWarningColor(mISDMins, WarningCrossNotifier.compareSeconds(allowedIsd, isdSeconds));
    }

    private void setWarningColor(TextView minuteView, boolean isLimitCrossed) {
        if (isLimitCrossed)
            minuteView.setTextColor(ContextCompat.getColor(mContext, R.color.warning_red));
        else
            minuteView.setTextColor(ContextCompat.getColor(mContext, android.R.color.primary_text_light));
    }

    public void onClick(View v) {
        Intent i = new Intent(mContext, LogListActivity.class);
        i.putExtra("calltype", mCallType.ordinal());
        switch (v.getId()) {
            case R.id.card_title_layout:
                break;
            case R.id.local_layout:
                i.putExtra("costtype", CostType.LOCAL.ordinal());
                break;
            case R.id.std_layout:
                i.putExtra("costtype", CostType.STD.ordinal());
                break;
            case R.id.roaming_layout:
                i.putExtra("costtype", CostType.ROAMING.ordinal());
                break;
            case R.id.isd_layout:
                i.putExtra("costtype", CostType.ISD.ordinal());
                break;
            case R.id.free_layout:
                i.putExtra("costtype", CostType.FREE.ordinal());
                break;
            case R.id.unknown_layout:
                i.putExtra("costtype", CostType.UNKNOWN.ordinal());
                break;
        }
        i.putExtra("startdate", cycleDates[0].getTime());
        i.putExtra("enddate", cycleDates[1].getTime());
        mContext.startActivity(i);
    }

}
