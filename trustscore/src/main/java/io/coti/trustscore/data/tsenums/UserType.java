package io.coti.trustscore.data.tsenums;

import java.util.HashMap;
import java.util.Map;

public enum UserType {
    CONSUMER("consumer"),
    MERCHANT("merchant"),
    ARBITRATOR("arbitrator"),
    FULL_NODE("fullnode"),
    DSP_NODE("dspnode"),
    TRUST_SCORE_NODE("trustscorenode");

    private static final Map<String, UserType> labelsMap = new HashMap<>();

    static {
        for (UserType e: values()) {
            labelsMap.put(e.text, e);
        }
    }

    private String text;

    UserType(String text) {
        this.text = text;
    }

    public static UserType enumFromString(String text) {
        return labelsMap.get(text);
    }

    @Override
    public String toString() {
        return text;
    }
}
