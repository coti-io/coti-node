package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.FinancialServerEvent;
import io.coti.financialserver.http.KYCEmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class EmailNotificationsService {

    private static final String CREATE_EMAIL_PATH = "sendEmail";
    private static final String SLASH = "/";
    @Value("${kycserver.url}")
    private String KYC_SERVER_URL;

    public void sendEmail(Hash disputeHash, Hash merchantHash, FinancialServerEvent financialServerEvent, List<DisputeItemData> disputeItemsData) {

        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.postForObject(
                    KYC_SERVER_URL + SLASH + CREATE_EMAIL_PATH,
                    new KYCEmailRequest(disputeHash, merchantHash, financialServerEvent, disputeItemsData),
                    KYCEmailRequest.class);
        } catch (Exception e) {
            //log.error("Couldn't send email notification with KYC", KYC_SERVER_URL); TODO: when KYC ready to get emails uncomment this
            //throw new RuntimeException(e);
        }

    }
}