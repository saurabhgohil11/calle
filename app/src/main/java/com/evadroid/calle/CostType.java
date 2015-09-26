package com.evadroid.calle;

public enum CostType {
    LOCAL, STD, FREE, ISD, UNKNOWN, ROAMING
    //ROAMING is a dummytype, we dont actually store it in DB we have a flag for that
    //ROAMING is introduced to pass costtype b/w diff classes and activities for easy function implementation
}
