package com.finch.calle.utils;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {

    public static Date getNextCycleDate(long timeInMillis) {
        if(timeInMillis < 0) {
            throw new IllegalArgumentException("timeInMillis must be greater than zero!");
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeInMillis);
        c.add(Calendar.MONTH,1);
        c.add(Calendar.DATE,-1);
        return new Date(c.getTimeInMillis());
    }

    public static Date getNextCycleDate(Date date) {
        if(date == null) {
            throw new IllegalArgumentException("date must not be null!");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH,1);
        c.add(Calendar.DATE,-1);
        return new Date(c.getTimeInMillis());
    }

    public static String timeToRelativeString(long timeInMillis) {  //returns some mins ago
        return DateUtils.getRelativeTimeSpanString (timeInMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    }

    public static String timeToString(int timeInSecs) {  //returns 0 mins 1 secs
        if(timeInSecs < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        return timeToString((long) timeInSecs * 1000);
    }

    public static String timeToRoundedString(long timeInSecs) {  //returns 1 mins
        if(timeInSecs < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        if(timeInSecs==0)
            return "0 Mins";
        return timeToString((timeInSecs/60+1)*60*1000,false);
    }

    public static String timeToString(long timeInMillis) {
        return timeToString(timeInMillis,true);
    }

    public static String timeToString(long timeInMillis,boolean showSeconds) {  //returns 0 mins 1 secs
        if(timeInMillis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        long days = TimeUnit.MILLISECONDS.toDays(timeInMillis);
        timeInMillis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis);
        timeInMillis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis);
        timeInMillis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis);

        StringBuilder sb = new StringBuilder(64);
        if(days > 0) {
            sb.append(days);
            sb.append("Days");
        }
        if(days > 0 || hours > 0) {
            sb.append(hours);
            sb.append(" Hours ");
        }
        if(days > 0 || hours > 0 || minutes > 0) {
            sb.append(minutes);
            sb.append(" Mins ");
        }
        if(showSeconds) {
            sb.append(seconds);
            sb.append(" Secs");
        }

        return(sb.toString());
    }

    public static String timeToDateString(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss a");
        return sdf.format(new Date(timeInMillis));
    }

    public static String toDisplayCase(String s) {

        final String ACTIONABLE_DELIMITERS = " '-/"; // these cause the character following
        // to be capitalized

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : Character.toLowerCase(c);
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString();
    }
}
