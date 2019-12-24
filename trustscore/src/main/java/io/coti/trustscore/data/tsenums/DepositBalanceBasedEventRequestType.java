package io.coti.trustscore.data.tsenums;

import io.coti.trustscore.data.tsevents.CloseDepositBalanceBasedEventData;
import io.coti.trustscore.data.tsevents.DepositBalanceBasedEventData;

public enum DepositBalanceBasedEventRequestType {
    DEPOSIT("DEPOSIT", DepositBalanceBasedEventData.class),
    CLOSEDEPOSIT("CLOSEDEPOSIT", CloseDepositBalanceBasedEventData.class);

    private String text;
    private Class eventClass;

    DepositBalanceBasedEventRequestType(String text , Class eventClass) {
        this.text = text;
        this.eventClass = eventClass;
    }

    @Override
    public String toString() {
        return text;
    }

    public Class getEventClass(){
        return eventClass;
    }
}
