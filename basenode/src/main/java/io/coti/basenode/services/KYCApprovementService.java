package io.coti.basenode.services;

import io.coti.basenode.crypto.KYCApprovementRequestCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.data.KYCApprovementResponse;
import io.coti.basenode.http.data.KYCApprovmentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KYCApprovementService {

    @Value("${cca.server.address}")
    private String ccaAddress ;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KYCApprovementRequestCrypto ccaApprovementCrypto;

    public ResponseEntity<KYCApprovementResponse> sendCCAApprovment(KYCApprovmentRequest KYCApprovmentRequest){
        KYCApprovementResponse dummyKYCApprovementResponse = new KYCApprovementResponse();
        dummyKYCApprovementResponse.setSignerHash(new Hash("abc"));
        dummyKYCApprovementResponse.setSignature(new SignatureData());
        dummyKYCApprovementResponse.setTrustScore(5.0);
        return ResponseEntity.ok(dummyKYCApprovementResponse);
//        try {
//            ccaApprovementCrypto.signMessage(KYCApprovmentRequest);
//
//            ResponseEntity<KYCApprovementResponse> approvementResponseEntity = restTemplate.postForEntity(ccaAddress, KYCApprovmentRequest, KYCApprovementResponse.class);
//
//            if (approvementResponseEntity.getStatusCode() != HttpStatus.OK) {
//                log.error("cca returned an error. response: {} . closing server", approvementResponseEntity);
//                System.exit(-1);
//            }
//            return approvementResponseEntity;
//        }
//        catch (Exception ex){
//            log.error("Exception while sending CCA approvement. closing server", ex);
//            System.exit(-1);
//        }
//        return ResponseEntity.noContent().build();
    }



}
