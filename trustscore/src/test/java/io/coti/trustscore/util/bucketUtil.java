package io.coti.trustscore.util;

import io.coti.basenode.data.*;
import io.coti.trustscore.rulesData.RulesData;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class bucketUtil {
    public static RulesData generateRulesDataObject() {
        RulesData rulesData = null;
        try {

            ClassLoader classLoader = bucketUtil.class.getClassLoader();
            File file = new File(classLoader.getResource("trustScoreRules.xml").getFile());
            JAXBContext jaxbContext = JAXBContext.newInstance(RulesData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            rulesData = (RulesData)jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            log.error("Exception when reading from trustScoreRules.xml file: ", e);
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
        TransactionData tx = new TransactionData(baseTransactions,
                transactionHash,
                "test",
                Arrays.asList(new TransactionTrustScoreData(
                        userHash,
                        transactionHash,
                        trustScore)),
                        new Date(),
                        userHash);
        return tx;
    }
}
