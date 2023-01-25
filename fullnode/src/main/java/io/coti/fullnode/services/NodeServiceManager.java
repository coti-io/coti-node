package io.coti.fullnode.services;

import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.fullnode.crypto.FullNodeFeeRequestCrypto;
import io.coti.fullnode.crypto.ResendTransactionRequestCrypto;
import io.coti.fullnode.websocket.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@SuppressWarnings({"java:S1104", "java:S1444"})
public class NodeServiceManager extends BaseNodeServiceManager {

    public static ResendTransactionRequestCrypto resendTransactionRequestCrypto;
    public static FullNodeFeeRequestCrypto fullNodeFeeRequestCrypto;
    public static WebSocketSender webSocketSender;
    public static FeeService feeService;

    @Autowired
    public ResendTransactionRequestCrypto autowiredResendTransactionRequestCrypto;
    @Autowired
    public FullNodeFeeRequestCrypto autowiredFullNodeFeeRequestCrypto;
    @Autowired
    public WebSocketSender autowiredWebSocketSender;
    @Autowired
    public FeeService autowiredFeeService;

}
