package io.coti.trustscore.data.scoreenums;

import io.coti.trustscore.data.scoreevents.KYCDocumentScoreData;
import io.coti.trustscore.data.scoreevents.Questionnaire1DocumentScoreData;
import io.coti.trustscore.data.scoreevents.Questionnaire2DocumentScoreData;
import io.coti.trustscore.data.scoreevents.Questionnaire3DocumentScoreData;

public enum DocumentRequestType {
    KYC("KYC", KYCDocumentScoreData.class),
    QUESTIONNAIRE1("QUESTIONNAIRE1", Questionnaire1DocumentScoreData.class),
    QUESTIONNAIRE2("QUESTIONNAIRE2", Questionnaire2DocumentScoreData.class),
    QUESTIONNAIRE3("QUESTIONNAIRE3", Questionnaire3DocumentScoreData.class);

    private String text;
    public Class score;

    DocumentRequestType(String text, Class score) {
        this.text = text;
        this.score = score;
    }

    public static DocumentRequestType enumFromString(String text) {
        for (DocumentRequestType value : DocumentRequestType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format("Not existing document type {}", text));
    }

    @Override
    public String toString() {
        return text;
    }
}
