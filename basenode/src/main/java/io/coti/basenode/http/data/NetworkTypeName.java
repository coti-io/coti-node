package io.coti.basenode.http.data;

import io.coti.basenode.data.NetworkType;

import java.util.EnumMap;
import java.util.Map;

public enum NetworkTypeName {
    ALPHA_NET(NetworkType.AlphaNet, "alphanet"),
    TEST_NET(NetworkType.TestNet, "testnet"),
    MAIN_NET(NetworkType.MainNet, "mainnet");

    private final NetworkType networkType;
    private final String network;

    private static class NetworkTypeNames {
        private static final Map<NetworkType, NetworkTypeName> networkTypeNameMap = new EnumMap<>(NetworkType.class);
    }

    NetworkTypeName(NetworkType networkType, String network) {
        this.networkType = networkType;
        NetworkTypeNames.networkTypeNameMap.put(networkType, this);
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

    public static NetworkTypeName getByNetworkType(NetworkType networkType) {
        NetworkTypeName networkTypeName = NetworkTypeNames.networkTypeNameMap.get(networkType);
        if (networkTypeName != null) {
            return networkTypeName;
        }

        throw new IllegalArgumentException("Unknown network type " + networkType);
    }
}
