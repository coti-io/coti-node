package io.coti.basenode.constants;

import io.coti.basenode.data.NetworkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BaseNodeApplicationConstant {

    @Autowired
    private static BaseNodeConstantInjector inj;
    public static final NetworkType NETWORK_TYPE = ConstantInjector.networkType;
    public static final String NETWORK_DIFFICULTY = ConstantInjector.networkDifficulty;
    public static final String NODE_IP = ConstantInjector.nodeIp;
    public static final String NODE_PORT = ConstantInjector.nodePort;
    public static final String WEB_SERVER_URL = ConstantInjector.webServerUrl;
    public static final String NODE_MANAGER_IP = ConstantInjector.nodeManagerIp;
    public static final String NODE_MANAGER_PORT = ConstantInjector.nodeManagerPort;
    public static final String NODE_MANAGER_PROPAGATION_PORT = ConstantInjector.nodeManagerPropagationPort;
    public static final String KYC_SERVER_ADDRESS = ConstantInjector.kycServerAddress;
    public static final String KYC_SERVER_PUBLIC_KEY = ConstantInjector.kycServerPublicKey;
    public static final String NODE_PRIVATE_KEY = inj.nodePrivateKey;
    public static final int MIN_SOURCE_PERCENTAGE = ConstantInjector.minSourcePercentage;
    public static final int MAX_NEIGHBOURHOOD_RADIUS = ConstantInjector.maxNeighbourhoodRadius;
    public static final int TRUST_CHAIN_THRESHOLD = ConstantInjector.trustChainThreshold;
    public static final String LOGGING_FILE_NAME = ConstantInjector.loggingFileName;
    public static final boolean ALLOW_TRANSACTION_MONITORING = ConstantInjector.allowTransactionMonitoring;

    private static class ConstantInjector {

        private static NetworkType networkType;
        private static String networkDifficulty;
        private static String nodeIp;
        private static String nodePort;
        private static String webServerUrl;
        private static String nodeManagerIp;
        private static String nodeManagerPort;
        private static String nodeManagerPropagationPort;
        private static String kycServerAddress;
        private static String kycServerPublicKey;
        private static String nodePrivateKey;
        private static int minSourcePercentage;
        private static int maxNeighbourhoodRadius;
        private static int trustChainThreshold;
        private static String loggingFileName;
        private static boolean allowTransactionMonitoring;

        private ConstantInjector(@Value("${network}") NetworkType networkType,
                                 @Value("${network.difficulty}") String networkDifficulty,
                                 @Value("${server.ip}") String nodeIp,
                                 @Value("${server.port}") String nodePort,
                                 @Value("${server.url}") String webServerUrl,
                                 @Value("${node.manager.ip}") String nodeManagerIp,
                                 @Value("${node.manager.port}") String nodeManagerPort,
                                 @Value("${node.manager.propagation.port}") String nodeManagerPropagationPort,
                                 @Value("${kycserver.url}") String kycServerAddress,
                                 @Value("${kycserver.public.key}") String kycServerPublicKey,
                                 @Value("{${global.private.key}}") String nodePrivateKey,
                                 @Value("${min.source.percentage}") int minSourcePercentage,
                                 @Value("${max.neighbourhood.radius}") int maxNeighbourhoodRadius,
                                 @Value("${cluster.trust.chain.threshold}") int trustChainThreshold,
                                 @Value("${logging.file.name}") String loggingFileName,
                                 @Value("${allow.transaction.monitoring}") boolean allowTransactionMonitoring) {

            ConstantInjector.networkType = networkType;
            ConstantInjector.networkDifficulty = networkDifficulty;
            ConstantInjector.nodeIp = nodeIp;
            ConstantInjector.nodePort = nodePort;
            ConstantInjector.webServerUrl = webServerUrl;
            ConstantInjector.nodeManagerIp = nodeManagerIp;
            ConstantInjector.nodeManagerPort = nodeManagerPort;
            ConstantInjector.nodeManagerPropagationPort = nodeManagerPropagationPort;
            ConstantInjector.kycServerAddress = kycServerAddress;
            ConstantInjector.kycServerPublicKey = kycServerPublicKey;
            ConstantInjector.nodePrivateKey = nodePrivateKey;
            ConstantInjector.minSourcePercentage = minSourcePercentage;
            ConstantInjector.maxNeighbourhoodRadius = maxNeighbourhoodRadius;
            ConstantInjector.trustChainThreshold = trustChainThreshold;
            ConstantInjector.loggingFileName = loggingFileName;
            ConstantInjector.allowTransactionMonitoring = allowTransactionMonitoring;

        }
    }

}
