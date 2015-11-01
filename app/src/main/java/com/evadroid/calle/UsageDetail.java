package com.evadroid.calle;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UsageDetail {
    Date cycleDates[];
    int outgoingSeconds;
    int incomingSeconds;

    public String getCycleString() {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yy");
        return sdf.format(cycleDates[0]) + " - " + sdf.format(cycleDates[1]);
    }

    public int getOutgoingSeconds() {
        return outgoingSeconds;
    }

    public int getIncomingSeconds() {
        return incomingSeconds;
    }

    @Override
    public String toString() {
        return "Cycle:" + getCycleString() + ", ogSecs:" + getOutgoingSeconds() + ", inSecs:" + getIncomingSeconds();
    }
}
