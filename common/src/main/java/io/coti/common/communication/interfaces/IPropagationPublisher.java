package io.coti.common.communication.interfaces;

import io.coti.common.data.interfaces.IEntity;

public interface IPropagationPublisher {

    <T extends IEntity> void propagate(T toPropagate, String channel);
}
