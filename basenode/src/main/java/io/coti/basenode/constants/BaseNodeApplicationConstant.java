package io.coti.basenode.constants;

import io.coti.basenode.data.NetworkType;
import org.springframework.beans.factory.annotation.Value;

public class BaseNodeApplicationConstant {

    public final static NetworkType NETWORK_TYPE = ConstantInjector.networkType;
    public final static String NODE_IP = ConstantInjector.nodeIp;
    public final static String NODE_MANAGER_IP = ConstantInjector.nodeManagerIp;
    public final static String NODE_MANAGER_PORT = ConstantInjector.nodeManagerPort;
    public final static String NODE_MANAGER_PROPAGATION_PORT = ConstantInjector.nodeManagerPropagationPort;
    public final static String KYC_SERVER_ADDRESS = ConstantInjector.kycServerAddress;

    private static class ConstantInjector {

        private static NetworkType networkType;
        private static String nodeIp;
        private static String nodeManagerIp;
        private static String nodeManagerPort;
        private static String nodeManagerPropagationPort;
        private static String kycServerAddress;

        private ConstantInjector(@Value("${network}") NetworkType networkType,
                                 @Value("${server.ip}") String nodeIp,
                                 @Value("${node.manager.ip}") String nodeManagerIp,
                                 @Value("${node.manager.port}") String nodeManagerPort,
                                 @Value("${node.manager.propagation.port}") String nodeManagerPropagationPort,
                                 @Value("${kycserver.url}") String kycServerAddress) {

            ConstantInjector.networkType = networkType;
            ConstantInjector.nodeIp = nodeIp;
            ConstantInjector.nodeManagerIp = nodeManagerIp;
            ConstantInjector.nodeManagerPort = nodeManagerPort;
            ConstantInjector.nodeManagerPropagationPort = nodeManagerPropagationPort;
            this.kycServerAddress = kycServerAddress;


        }
    }

}
