package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.DisputeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class EmailNotificationsService {

    //@Value("${cca.address}")
    private String CCA_ADDRESS;

    public void sendEmailNewDispute(Hash merchantHash, DisputeData disputeData) {
        sendEmail(merchantHash, disputeData);
    }

    private void sendEmail(Hash merchantHash, DisputeData disputeData) {
        RestTemplate restTemplate = new RestTemplate();
    }
}