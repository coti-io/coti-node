package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.MessageArrivalValidationData;

public interface IMessageArrivalValidationService {

    //TODO 3/24/2019 astolia: add methods.

    boolean verifyAndLogSingleMessageArrivalValidation(MessageArrivalValidationData message);

}
