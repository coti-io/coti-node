package io.coti.basenode.data;

import lombok.Data;

@Data
public class WebSocketData {

    private String address;
    private Object data;

    public WebSocketData(Object data, String address) {
        this.data = data;
        this.address = address;
    }
}
