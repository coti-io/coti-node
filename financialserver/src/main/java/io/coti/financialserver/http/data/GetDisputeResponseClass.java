package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.DisputeData;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
public enum GetDisputeResponseClass {
    CONSUMER(ActionSide.CONSUMER, ConsumerDisputeResponseData.class),
    MERCHANT(ActionSide.MERCHANT, MerchantDisputeResponseData.class),
    ARBITRATOR(ActionSide.ARBITRATOR, ArbitratorDisputeResponseData.class) {
        @Override
        public GetDisputeResponseData getNewInstance(DisputeData disputeData, Hash userHash) {
            try {
                Constructor<? extends GetDisputeResponseData> constructor = getDisputeResponseClass.getConstructor(DisputeData.class, Hash.class);
                return constructor.newInstance(disputeData, userHash);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                log.error("Error at getting new instance for GetDisputeResponseClass", e);
                return null;
            }
        }
    };

    private ActionSide actionSide;
    protected Class<? extends GetDisputeResponseData> getDisputeResponseClass;

    private static class GetDisputeResponseClasses {
        private static final Map<ActionSide, GetDisputeResponseClass> getDisputeResponseClassMap = new EnumMap<>(ActionSide.class);
    }

    <T extends GetDisputeResponseData> GetDisputeResponseClass(ActionSide actionSide, Class<T> getDisputeResponseClass) {
        this.actionSide = actionSide;
        GetDisputeResponseClasses.getDisputeResponseClassMap.put(actionSide, this);
        this.getDisputeResponseClass = getDisputeResponseClass;
    }

    public static GetDisputeResponseClass getByActionSide(ActionSide actionSide) {
        return GetDisputeResponseClasses.getDisputeResponseClassMap.get(actionSide);
    }

    public ActionSide getActionSide() {
        return actionSide;
    }

    public GetDisputeResponseData getNewInstance(DisputeData disputeData, Hash userHash) {
        try {
            Constructor<? extends GetDisputeResponseData> constructor = getDisputeResponseClass.getConstructor(DisputeData.class);
            return constructor.newInstance(disputeData);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.error("Error at getting new instance for GetDisputeResponseClass", e);
            return null;
        }
    }
}
