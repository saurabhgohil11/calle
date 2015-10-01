package com.evadroid.calle;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.evadroid.calle.utils.DateTimeUtils;
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

	private Context mContext;
	private CallType mCallType;
	private Date cycleDates[];

	
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
        mLocalSubMinutesLayout = (TableLayout) rootView.findViewById(R.id.sub_local_min_layout);
        mSTDSubMinutesLayout = (TableLayout) rootView.findViewById(R.id.sub_std_min_layout);
		
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

        mFreeMinsNoteAdded = (TextView) rootView.findViewById(R.id.free_mins_not_added_text);

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
			mTitleLabel.setText(R.string.outgoing);
			mTitleLabel.setTextColor(getResources().getColor(R.color.funky_green));
			mTitleMins.setTextColor(getResources().getColor(R.color.funky_green));
		} else {
			mTitleLabel.setText(R.string.incoming);
			mTitleLabel.setTextColor(getResources().getColor(R.color.funky_orange));
			mTitleMins.setTextColor(getResources().getColor(R.color.funky_orange));
		}

        DataBaseHelper dbHelper = AppGlobals.getDataBaseHelper(getContext());
		if(dbHelper == null) {
			AppGlobals.log(this,"returning from updateCallMinutesCard due to null dbHelper");
            return;
		}
        int seconds;

        //total seconds
        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, null);
        //mTitleMins.setText(seconds + " " + minsStr);
        setText(mTitleMins,seconds);

        //local Minutes
        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL);
        setVisibleAndSetText(mLocalLayout, mLocalMins, seconds);

        if(seconds>0 && mCallType == CallType.OUTGOING) {
            mLocalSubMinutesLayout.setVisibility(View.VISIBLE);
        } else {
            mLocalSubMinutesLayout.setVisibility(View.GONE);
        }

        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL, PhoneNumberUtil.PhoneNumberType.MOBILE);
        //mLocalMobileMins.setText(seconds + " " + minsStr);
        setText(mLocalMobileMins, seconds);

        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL, PhoneNumberUtil.PhoneNumberType.FIXED_LINE);
        //mLocalFixedLineMins.setText(seconds + " " + minsStr);
        setText(mLocalFixedLineMins, seconds);

        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.LOCAL, null);
        //mLocalOtherMins.setText(seconds + " " + minsStr);
        setText(mLocalOtherMins, seconds);

        //std seconds
        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD);
        setVisibleAndSetText(mStdLayout, mStdMins, seconds);

        if(seconds>0 && mCallType == CallType.OUTGOING) {
            mSTDSubMinutesLayout.setVisibility(View.VISIBLE);
        } else {
            mSTDSubMinutesLayout.setVisibility(View.GONE);
        }

        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD, PhoneNumberUtil.PhoneNumberType.MOBILE);
        //mSTDMobileMins.setText(seconds + " " + minsStr);
        setText(mSTDMobileMins, seconds);

        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD, PhoneNumberUtil.PhoneNumberType.FIXED_LINE);
        //mSTDFixedLineMins.setText(seconds + " " + minsStr);
        setText(mSTDFixedLineMins, seconds);

        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.STD, null);
        //mSTDOtherMins.setText(seconds + " " + minsStr);
        setText(mSTDOtherMins, seconds);

        //isd seconds
        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.ISD);
        setVisibleAndSetText(mISDLayout, mISDMins, seconds);

        //Unknown seconds
        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.UNKNOWN);
        setVisibleAndSetText(mUnknownLayout, mUnknownMins, seconds);

        //Roaming seconds
        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.ROAMING);
        setVisibleAndSetText(mRoamingLayout, mRoamingMins, seconds);

        //free seconds
        seconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), mCallType, CostType.FREE);
        setVisibleAndSetText(mFreeLayout, mFreeMins, seconds);

        if(seconds>0 && mCallType == CallType.OUTGOING) {
            mFreeMinsNoteAdded.setVisibility(View.VISIBLE);
            mFreeMins.setTextColor(Color.GRAY);
        } else {
            mFreeMinsNoteAdded.setVisibility(View.GONE);
        }
	}

    private void setText(TextView minuteView,int seconds) {
        if(AppGlobals.isMinuteMode)
            minuteView.setText(DateTimeUtils.timeToRoundedString(seconds));
        else
            minuteView.setText(DateTimeUtils.timeToString(seconds));
    }
	
	private void setVisibleAndSetText(View layout, TextView minuteView, int seconds) {
        if(layout==null || minuteView == null) return;
        if(seconds > 0) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
        if(AppGlobals.isMinuteMode)
            minuteView.setText(DateTimeUtils.timeToRoundedString(seconds));
        else
            minuteView.setText(DateTimeUtils.timeToString(seconds));
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
