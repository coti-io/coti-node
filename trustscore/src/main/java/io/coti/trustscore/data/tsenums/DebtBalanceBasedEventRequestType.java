package io.coti.trustscore.data.tsenums;

import io.coti.trustscore.data.tsevents.*;

public enum DebtBalanceBasedEventRequestType {
    DEBT("DEBT", DebtBalanceBasedEventData.class),
    CLOSEDEBT("CLOSEDEBT", CloseDebtBalanceBasedEventData.class);

    private String text;
    private Class eventClass;

    DebtBalanceBasedEventRequestType(String text , Class eventClass) {
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
