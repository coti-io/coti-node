package io.coti.basenode.http.data;

import io.coti.basenode.data.NetworkType;

public enum NetworkTypeName {
    ALPHA_NET(NetworkType.ALPHA_NET, "alphanet"),
    TEST_NET(NetworkType.TEST_NET, "testnet"),
    MAIN_NET(NetworkType.MAIN_NET, "mainnet");

    private NetworkType networkType;
    private String network;

    private NetworkTypeName(NetworkType networkType, String network) {
        this.networkType = networkType;
        this.network = network;
    }

    public String getNetwork() {
        return network;
    }

    public static NetworkType getNetworkType(String network) {
        for (NetworkTypeName networkTypeName : values()) {
            if (networkTypeName.network.equals(network)) {
                return networkTypeName.networkType;
            }
        }
        throw new IllegalArgumentException("Unknown network type " + network);
    }
}
