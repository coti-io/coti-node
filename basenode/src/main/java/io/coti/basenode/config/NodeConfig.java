package io.coti.basenode.config;

import io.coti.basenode.crypto.NodeCryptoHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NodeConfig {

    @Value("#{'${global.private.key}'}")
    private String nodePrivateKey;

    @Bean
    public MethodInvokingFactoryBean nodePrivateKey() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setStaticMethod(NodeCryptoHelper.class.getName() + ".nodePrivateKey");
        methodInvokingFactoryBean.setArguments(nodePrivateKey);

        return methodInvokingFactoryBean;
    }
}
