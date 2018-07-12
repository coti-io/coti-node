package io.coti.common.http;

import lombok.Data;

@Data
//@JsonRootName(value = "lastIndex")
public class GetLastIndexResponse extends Response {

    public int lastIndex;

    public GetLastIndexResponse() {
        super();
    }

    public GetLastIndexResponse(int lastIndex) {
        super();
        this.lastIndex = lastIndex;
    }
}