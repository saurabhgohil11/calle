package com.evadroid.calle;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Date;

public class WarningCrossNotifier {
    private final Context mContext;
    private final NotificationManager mNotificationManager;
    public static final int TAG_STD_LOCAL = 121;
    public static final int TAG_STD = 122;
    public static final int TAG_LOCAL = 123;
    public static final int TAG_ROAMING = 124;
    public static final int TAG_ISD = 125;
    int localSeconds;
    int stdSeconds;
    int isdSeconds;
    int roamingSeconds;
    Date[] cycleDates;
    DataBaseHelper dbHelper;

    public WarningCrossNotifier(Context context) {

        mContext = context;
        if (context == null) {
            Log.e(AppGlobals.LOG_TAG, "WarningCrossNotifier : mContext null");
            mNotificationManager = null;
            return;
        }
        mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        cycleDates = AppGlobals.getCurrentBillCycleDates();
        dbHelper = AppGlobals.getDataBaseHelper(mContext);

    }

    public void checkAndShowNotification(CostType costType) {
        if (mContext == null) {
            Log.e(AppGlobals.LOG_TAG, "WarningCrossNotifier : mContext null");
            return;
        }
        if (dbHelper == null) {
            Log.e(AppGlobals.LOG_TAG, "WarningCrossNotifier : dbHelper null");
            dbHelper = AppGlobals.getDataBaseHelper(mContext);
        }

        localSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), CallType.OUTGOING, CostType.LOCAL);
        stdSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), CallType.OUTGOING, CostType.STD);
        isdSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), CallType.OUTGOING, CostType.ISD);
        roamingSeconds = dbHelper.getTotalSeconds(cycleDates[0].getTime(), cycleDates[1].getTime(), CallType.OUTGOING, CostType.ROAMING);


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int allowedSeconds;
        boolean shouldShowNotification;

        if (costType == CostType.LOCAL || costType == CostType.UNKNOWN) {
            allowedSeconds = sp.getInt(AppGlobals.PKEY_LOCAL_LIMIT, -1) * 60;
            shouldShowNotification = compareSeconds(allowedSeconds, localSeconds);
            if (shouldShowNotification)
                showNotification(TAG_LOCAL, localSeconds, allowedSeconds);
        }

        if (costType == CostType.STD || costType == CostType.UNKNOWN) {
            allowedSeconds = sp.getInt(AppGlobals.PKEY_STD_LIMIT, -1) * 60;
            shouldShowNotification = compareSeconds(allowedSeconds, stdSeconds);
            if (shouldShowNotification)
                showNotification(TAG_STD, stdSeconds, allowedSeconds);
        }

        if (costType == CostType.LOCAL || costType == CostType.STD || costType == CostType.UNKNOWN) {
            allowedSeconds = sp.getInt(AppGlobals.PKEY_STD_LOCAL_LIMIT, -1) * 60;
            shouldShowNotification = compareSeconds(allowedSeconds, localSeconds + stdSeconds);
            if (shouldShowNotification)
                showNotification(TAG_STD_LOCAL, localSeconds + stdSeconds, allowedSeconds);
        }

        if (costType == CostType.ISD || costType == CostType.UNKNOWN) {
            allowedSeconds = sp.getInt(AppGlobals.PKEY_ISD_LIMIT, -1) * 60;
            shouldShowNotification = compareSeconds(allowedSeconds, isdSeconds);
            if (shouldShowNotification)
                showNotification(TAG_ISD, isdSeconds, allowedSeconds);
        }

        if (costType == CostType.ROAMING) {
            allowedSeconds = sp.getInt(AppGlobals.PKEY_ROAMING_LIMIT, -1) * 60;
            shouldShowNotification = compareSeconds(allowedSeconds, roamingSeconds);
            if (shouldShowNotification)
                showNotification(TAG_ROAMING, roamingSeconds, allowedSeconds);
        }
    }

    private void showNotification(int tag, int usedSeconds, int allowedSeconds) {
        if (mContext == null)
            return;
        String title = getTitleNotificationString(tag);
        String msg = getMsgNotificationString(tag, usedSeconds, allowedSeconds);
        Intent calleIntent = new Intent(mContext, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), calleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true);
        mNotificationManager.notify(tag, mBuilder.build());
    }

    private String getTitleNotificationString(int tag) {
        switch (tag) {
            case TAG_STD_LOCAL:
                return mContext.getResources().getString(R.string.warning_notification_title_std_local);
            case TAG_LOCAL:
                return mContext.getResources().getString(R.string.warning_notification_title_local);
            case TAG_STD:
                return mContext.getResources().getString(R.string.warning_notification_title_std);
            case TAG_ROAMING:
                return mContext.getResources().getString(R.string.warning_notification_title_roaming);
            case TAG_ISD:
                return mContext.getResources().getString(R.string.warning_notification_title_isd);
            default:
                return "";
        }
    }

    private String getMsgNotificationString(int tag, int usedSeconds, int allowedSeconds) {
        switch (tag) {
            case TAG_STD_LOCAL:
                return String.format(mContext.getResources().getString(R.string.warning_notification_msg_std_local), usedSeconds / 60, allowedSeconds / 60);
            case TAG_LOCAL:
                return String.format(mContext.getResources().getString(R.string.warning_notification_msg_local), usedSeconds / 60, allowedSeconds / 60);
            case TAG_STD:
                return String.format(mContext.getResources().getString(R.string.warning_notification_msg_std), usedSeconds / 60, allowedSeconds / 60);
            case TAG_ROAMING:
                return String.format(mContext.getResources().getString(R.string.warning_notification_msg_roaming), usedSeconds / 60, allowedSeconds / 60);
            case TAG_ISD:
                return String.format(mContext.getResources().getString(R.string.warning_notification_msg_isd), usedSeconds / 60, allowedSeconds / 60);
            default:
                return "";
        }
    }

    public static boolean compareSeconds(int allowedSeconds, int usedSeconds) {
        if (allowedSeconds < 0)
            return false;
        return (usedSeconds > allowedSeconds);
    }
}
