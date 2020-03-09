package io.coti.basenode.http.data;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AddressStatus {
    CREATED("Created"),
    EXISTS("Exists");

    private String status;

    AddressStatus(String status) {
        this.status = status;
    }

    @JsonValue
    @Override
    public String toString() {
        return status;
    }
}
