package io.coti.basenode.config;

import io.coti.basenode.http.data.NetworkTypeName;

import java.beans.PropertyEditorSupport;

public class NetworkTypeConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        setValue(NetworkTypeName.getNetworkType(text));
    }
}
