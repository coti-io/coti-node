package io.coti.basenode.data.interfaces;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface IPropagatable extends IEntity {
}
