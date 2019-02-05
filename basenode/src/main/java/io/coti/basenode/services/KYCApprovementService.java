package io.coti.basenode.services;

import io.coti.basenode.crypto.GetNodeRegistrationRequestCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.http.GetNodeRegistrationRequest;
import io.coti.basenode.http.GetNodeRegistrationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KYCApprovementService {

    @Value("${kycserver.url}")
    private String kycServerAddress;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private GetNodeRegistrationRequestCrypto kycApprovementCrypto;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;


    public ResponseEntity<GetNodeRegistrationResponse> sendKycServerApprovement(GetNodeRegistrationRequest getNodeRegistrationRequest) {


    }


}
