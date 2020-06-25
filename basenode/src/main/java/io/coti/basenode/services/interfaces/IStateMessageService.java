package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.messages.StateMessage;

public interface IStateMessageService {

    void init();

    void handleStateMessage(StateMessage stateMessage);

}
