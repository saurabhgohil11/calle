package com.finch.calle;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UsageDetail {
    Date cycleDates[];
    int outgoingMinutes;
    int incomingMinutes;

    public String getCycleString() {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yy");
        return sdf.format(cycleDates[0]) + " - " + sdf.format(cycleDates[1]);
    }
    public int getOutgoingMinutes() {
        return outgoingMinutes;
    }
    public int getIncomingMinutes() {
        return incomingMinutes;
    }

    @Override
    public String toString() {
        return "Cycle:"+getCycleString()+", ogMins:"+getOutgoingMinutes()+", inMins:"+getIncomingMinutes();
    }
}
