package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.config.rules.UserScoreRules;
import io.coti.trustscore.data.scorebuckets.BucketDocumentScoreData;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreenums.ScoreType;
import io.coti.trustscore.data.scoreenums.UserType;
import io.coti.trustscore.data.scoreevents.DocumentScoreData;
import io.coti.trustscore.data.parameters.DocumentDecayedContributionData;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.services.calculationservices.BucketDocumentScoreCalculator;
import io.coti.trustscore.services.interfaces.IBucketService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketDocumentScoreService implements IBucketService<DocumentScoreData, BucketDocumentScoreData> {

    private static Map<UserType, UserParameters> kYCUserParametersMap;
    private static Map<UserType, UserParameters> questionnaire1UserParametersMap;
    private static Map<UserType, UserParameters> questionnaire2UserParametersMap;
    private static Map<UserType, UserParameters> questionnaire3UserParametersMap;

    public static UserParameters userParameters(FinalScoreType finalScoreType, UserType userType) {
        switch (finalScoreType) {
            case KYC:
                return kYCUserParametersMap.get(userType);
            case QUESTIONNAIRE1:
                return questionnaire1UserParametersMap.get(userType);
            case QUESTIONNAIRE2:
                return questionnaire2UserParametersMap.get(userType);
            case QUESTIONNAIRE3:
                return questionnaire3UserParametersMap.get(userType);
        }
        return null;
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
    public BucketDocumentScoreData addScoreToCalculations(DocumentScoreData scoreData, BucketDocumentScoreData bucketData) {
        BucketDocumentScoreCalculator bucketCalculator = new BucketDocumentScoreCalculator(bucketData);
        bucketCalculator.decayScores();

        FinalScoreType finalScoreType = FinalScoreType.enumFromString(scoreData.getClass().getSimpleName());
        DocumentDecayedContributionData documentDecayedContributionData = new DocumentDecayedContributionData(scoreData.getScore(),
                scoreData.getScore());
        bucketData.getActualScoresDataMap().put(finalScoreType, documentDecayedContributionData);
        return bucketData;
    }

    @Override
    public double getBucketSumScore(BucketDocumentScoreData bucketData) {
        BucketDocumentScoreCalculator bucketCalculator = new BucketDocumentScoreCalculator(bucketData);
        bucketCalculator.decayScores();
        return bucketCalculator.getBucketSumScore();
    }

    @Override
    public ScoreType getScoreType() {
        return ScoreType.DOCUMENT_SCORE;
    }
}
