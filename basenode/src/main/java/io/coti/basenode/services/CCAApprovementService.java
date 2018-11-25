package io.coti.basenode.services;

import io.coti.basenode.crypto.CCAApprovementRequestCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.data.CCAApprovementResponse;
import io.coti.basenode.http.data.CCAApprovmentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CCAApprovementService {

    @Value("${cca.server.address}")
    private String ccaAddress ;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CCAApprovementRequestCrypto ccaApprovementCrypto;

    public ResponseEntity<CCAApprovementResponse> sendCCAApprovment(CCAApprovmentRequest ccaApprovmentRequest){
        CCAApprovementResponse dummyCcaApprovementResponse = new CCAApprovementResponse();
        dummyCcaApprovementResponse.setSignerHash(new Hash("abc"));
        dummyCcaApprovementResponse.setSignature(new SignatureData());
        dummyCcaApprovementResponse.setTrustScore(5.0);
        return ResponseEntity.ok(dummyCcaApprovementResponse);
//        try {
//            ccaApprovementCrypto.signMessage(ccaApprovmentRequest);
//
//            ResponseEntity<CCAApprovementResponse> approvementResponseEntity = restTemplate.postForEntity(ccaAddress, ccaApprovmentRequest, CCAApprovementResponse.class);
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
