package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.crypto.WebSocketAuthRequestCrypto;
import io.coti.financialserver.data.WebSocketUserHashSessionName;
import io.coti.financialserver.http.WebSocketAuthRequest;
import io.coti.financialserver.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static io.coti.financialserver.http.HttpStringConstants.INVALID_SIGNATURE;
import static io.coti.financialserver.http.HttpStringConstants.SUCCESS;

@Slf4j
@Component
public class WebSocketService {

    @Autowired
    private ConsumerDisputes consumerDisputes;
    @Autowired
    private MerchantDisputes merchantDisputes;
    @Autowired
    private ArbitratorDisputes arbitratorDisputes;
    @Autowired
    private WebSocketAuthRequestCrypto webSocketAuthRequestCrypto;
    @Autowired
    private SimpMessagingTemplate messagingSender;
    @Autowired
    Disputes disputes;
    @Autowired
    WebSocketMapUserHashSessionName webSocketMapUserHashSessionName;

    public ResponseEntity<IResponse> disputesSubscribe(WebSocketAuthRequest webSocketAuthRequest, SimpMessageHeaderAccessor headerAccessor) {

        /*webSocketAuthRequestCrypto.signMessage(webSocketAuthRequest);
        if (!webSocketAuthRequestCrypto.verifySignature(webSocketAuthRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }*/

        String webSocketUserListenerName = headerAccessor.getUser().getName();
        Hash userHash = webSocketAuthRequest.getUserHash();
        WebSocketUserHashSessionName webSocketUserHashSessionName = webSocketMapUserHashSessionName.getByHash(webSocketUserListenerName);
        if(webSocketUserHashSessionName == null) {
            webSocketUserHashSessionName = new WebSocketUserHashSessionName(userHash);
        }

        webSocketUserHashSessionName.setWebSocketUserName(webSocketUserListenerName);
        webSocketMapUserHashSessionName.put(webSocketUserHashSessionName);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

}