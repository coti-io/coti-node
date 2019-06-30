package io.coti.storagenode.data.enums;

public enum ElasticSearchData {

    ADDRESSES("addresses","addressData"),
    TRANSACTIONS("transactions","transactionData");

    private String index;
    private String object;

    ElasticSearchData(String index, String object) {
        this.index = index;
        this.object = object;
    }

    public String getIndex(){
        return index;
    }

    public String getObjectName(){
        return object;
    }
}
