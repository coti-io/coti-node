package io.coti.basenode.services;

import io.coti.basenode.exceptions.RuleConditionValidationException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public enum RulesConditionsService {
    FORCE_DSPC_FOR_TCC(TransactionIndexService.class, "getLastTransactionIndex", ">Long") {
        @Override
        public boolean isRuleApplicable(Object service, Object threshold) {
            return isTransactionRuleApplicable(service, (Long)threshold);
        }
    };

    private final Class<? extends Object> serviceClass;
    private final String methodName;
    private final String condition;

    RulesConditionsService(Class<? extends Object> serviceClass, String methodName, String condition) {
        this.serviceClass = serviceClass;
        this.methodName = methodName;
        this.condition = condition;
    }

    public boolean isTransactionRuleApplicable(Object service, Object threshold) {
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
            return isConditionMet(retVal, threshold);
        } else
            return false;
    }

    public boolean isConditionMet(Object retVal, Object threshold) {
        Class<? extends Object> classToConvertTo = threshold.getClass();
        if (retVal != null) {
            switch (this.condition) {
                case "eq":
                    return classToConvertTo.cast(retVal).equals(threshold);
                case ">Long":
                    return (Long) classToConvertTo.cast(retVal) > (Long) threshold;
                case "<Long":
                    return (Long) classToConvertTo.cast(retVal) < (Long) threshold;
                default:
                    return false;
            }
        }
        return false;
    }

    public abstract boolean isRuleApplicable(Object service, Object threshold);
}
