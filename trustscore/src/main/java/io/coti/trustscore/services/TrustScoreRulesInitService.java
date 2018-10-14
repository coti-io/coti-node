package io.coti.trustscore.services;

import io.coti.trustscore.rulesData.RulesData;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;


@Data
@Service
public class TrustScoreRulesInitService {

    @Autowired
    BucketTransactionService bucketTransactionService;

    private RulesData rulesData;

    @PostConstruct()
    void init() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(RulesData.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        rulesData = (RulesData) jaxbUnmarshaller.unmarshal(new File("trustScoreRules.xml"));

        bucketTransactionService.init(rulesData);
    }
}
