package io.coti.trustscore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.*;
import io.coti.trustscore.config.rules.RulesData;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class BucketUtil {
    public static RulesData generateRulesDataObject() {
        RulesData rulesData = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ClassLoader classLoader = BucketUtil.class.getClassLoader();
            rulesData = objectMapper.readValue(new File(classLoader.getResource("trustScoreRules.json").getFile()), RulesData.class);
        } catch (IOException e) {
            log.error("Error reading from JSON file", e);
            log.error("Shutting down!");
            System.exit(1);
        }
        return rulesData;
    }

    public static TransactionData createTransactionWithSpecificHash(Hash transactionHash, Hash userHash, Double trustScore) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>(
                Arrays.asList(new BaseTransactionData
                        (new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                                new BigDecimal(0),
                                transactionHash,
                                new SignatureData("", ""),
                                new Date())));
        TransactionData transactionData = new TransactionData(baseTransactions,
                transactionHash,
                "test",
                Arrays.asList(new TransactionTrustScoreData(
                        userHash,
                        transactionHash,
                        trustScore)),
                new Date(),
                userHash);
        DspConsensusResult dspConsensusResult = new DspConsensusResult(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"));
        dspConsensusResult.setIndexingTime(new Date());
        transactionData.setDspConsensusResult(dspConsensusResult);
        return transactionData;
    }
}
