package com.evadroid.calle.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.evadroid.calle.R;

public class AppRater {
    private final static String APP_TITLE = "Call-E";
    public final static String APP_PNAME = "com.evadroid.calle";

    private final static int DAYS_UNTIL_PROMPT = 3;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 5;//Min number of launches

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }
        editor.commit();
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        Resources resources = mContext.getResources();
        String rate = resources.getString(R.string.rate);
        String rateMsg = resources.getString(R.string.rate_msg, APP_TITLE);
        String remindMeLater = resources.getString(R.string.remind_me_later);
        String noThanks = resources.getString(R.string.no_thanks);
        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
        dialog.setTitle(rate + " " + APP_TITLE);

        dialog.setMessage(rateMsg);

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, rate + "\n" + APP_TITLE, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                dialog.dismiss();
            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, noThanks, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, remindMeLater, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (editor != null) {
                    editor.putLong("launch_count", 2);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}