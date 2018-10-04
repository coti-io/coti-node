package io.coti.trustscore.services;

import io.coti.trustscore.data.UsersScoresByType;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;



@Data
@Service
public class TrustScoreRulesInitService{

    private UsersScoresByType usersScoresByType;

    @PostConstruct()
    void init() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(UsersScoresByType.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        usersScoresByType = (UsersScoresByType) jaxbUnmarshaller.unmarshal(new File("trustScoreRules.xml"));
    }

}
