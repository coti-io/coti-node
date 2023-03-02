package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.interfaces.IEntity;

import java.math.BigDecimal;

public interface ITokenFeeData extends IEntity {

    String toString();

    BigDecimal getFeeAmount(BigDecimal amount);

    boolean valid();
}
