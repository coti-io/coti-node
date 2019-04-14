package io.coti.basenode.http.data;

import io.coti.basenode.data.NetworkType;

public enum NetworkTypeName {
    AlphaNet(NetworkType.AlphaNet, "alphanet"),
    TestNet(NetworkType.TestNet, "testnet"),
    MainNet(NetworkType.MainNet, "mainnet");

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
