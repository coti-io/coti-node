package io.coti.basenode.data;

public class WebSocketData {

    private Object data = null;

    public Object getData() {
        return this.data;
    }

    public String getAddress() {
        return this.address;
    }

    private String address = null;

    public WebSocketData(Object data, String address) {
        this.data = data;
        this.address = address;
    }
}
