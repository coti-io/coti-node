package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.tsbuckets.BucketDocumentEventData;
import io.coti.trustscore.data.tsenums.FinalEventType;
import io.coti.trustscore.data.tsenums.EventType;
import io.coti.trustscore.data.tsenums.UserType;
import io.coti.trustscore.data.tsevents.DocumentEventData;
import io.coti.trustscore.data.contributiondata.DocumentDecayedContributionData;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.calculationservices.BucketDocumentScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketDocumentScoreService implements IBucketService<DocumentEventData, BucketDocumentEventData> {

    private static Map<UserType, UserParameters> kYCUserParametersMap;
    private static Map<UserType, UserParameters> questionnaire1UserParametersMap;
    private static Map<UserType, UserParameters> questionnaire2UserParametersMap;
    private static Map<UserType, UserParameters> questionnaire3UserParametersMap;

    public static UserParameters userParameters(FinalEventType finalEventType, UserType userType) {
        switch (finalEventType) {
            case KYC:
                return kYCUserParametersMap.get(userType);
            case QUESTIONNAIRE1:
                return questionnaire1UserParametersMap.get(userType);
            case QUESTIONNAIRE2:
                return questionnaire2UserParametersMap.get(userType);
            case QUESTIONNAIRE3:
                return questionnaire3UserParametersMap.get(userType);
            default:
                return null;
        }
    }

    public static void init(Map<String, ScoreRules> classToScoreRulesMap) {
        kYCUserParametersMap = new ConcurrentHashMap<>();
        questionnaire1UserParametersMap = new ConcurrentHashMap<>();
        questionnaire2UserParametersMap = new ConcurrentHashMap<>();
        questionnaire3UserParametersMap = new ConcurrentHashMap<>();

        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("KYCDocumentScoreData").getUsers()) {
            kYCUserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new UserParameters(userScoreRules));
        }
        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("Questionnaire1DocumentScoreData").getUsers()) {
            questionnaire1UserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new UserParameters(userScoreRules));
        }
        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("Questionnaire2DocumentScoreData").getUsers()) {
            questionnaire2UserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new UserParameters(userScoreRules));
        }
        for (UserScoreRules userScoreRules : classToScoreRulesMap.get("Questionnaire3DocumentScoreData").getUsers()) {
            questionnaire3UserParametersMap.put(UserType.enumFromString(userScoreRules.getUserType()), new UserParameters(userScoreRules));
        }
    }

    @Override
    public BucketDocumentEventData addScoreToCalculations(DocumentEventData scoreData, BucketDocumentEventData bucketData) {
        BucketDocumentScoreCalculator bucketCalculator = new BucketDocumentScoreCalculator(bucketData);
        bucketCalculator.decayScores();

        FinalEventType finalEventType = FinalEventType.enumFromString(scoreData.getClass().getSimpleName());
        DocumentDecayedContributionData documentDecayedContributionData = new DocumentDecayedContributionData(scoreData.getScore(),
                scoreData.getScore());
        bucketData.getActualScoresDataMap().put(finalEventType, documentDecayedContributionData);
        return bucketData;
    }

    @Override
    public double getBucketSumScore(BucketDocumentEventData bucketData) {
        BucketDocumentScoreCalculator bucketCalculator = new BucketDocumentScoreCalculator(bucketData);
        bucketCalculator.decayScores();
        return bucketCalculator.getBucketSumScore();
    }

    @Override
    public EventType getScoreType() {
        return EventType.DOCUMENT_SCORE;
    }
}
