package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.WebSocketUserHashSessionName;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class WebSocketMapUserHashSessionName extends Collection<WebSocketUserHashSessionName> {

    public WebSocketMapUserHashSessionName() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
