package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.DisputeData;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public enum GetDisputeResponseClass {
    Consumer(ConsumerDisputeResponseData.class),
    Merchant(MerchantDisputeResponseData.class),
    Arbitrator(ArbitratorDisputeResponseData.class) {
        @Override
        public GetDisputeResponseData getNewInstance(DisputeData disputeData, Hash userHash) {
            try {
                Constructor<? extends GetDisputeResponseData> constructor = getDisputeResponseClass.getConstructor(DisputeData.class, Hash.class);
                return constructor.newInstance(disputeData, userHash);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                return null;
            }
        }
    };

    protected Class<? extends GetDisputeResponseData> getDisputeResponseClass;

    <T extends GetDisputeResponseData> GetDisputeResponseClass(Class<T> getDisputeResponseClass) {
        this.getDisputeResponseClass = getDisputeResponseClass;
    }

    public GetDisputeResponseData getNewInstance(DisputeData disputeData, Hash userHash) {
        try {
            Constructor<? extends GetDisputeResponseData> constructor = getDisputeResponseClass.getConstructor(DisputeData.class);
            return constructor.newInstance(disputeData);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
