package io.coti.basenode.config;

import io.coti.basenode.constants.BaseNodeApplicationConstant;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EarlyBeans {

    @Bean
    public Void earlyBean(ApplicationContext appContext) {

        appContext.getBeansOfType(BaseNodeApplicationConstant.class);

        return null;
    }

}

