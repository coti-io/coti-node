package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.messages.StateMessageData;

public interface IStateMessageService {

    void init();

    void handleStateMessage(StateMessageData stateMessage);

}
