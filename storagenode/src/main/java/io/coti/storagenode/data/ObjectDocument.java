package io.coti.storagenode.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class ObjectDocument {
    private Hash hash;
    private String indexName;
    private String objectName;
    private String objectAsJsonString;

    public ObjectDocument(String indexName, String objectName, Hash hash, String objectAsJsonString) {
        this.hash = hash;
        this.indexName = indexName;
        this.objectName = objectName;
        this.objectAsJsonString = objectAsJsonString;
    }
}
