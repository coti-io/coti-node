package io.coti.basenode.services;

import io.coti.basenode.data.NetworkType;
import io.coti.basenode.exceptions.RuleConditionValidationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public enum RulesConditionsService {
    NETWORK_TYPE_MAINNET(BaseNodeInitializationService.class, "getNetworkType", "eq", NetworkType.MainNet),
    NETWORK_TYPE_TESTNET(BaseNodeInitializationService.class, "getNetworkType", "eq", NetworkType.TestNet),
    FORCE_DSPC_FOR_TCC_MAINNET(TransactionIndexService.class, "getLastTransactionIndex", ">", Long.valueOf(700000)),
    FORCE_DSPC_FOR_TCC_TESTNET(TransactionIndexService.class, "getLastTransactionIndex", ">", Long.valueOf(100000));

    private final Class<? extends Object> serviceClass;
    private final String methodName;
    private final String condition;
    private final Object threshold;

    RulesConditionsService(Class<? extends Object> serviceClass, String methodName, String condition, Object threshold) {
        this.serviceClass = serviceClass;
        this.methodName = methodName;
        this.condition = condition;
        this.threshold = threshold;
    }

    public boolean isTransactionRuleApplicable(Object service) {
        if (service.getClass().equals(this.serviceClass)) {
            Object retVal = new Object();
            Method[] declaredMethods = service.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.getName().equals(methodName)
                        && method.getGenericParameterTypes().length == 0) {
                    try {
                        retVal = method.invoke(service);
                        break;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("Exception while attempting to invoke method: {} ", this.methodName);
                        throw new RuleConditionValidationException("Error validating rule.\n" + e.getMessage(), e);
                    }
                }
            }
            return isConditionMet(retVal);
        } else
            return false;
    }

    public boolean isConditionMet(Object retVal) {
        Class<? extends Object> classToConvertTo = this.threshold.getClass();
        if (retVal != null) {
            switch (this.condition) {
                case "eq":
                    return classToConvertTo.cast(retVal).equals(this.threshold);
                case ">":
                    return (Long) classToConvertTo.cast(retVal) > (Long) this.threshold;
                case "<":
                    return (Long) classToConvertTo.cast(retVal) < (Long) this.threshold;
                default:
                    return false;
            }
        }
        return false;
    }

}
