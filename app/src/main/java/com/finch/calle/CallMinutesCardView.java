package com.finch.calle;


import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.Date;

public class CallMinutesCardView extends LinearLayout implements View.OnClickListener{

    private RelativeLayout mTitleLayout;
    private LinearLayout mLocalLayout;
    private LinearLayout mStdLayout;
    private LinearLayout mRoamingLayout;
    private LinearLayout mISDLayout;
    private LinearLayout mFreeLayout;
    private LinearLayout mUnknownLayout;
    private LinearLayout mLocalSubMinutesLayout;
    private LinearLayout mSTDSubMinutesLayout;;

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
	
	private Context mContext;
	private CallType mCallType;
	private Date cycleDates[];
	
	String minsStr;
	
	public CallMinutesCardView(Context context) {
		this(context, null);
	}

	public CallMinutesCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		minsStr = context.getResources().getString(R.string.mins);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rootView = inflater.inflate(R.layout.call_minutes_card, this, true);
		initView(rootView);
	}


	
	public void setCycleAndType(Date cycleDates[],CallType callType) {
		this.mCallType = callType;
		this.cycleDates = cycleDates;
		
	}
	
	private void initView(View rootView) {
	
		mTitleLayout  = (RelativeLayout) rootView.findViewById(R.id.card_title_layout);
        mLocalLayout = (LinearLayout) rootView.findViewById(R.id.local_layout);
        mStdLayout = (LinearLayout) rootView.findViewById(R.id.std_layout);
        mRoamingLayout = (LinearLayout) rootView.findViewById(R.id.roaming_layout);
        mISDLayout = (LinearLayout) rootView.findViewById(R.id.isd_layout);
        mFreeLayout = (LinearLayout) rootView.findViewById(R.id.free_layout);
        mUnknownLayout = (LinearLayout) rootView.findViewById(R.id.unknown_layout);
        mLocalSubMinutesLayout = (LinearLayout) rootView.findViewById(R.id.sub_local_min_layout);
        mSTDSubMinutesLayout = (LinearLayout) rootView.findViewById(R.id.sub_std_min_layout);
		
		mTitleLabel  = (TextView) rootView.findViewById(R.id.card_title);

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

        mTitleLayout.setOnClickListener(this);
        mLocalLayout.setOnClickListener(this);
        mStdLayout.setOnClickListener(this);
        mRoamingLayout.setOnClickListener(this);
        mISDLayout.setOnClickListener(this);
        mFreeLayout.setOnClickListener(this);
        mUnknownLayout.setOnClickListener(this);
	}
	
	public void updateCallMinutesCard() {
		if (mTitleLabel == null) {
            AppGlobals.log(this,"returning from updateCallMinutesCard");
            return;
        }
		if(mCallType == CallType.OUTGOING) {
			mTitleLabel.setText(R.string.outgoing_calls);
			mTitleLabel.setTextColor(getResources().getColor(R.color.funky_green));
			mTitleMins.setTextColor(getResources().getColor(R.color.funky_green));
		} else {
			mTitleLabel.setText(R.string.incoming_calls);
			mTitleLabel.setTextColor(getResources().getColor(R.color.funky_orange));
			mTitleMins.setTextColor(getResources().getColor(R.color.funky_orange));
		}

        DataBaseHelper dbHelper = AppGlobals.dbHelper;
		if(dbHelper == null) {
			AppGlobals.log(this,"returning from updateCallMinutesCard due to null dbHelper");
            return;
		}
        int minutes;

        //total minutes
        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, null);

        mTitleMins.setText(minutes + " " + minsStr);
        //local Minutes
        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL);
        setVisibleAndSetText(mLocalLayout, mLocalMins, minutes);

        if(minutes>0 && mCallType == CallType.OUTGOING) {
            mLocalSubMinutesLayout.setVisibility(View.VISIBLE);
        } else {
            mLocalSubMinutesLayout.setVisibility(View.GONE);
        }

        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL, PhoneNumberUtil.PhoneNumberType.MOBILE);
        mLocalMobileMins.setText(minutes + " " + minsStr);

        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL, PhoneNumberUtil.PhoneNumberType.FIXED_LINE);
        mLocalFixedLineMins.setText(minutes + " " + minsStr);

        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL, null);
        mLocalOtherMins.setText(minutes + " " + minsStr);

        //std minutes
        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD);
        setVisibleAndSetText(mStdLayout, mStdMins, minutes);

        if(minutes>0 && mCallType == CallType.OUTGOING) {
            mSTDSubMinutesLayout.setVisibility(View.VISIBLE);
        } else {
            mSTDSubMinutesLayout.setVisibility(View.GONE);
        }

        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD, PhoneNumberUtil.PhoneNumberType.MOBILE);
        mSTDMobileMins.setText(minutes + " " + minsStr);

        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD, PhoneNumberUtil.PhoneNumberType.FIXED_LINE);
        mSTDFixedLineMins.setText(minutes + " " + minsStr);

        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD, null);
        mSTDOtherMins.setText(minutes + " " + minsStr);

        //isd minutes
        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.ISD);
        setVisibleAndSetText(mISDLayout, mISDMins, minutes);

        //Unknown minutes
        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.UNKNOWN);
        setVisibleAndSetText(mUnknownLayout, mUnknownMins, minutes);

        //Roaming minutes
        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType,CostType.ROAMING);
        setVisibleAndSetText(mRoamingLayout, mRoamingMins, minutes);

        //free minutes
        minutes = dbHelper.getTotalMinutes(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.FREE);
        setVisibleAndSetText(mFreeLayout, mFreeMins, minutes);
        mFreeMins.setText("- "+minutes + " " + minsStr);
	}
	
	private void setVisibleAndSetText(View layout, TextView minuteView, int minutes) {
        if(layout==null || minuteView == null) return;
        if(minutes > 0) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
        minuteView.setText(minutes + " " + minsStr);
    }

	public void onClick(View v) {
		Intent i = new Intent(mContext,LogListActivity.class);
        i.putExtra("calltype", mCallType.ordinal());
        switch (v.getId()) {
            case R.id.card_title_layout:
                break;
            case R.id.local_layout:
                i.putExtra("costtype",CostType.LOCAL.ordinal());
                break;
            case R.id.std_layout:
                i.putExtra("costtype",CostType.STD.ordinal());
                break;
            case R.id.roaming_layout:
                i.putExtra("costtype",CostType.ROAMING.ordinal());
                break;
            case R.id.isd_layout:
                i.putExtra("costtype",CostType.ISD.ordinal());
                break;
            case R.id.free_layout:
                i.putExtra("costtype",CostType.FREE.ordinal());
                break;
            case R.id.unknown_layout:
                i.putExtra("costtype",CostType.UNKNOWN.ordinal());
                break;
        }
        i.putExtra("startdate",cycleDates[0].getTime());
        i.putExtra("enddate",cycleDates[1].getTime());
        mContext.startActivity(i);
	}

}
